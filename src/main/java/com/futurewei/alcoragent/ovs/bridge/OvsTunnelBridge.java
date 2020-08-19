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

public class OvsTunnelBridge extends OvsBridge {
    private String patchIntPort;
    private String patchTunPort;
    private OvsAgent ovsAgent;

    public OvsTunnelBridge(OvsAgent ovsAgent, String bridgeName, boolean dropFlowsOnStart, String patchIntPort, String patchTunPort) {
        super(bridgeName, dropFlowsOnStart);
        this.ovsAgent = ovsAgent;
        this.patchIntPort = patchIntPort;
        this.patchTunPort = patchTunPort;
    }

    public void init() throws Exception {
        /**
         * 1.Create bridge: ovs-vsctl --may-exist add-br br-tun set Bridge br-int datapath_type=system
         * 2.Delete controller: ovs-vsctl del-controller br-tun
         */
        super.init();

        /**
         * 3.Create port: ovs-vsctl --may-exist add-port br-int patchTunPort
         * ovs-vsctl set xxx
         * attrs = [('type', 'patch'),
         * ('options', {'peer': remote_name})]
         */
        ovsAgent.getOvsIntegrationBridge().addPort(patchTunPort);


        /**
         * 4.Create port: ovs-vsctl --may-exist add-port br-tun patchIntPort
         * ovs-vsctl set xxx
         * attrs = [('type', 'patch'),
         * ('options', {'peer': remote_name})]
         */
        addPort(patchIntPort);
    }
}
