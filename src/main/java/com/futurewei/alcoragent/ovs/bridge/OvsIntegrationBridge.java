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

import com.futurewei.alcoragent.ovs.flow.FLowTable;
import com.futurewei.alcoragent.ovs.flow.FlowAction;
import com.futurewei.alcoragent.ovs.flow.OvsFlow;

public class OvsIntegrationBridge extends OvsBridge {
    private String intPeerPatchPort;

    public OvsIntegrationBridge(String bridgeName, boolean dropFlowsOnStart, String intPeerPatchPort) {
        super(bridgeName, dropFlowsOnStart);
        this.intPeerPatchPort = intPeerPatchPort;
    }

    public void init() throws Exception {
        /**
         * 1.Create bridge: ovs-vsctl --may-exist add-br br-int set Bridge br-int datapath_type=system
         * 2.Set security mode: ovs-vsctl set-fail-mode br-int secure
         * 3.Delete controller: ovs-vsctl del-controller br-int
         */
        super.init();

        /**
         * 4.Delete port: ovs-vsctl --if-exists del-port br-int intPeerPatchPort
         */
        if (dropFlowsOnStart) {
            deletePort(intPeerPatchPort);
        }

        /**
         * 6.Install drop flow: ovs-ofctl add-flows br-int "table=23, priority=0, actions=drop"
         */
        addFlows(new OvsFlow(FLowTable.CANARY_TABLE, 0, FlowAction.DROP));

        /**
         * 7.Install normal flow: ovs-ofctl add-flows br-int "table=0, priority=0, actions=normal"
         */
        addFlows(new OvsFlow(FLowTable.LOCAL_SWITCHING, 0, FlowAction.NORMAL));

        /**
         * 8.Install drop flow: ovs-ofctl add-flows br-int "table=24, priority=0, actions=drop"
         * table=24 for ARP poison/spoofing prevention rules
         */
        addFlows(new OvsFlow(FLowTable.ARP_SPOOF_TABLE, 0, FlowAction.DROP));
    }
}
