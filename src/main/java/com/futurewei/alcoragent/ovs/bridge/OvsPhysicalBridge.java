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
package com.futurewei.alcoragent.ovs.bridge;

import com.futurewei.alcoragent.ovs.OvsAgent;
import com.futurewei.alcoragent.ovs.command.VsctlCommand;
import com.futurewei.alcoragent.ovs.flow.FLowTable;
import com.futurewei.alcoragent.ovs.flow.FlowAction;
import com.futurewei.alcoragent.ovs.flow.OvsFlow;

import java.util.List;


public class OvsPhysicalBridge extends OvsBridge {
    private String intIfName;
    private String phyIfName;
    private OvsAgent ovsAgent;

    public OvsPhysicalBridge(OvsAgent ovsAgent, String bridgeName, boolean dropFlowsOnStart,
                             String intIfName, String phyIfName) {
        super(bridgeName, dropFlowsOnStart);
        this.ovsAgent = ovsAgent;
        this.bridgeName = bridgeName;
        this.intIfName = intIfName;
        this.phyIfName = phyIfName;
    }

    public void init() throws Exception {
        /**
         * 1.Get all bridges: ovs-vsctl list-br
         */
        List<String> bridges = VsctlCommand.listBridge();
        if (!bridges.contains(bridgeName)) {
            //throw new PhyBridgeNotFound();
        }

        /**
         *2.Set bridge datapath_type: ovs-vsctl --may-exist add-br br-phy set Bridge br-int datapath_type=system
         *3.Set security mode: ovs-vsctl set-fail-mode br-phy secure
         *4.Delete controller: ovs-vsctl del-controller br-phy
         */
        super.init();

        /**
         * 6.Install normal flow: ovs-ofctl add-flows br-phy "table=0, priority=0, actions=normal"
         */
        addFlows(new OvsFlow(FLowTable.LOCAL_SWITCHING, 0, FlowAction.NORMAL));

        /**
         * 7.Create port: ovs-vsctl --may-exist add-port br-int int-xxx
         */
        ovsAgent.getOvsIntegrationBridge().addPort(intIfName);

        /**
         * 8.Create port: ovs-vsctl --may-exist add-port br-phy phy-xxx
         */
        addPort(phyIfName);

        /**
         * 9.Drop flows: ovs-ofctl add-flows br-int "table=O, priority=2, in_port=int_if_name, actions=drop"
         */
        ovsAgent.getOvsIntegrationBridge().addFlows(
                new OvsFlow(FLowTable.LOCAL_SWITCHING, 2, intIfName, FlowAction.DROP));

        /**
         * 10.Drop flows: ovs-ofctl add-flows br-phy "table=O, priority=2, i n_port=phys_if_name, actions=drop"
         */
        addFlows(new OvsFlow(FLowTable.LOCAL_SWITCHING, 2, phyIfName, FlowAction.DROP));

        //11.Set status(up) and mtu
    }
}
