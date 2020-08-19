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
package com.futurewei.alcoragent.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {
    @Value("${int.bridge.name:#{\"br-int\"}}")
    private String intBridgeName;

    @Value("${tun.bridge.name:#{\"br-tun\"}}")
    private String tunBridgeName;

    @Value("${phy.bridge.name:#{\"br-phy\"}}")
    private String phyBridgeName;

    @Value("${drop.flows.on.start:#{true}}")
    private boolean dropFlowsOnStart;

    @Value("${patch.int.port:#{\"patch-int\"}}")
    private String patchIntPort;

    @Value("${patch.tun.port:#{\"patch-tun\"}}")
    private String patchTunPort;

    @Value("${int.if.name:#{\"int-xxx\"}}")
    private String intIfName;

    @Value("${phy.if.name:#{\"phy-xxx\"}}")
    private String phyIfName;

    @Value("${vlan.id.start:#{1}}")
    private int startVlanId;

    @Value("${vlan.id.end:#{4095}}")
    private int endVlanId;

    @Value("${ovs.port.attribute.table:#{\"Interface\"}}")
    private String portAttributeTable;

    @Value("${ovs.port.config.table:#{\"Port\"}}")
    private String portConfigTable;

    @Value("${vxlan.dst.port:#{4578}}")
    private int vxlanDstPort;

    @Value("${vxlan.dont.fragment:#{true}}")
    private boolean dontFragment;

    @Value("${vxlan.local.ip:#{\"127.0.0.1\"}}")
    private String localIp;

    public String getIntBridgeName() {
        return intBridgeName;
    }

    public void setIntBridgeName(String intBridgeName) {
        this.intBridgeName = intBridgeName;
    }

    public String getTunBridgeName() {
        return tunBridgeName;
    }

    public void setTunBridgeName(String tunBridgeName) {
        this.tunBridgeName = tunBridgeName;
    }

    public String getPhyBridgeName() {
        return phyBridgeName;
    }

    public void setPhyBridgeName(String phyBridgeName) {
        this.phyBridgeName = phyBridgeName;
    }

    public boolean isDropFlowsOnStart() {
        return dropFlowsOnStart;
    }

    public void setDropFlowsOnStart(boolean dropFlowsOnStart) {
        this.dropFlowsOnStart = dropFlowsOnStart;
    }

    public String getPatchIntPort() {
        return patchIntPort;
    }

    public void setPatchIntPort(String patchIntPort) {
        this.patchIntPort = patchIntPort;
    }

    public String getPatchTunPort() {
        return patchTunPort;
    }

    public void setPatchTunPort(String patchTunPort) {
        this.patchTunPort = patchTunPort;
    }

    public String getIntIfName() {
        return intIfName;
    }

    public void setIntIfName(String intIfName) {
        this.intIfName = intIfName;
    }

    public String getPhyIfName() {
        return phyIfName;
    }

    public void setPhyIfName(String phyIfName) {
        this.phyIfName = phyIfName;
    }

    public int getStartVlanId() {
        return startVlanId;
    }

    public void setStartVlanId(int startVlanId) {
        this.startVlanId = startVlanId;
    }

    public int getEndVlanId() {
        return endVlanId;
    }

    public void setEndVlanId(int endVlanId) {
        this.endVlanId = endVlanId;
    }

    public String getPortAttributeTable() {
        return portAttributeTable;
    }

    public void setPortAttributeTable(String portAttributeTable) {
        this.portAttributeTable = portAttributeTable;
    }

    public String getPortConfigTable() {
        return portConfigTable;
    }

    public void setPortConfigTable(String portConfigTable) {
        this.portConfigTable = portConfigTable;
    }

    public int getVxlanDstPort() {
        return vxlanDstPort;
    }

    public void setVxlanDstPort(int vxlanDstPort) {
        this.vxlanDstPort = vxlanDstPort;
    }

    public boolean isDontFragment() {
        return dontFragment;
    }

    public void setDontFragment(boolean dontFragment) {
        this.dontFragment = dontFragment;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }
}
