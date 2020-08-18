/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcoragent.ovs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.schema.Neighbor.NeighborConfiguration;
import com.futurewei.alcor.schema.Neighbor.NeighborState;
import com.futurewei.alcor.schema.Port.PortConfiguration;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.schema.Subnet.SubnetState;
import com.futurewei.alcor.schema.Vpc.VpcConfiguration;
import com.futurewei.alcor.schema.Vpc.VpcState;
import com.futurewei.alcoragent.agent.Agent;
import com.futurewei.alcoragent.agent.AgentConfiguration;
import com.futurewei.alcoragent.ovs.bridge.OvsIntegrationBridge;
import com.futurewei.alcoragent.ovs.bridge.OvsPhysicalBridge;
import com.futurewei.alcoragent.ovs.bridge.OvsTunnelBridge;
import com.futurewei.alcoragent.ovs.command.VsctlCommand;
import com.futurewei.alcoragent.ovs.exception.LocalVlanNotFound;
import com.futurewei.alcoragent.ovs.exception.PortAttributeNotFound;
import com.futurewei.alcoragent.ovs.exception.VpcConfigurationNotFound;
import com.futurewei.alcoragent.ovs.flow.FLowTable;
import com.futurewei.alcoragent.ovs.flow.OvsFlow;
import com.futurewei.alcoragent.ovs.tunnel.TunnelManager;
import com.futurewei.alcoragent.ovs.vlan.LocalVlan;
import com.futurewei.alcoragent.ovs.vlan.VlanManager;
import org.apache.maven.shared.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class OvsAgent implements Agent {
    private static final Logger LOG = LoggerFactory.getLogger(OvsAgent.class);

    @Autowired
    private AgentConfiguration agentConfiguration;
    private OvsIntegrationBridge ovsIntegrationBridge;
    private OvsTunnelBridge ovsTunnelBridge;
    private OvsPhysicalBridge ovsPhysicalBridge;
    private String portAttributeTable;
    private String portConfigTable;
    private boolean preventArpSpoofing;
    private VlanManager vlanManager;
    private TunnelManager tunnelManager;

    @PostConstruct
    public void init() throws Exception {
        this.portAttributeTable = agentConfiguration.getPortAttributeTable();
        this.portConfigTable = agentConfiguration.getPortConfigTable();

        ovsIntegrationBridge = new OvsIntegrationBridge(agentConfiguration.getIntBridgeName(),
                agentConfiguration.isDropFlowsOnStart(),
                agentConfiguration.getIntPeerPatchPort());
        ovsIntegrationBridge.init();

        ovsTunnelBridge = new OvsTunnelBridge(this,
                agentConfiguration.getTunBridgeName(),
                agentConfiguration.isDropFlowsOnStart(),
                agentConfiguration.getIntPeerPatchPort(),
                agentConfiguration.getTunPeerPatchPort());
        ovsTunnelBridge.init();

        ovsPhysicalBridge = new OvsPhysicalBridge(this,
                agentConfiguration.getPhyBridgeName(),
                agentConfiguration.isDropFlowsOnStart(),
                agentConfiguration.getIntIfName(),
                agentConfiguration.getPhyIfName());
        ovsPhysicalBridge.init();

        vlanManager = new VlanManager(agentConfiguration.getStartVlanId(),
                agentConfiguration.getEndVlanId());

        tunnelManager = new TunnelManager();
    }

    @Override
    public void createGoalState(GoalState goalState) {

    }

    @Override
    public void createVpcState(VpcState vpcState) {

    }

    @Override
    public void createSubnetState(SubnetState subnetState) {

    }

    private PortAttribute.ExternalId parseExternalId(String input) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(input.getBytes(), PortAttribute.ExternalId.class);
    }

    private List<PortAttribute> getPortAttribute() throws Exception {
        List<String> ports = ovsIntegrationBridge.listPort();
        List<String> columns = Arrays.asList("name", "external_ids", "ofport");
        return VsctlCommand.getPortAttribute(portAttributeTable, ports, columns);
    }

    private PortAttribute getPortAttribute(PortState portState) throws Exception {
        String portId = portState.getConfiguration().getId();
        List<PortAttribute> portsAttributes = getPortAttribute();
        PortAttribute portAttribute = null;
        for (PortAttribute portAttr: portsAttributes) {
            String ifaceId = portAttr.getExternalId().getIfaceId();
            if (portId.startsWith(ifaceId)) {
                portAttribute = portAttr;
                break;
            }
        }

        if (portAttribute == null) {
            LOG.error("Can not find port attribute, ");
            throw new PortAttributeNotFound();
        }

        LOG.info("Find port attribute: {}", portAttribute);

        return portAttribute;
    }

    private VpcConfiguration getVpcConfiguration(PortState portState, GoalState goalState) throws Exception {
        String vpcId = portState.getConfiguration().getVpcId();
        VpcConfiguration vpcConfiguration = null;
        for (VpcState vpcState: goalState.getVpcStatesList()) {
            VpcConfiguration vpcConfig = vpcState.getConfiguration();
            if (vpcId.equals(vpcConfig.getId())) {
                vpcConfiguration = vpcConfig;
                break;
            }
        }

        if (vpcConfiguration == null) {
            LOG.error("Can not find vpc configuration in goalState");
            throw new VpcConfigurationNotFound();
        }

        LOG.info("Find port vpc configuration: {}", vpcConfiguration);

        return vpcConfiguration;
    }

    private void updatePortAttribute(String port, String attribute) throws IOException, InterruptedException {
        VsctlCommand.setPortAttribute(portConfigTable, port, "other_config", attribute);
    }

    @Override
    public void createPortState(PortState portState, GoalState goalState) throws Exception {
        LOG.debug("Create port state, portState: {}", portState);

        /**
         * Get the properties of all ports
         * ovs-vsctl --columns=name,external_ids,ofport list Interface port1 port2
         */
        PortAttribute portAttribute = getPortAttribute(portState);
        VpcConfiguration vpcConfiguration = getVpcConfiguration(portState, goalState);

        /**
         * Allocate local vlan id for the new vpc
         */
        PortConfiguration portConfiguration = portState.getConfiguration();
        String vpcId = portState.getConfiguration().getVpcId();
        LocalVlan localVlan = vlanManager.getLocalVlan(vpcId);
        if (localVlan == null) {
            String networkType = portConfiguration.getNetworkType().name();
            String physicalNetwork = null; //TODO:get physicalNetwork
            int segmentId = 0; //TODO:get segmentId
            localVlan = vlanManager.allocateLocalVlan(networkType,
                    physicalNetwork, vpcId, segmentId);

            /*
            List<Integer> tunnelPorts = tunnelManager.getTunnelPorts(networkType);
            if (tunnelPorts.size() > 0) {
                String tunnelPortString = StringUtils.join(tunnelPorts.toArray(), ",");

                Map<String, String> matchFields = new HashMap<>();
                matchFields.put("dl_vlan", String.valueOf(localVlan.getVlanId()));


                List<String> actions = new ArrayList<>();
                actions.add("strip_vlan");
                actions.add("set_tunnel:" + segmentId);
                actions.add("output:" + tunnelPortString);

                /**
                 * Install flood flow: ovs-ofctl mod-flows br-tun "table=22, dl_vlan=vlanId,
                 * actions=strip_vlan,set_tunnel:segmentId,output:tunnelPorts"
                 */
            /*
                ovsTunnelBridge.modifyFlows(new OvsFlow(FLowTable.FLOOD_TO_TUN, matchFields, actions));
            }*/

            /**
             * Install flow table entry for segment_id to vlan_id mapping
             * ovs-ofctl add-flows br-tun "table=4, priority=1, tun_id=segmentId,
             * actions=mod_vlan_vid:%s,resubmit(,10)"
             */
            Map<String, String> matchFields = new HashMap<>();
            matchFields.put("priority", String.valueOf(1));
            matchFields.put("tun_id", String.valueOf(segmentId));

            List<String> actions = new ArrayList<>();
            actions.add("mod_vlan_vid:" + localVlan.getVlanId());
            actions.add("resubmit(," + FLowTable.LEARN_FROM_TUN + ")");

            ovsTunnelBridge.addFlows(new OvsFlow(FLowTable.VXLAN_TUN_TO_LV, matchFields, actions));
        }

        /**
         * Set local vlan information to Port table
         * vlan_mapping = {'net_uuid': net_uuid,
         *                 'network_type': network_type,
         *                 'physical_network': str(physical_network),
         *                 'segmentation_id': str(segmentation_id)}
         * ovs-vsctl set Port port-name other_config="{net_uuid="net_uuid", network_type: network_type,
         * physical_network: str(physical_network), segmentation_id: str(segmentation_id)}"
         */

        Map<String, String> localVlanMap = new HashMap<>();
        localVlanMap.put("net_uuid", vpcId);
        localVlanMap.put("network_type", localVlan.getNetworkType());
        localVlanMap.put("physical_network", localVlan.getPhysicalNetwork());
        localVlanMap.put("segmentation_id", String.valueOf(localVlan.getSegmentId()));
        localVlanMap.put("tag", String.valueOf(localVlan.getVlanId()));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(localVlanMap);

        updatePortAttribute(portAttribute.getName(), json);

        /*
        if (portConfiguration.isPortSecurityEnabled()) {
            add flow for allowed_address_pairs
            add flow fro arp spoofing protection: ovs-ofctl add-flows br-tun "table=24,  priority=2,
            proto='arp', arp_spa=ip, in_port=port, actions=resubmit(,25)”, 其中25为MAC_SPOOF_TABLE
        }*/
    }

    private String getTunnelName(String networkType, String localIp, String remoteIp) {
        return networkType + "-" + localIp + ":" + remoteIp;
    }

    private Integer createTunnelPort(String networkType, String remoteIp) throws Exception {
        String tunnelName = getTunnelName(networkType, agentConfiguration.getLocalIp(), remoteIp);

        ovsTunnelBridge.addPort(tunnelName);

        Map<String, String> optionsMap = new HashMap<>();
        optionsMap.put("dst_port", String.valueOf(agentConfiguration.getVxlanDstPort()));
        optionsMap.put("df_default", String.valueOf(agentConfiguration.isDontFragment()));
        optionsMap.put("remote_ip", remoteIp);
        optionsMap.put("local_ip", agentConfiguration.getLocalIp());
        optionsMap.put("in_ley", "flow");
        optionsMap.put("out_ley", "flow");

        ObjectMapper mapper = new ObjectMapper();
        String optionsJson = mapper.writeValueAsString(optionsMap);

        VsctlCommand.setPortAttribute(portAttributeTable, tunnelName, "options", optionsJson);

        PortAttribute portAttribute = VsctlCommand.getPortAttribute(portAttributeTable,
                tunnelName, Collections.singletonList("ofport"));

        tunnelManager.addTunnelPort(networkType, remoteIp, portAttribute.getOfPort());

        Map<String, String> matchFields = new HashMap<>();
        matchFields.put("priority", String.valueOf(1));
        matchFields.put("in_port", String.valueOf(portAttribute.getOfPort()));

        List<String> actions = new ArrayList<>();
        actions.add("resubmit(," + FLowTable.VXLAN_TUN_TO_LV + ")");
        ovsTunnelBridge.addFlows(new OvsFlow(FLowTable.LOCAL_SWITCHING, matchFields, actions));

        return portAttribute.getOfPort();
    }

    @Override
    public void createNeighborState(NeighborState neighborState, GoalState goalState) throws Exception {
        NeighborConfiguration neighborConfiguration = neighborState.getConfiguration();
        String vpcId = neighborConfiguration.getVpcId();

        LocalVlan localVlan = vlanManager.getLocalVlan(vpcId);
        if (localVlan == null) {
            throw new LocalVlanNotFound();
        }

        String hostIpAddress = neighborConfiguration.getHostIpAddress();
        Integer ofPort = tunnelManager.getTunnelPort(localVlan.getNetworkType(), hostIpAddress);
        if (ofPort == null) {
            ofPort = createTunnelPort(localVlan.getNetworkType(), hostIpAddress);
        }

        String neighborIp = neighborConfiguration.getFixedIps(0).getIpAddress();
        String neighborMac = neighborConfiguration.getMacAddress();

        Map<String, String> matchFields1 = new HashMap<>();
        matchFields1.put("priority", String.valueOf(1));
        matchFields1.put("proto", "arp");
        matchFields1.put("dl_vlan", String.valueOf(localVlan.getVlanId()));
        matchFields1.put("nw_dst", neighborIp);

        List<String> actions1 = new ArrayList<>();
        actions1.add("move:NXM_OF_ETH_SRC[]->NXM_OF_ETH_DST[]");
        actions1.add("mod_dl_src:" + neighborMac);
        actions1.add("load:0x2->NXM_OF_ARP_OP[]");
        actions1.add("move:NXM_NX_ARP_SHA[]->NXM_NX_ARP_THA[]");
        actions1.add("move:NXM_OF_ARP_SPA[]->NXM_OF_ARP_TPA[]");
        actions1.add("load:" + neighborMac + "->NXM_NX_ARP_SHA[]");
        actions1.add("load:" + neighborIp + "->NXM_OF_ARP_SPA[]");
        actions1.add("in_port");

        ovsTunnelBridge.addFlows(new OvsFlow(FLowTable.ARP_RESPONDER, matchFields1, actions1));

        Map<String, String> matchFields2 = new HashMap<>();
        matchFields2.put("priority", String.valueOf(2));
        matchFields2.put("dl_vlan", String.valueOf(localVlan.getVlanId()));
        matchFields2.put("dl_dst", neighborConfiguration.getMacAddress());

        List<String> actions2 = new ArrayList<>();
        actions2.add("strip_vlan");
        actions2.add("set_tunnel:" + localVlan.getSegmentId());
        actions2.add("output:" + ofPort);

        ovsTunnelBridge.addFlows(new OvsFlow(FLowTable.UCAST_TO_TUN, matchFields2, actions2));
    }

    public OvsIntegrationBridge getOvsIntegrationBridge() {
        return ovsIntegrationBridge;
    }

    public void setOvsIntegrationBridge(OvsIntegrationBridge ovsIntegrationBridge) {
        this.ovsIntegrationBridge = ovsIntegrationBridge;
    }

    public OvsTunnelBridge getOvsTunnelBridge() {
        return ovsTunnelBridge;
    }

    public void setOvsTunnelBridge(OvsTunnelBridge ovsTunnelBridge) {
        this.ovsTunnelBridge = ovsTunnelBridge;
    }

    public OvsPhysicalBridge getOvsPhysicalBridge() {
        return ovsPhysicalBridge;
    }

    public void setOvsPhysicalBridge(OvsPhysicalBridge ovsPhysicalBridge) {
        this.ovsPhysicalBridge = ovsPhysicalBridge;
    }
}
