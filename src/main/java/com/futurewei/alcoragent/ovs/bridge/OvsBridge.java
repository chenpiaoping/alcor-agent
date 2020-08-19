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

import com.futurewei.alcoragent.ovs.flow.OvsFlow;
import com.futurewei.alcoragent.ovs.command.OfctlCommand;
import com.futurewei.alcoragent.ovs.command.VsctlCommand;

import java.util.List;
import java.util.Map;

public class OvsBridge implements Bridge {
    protected String bridgeName;
    protected boolean dropFlowsOnStart;

    public OvsBridge(String bridgeName, boolean dropFlowsOnStart) {
        this.bridgeName = bridgeName;
        this.dropFlowsOnStart = dropFlowsOnStart;
    }

    public String getBridgeName() {
        return bridgeName;
    }

    public void setBridgeName(String bridgeName) {
        this.bridgeName = bridgeName;
    }

    public boolean isDropFlowsOnStart() {
        return dropFlowsOnStart;
    }

    public void setDropFlowsOnStart(boolean dropFlowsOnStart) {
        this.dropFlowsOnStart = dropFlowsOnStart;
    }

    public void init() throws Exception {
        /**
         * 1.Create bridge: ovs-vsctl --may-exist add-br br-int set Bridge br-int datapath_type=system
         */
        VsctlCommand.addBridge(bridgeName, "set Bridge br-int datapath_type=system");

        /**
         * 2.Set security mode: ovs-vsctl set-fail-mode br-int secure
         */
        VsctlCommand.setFailMode(bridgeName, "secure");

        /**
         * 3.Delete controller: ovs-vsctl del-controller br-int
         */
        VsctlCommand.deleteController(bridgeName);

        /**
         * 4.Remove all flows: ovs-ofctl del-flows br-int
         */
        if (dropFlowsOnStart) {
            deleteFlows();
        }
    }

    @Override
    public void createBridge() throws Exception {
        VsctlCommand.addBridge(bridgeName, "");
    }

    @Override
    public void addPort(String portName) throws Exception {
        VsctlCommand.deletePort(bridgeName, portName);
    }

    @Override
    public void addPort(String portName, Map<String, String> attributes) throws Exception {
        VsctlCommand.deletePort(bridgeName, portName);

        VsctlCommand.setPortAttribute("Port", portName, "tag", String.valueOf(4095));

        for (Map.Entry<String, String> entry: attributes.entrySet()) {
            VsctlCommand.setPortAttribute("Interface", portName, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void deletePort(String portName) throws Exception {
        VsctlCommand.deletePort(bridgeName, portName);
    }

    @Override
    public List<String> listPort() throws Exception {
        return VsctlCommand.listPort(bridgeName);
    }

    @Override
    public void deleteFlows() throws Exception {
        OfctlCommand.deleteFlows(bridgeName);
    }

    @Override
    public void addFlows(OvsFlow flow) throws Exception {
        OfctlCommand.addFlows(bridgeName, flow.toString());
    }

    @Override
    public void modifyFlows(OvsFlow flow) throws Exception {
        OfctlCommand.modifyFlows(bridgeName, flow.toString());
    }
}
