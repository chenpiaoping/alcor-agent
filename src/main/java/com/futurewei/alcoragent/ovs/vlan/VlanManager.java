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
package com.futurewei.alcoragent.ovs.vlan;

import java.util.HashMap;
import java.util.Map;

public class VlanManager {
    private int totalVlanIds;
    private VlanAllocator vlanAllocator;
    private Map<String, LocalVlan> allocatedLocalVlans = new HashMap<>();

    public VlanManager() {
        vlanAllocator = new VlanAllocatorImpl(1, 4095);
        totalVlanIds = 4095;
    }

    public VlanManager(int startVlanId, int endVlanId) {
        vlanAllocator = new VlanAllocatorImpl(startVlanId, endVlanId);
        totalVlanIds = endVlanId - startVlanId + 1;
    }

    public int getTotalVlanIds() {
        return totalVlanIds;
    }

    public int getUsedVlanIds() {
        return allocatedLocalVlans.size();
    }

    public LocalVlan allocateLocalVlan(String networkType, String physicalNetwork, String vpcId, int segmentId) throws Exception {
        LocalVlan localVlan = allocatedLocalVlans.get(vpcId);
        if (localVlan != null) {
            return localVlan;
        }

        //Allocate vlan id for vpc
        int vlanId = vlanAllocator.allocate(0);
        localVlan = new LocalVlan(networkType, physicalNetwork, vpcId, vlanId, segmentId);
        allocatedLocalVlans.put(vpcId, localVlan);

        return localVlan;
    }

    public LocalVlan getLocalVlan(String vpcId) {
        return allocatedLocalVlans.get(vpcId);
    }
}
