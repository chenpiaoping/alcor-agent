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
package com.futurewei.alcoragent.ovs.tunnel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TunnelManager {
    private Map<String, Map<String, Integer>> tunnelPorts;

    public TunnelManager() {
        tunnelPorts = new HashMap<>();
    }

    public Integer getTunnelPort(String networkType, String remoteIp) {
        if (!tunnelPorts.containsKey(networkType)) {
            return null;
        }

        return tunnelPorts.get(networkType).get(remoteIp);
    }

    public void addTunnelPort(String networkType, String remoteIp, Integer ofPort) {
        if (!tunnelPorts.containsKey(networkType)) {
            tunnelPorts.put(networkType, new HashMap<>());
        }

        tunnelPorts.get(networkType).put(remoteIp, ofPort);
    }

    public List<Integer> getTunnelPorts(String networkType) {
        if (!tunnelPorts.containsKey(networkType)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(tunnelPorts.get(networkType).values());
    }
}
