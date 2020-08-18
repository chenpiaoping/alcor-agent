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

import com.futurewei.alcoragent.ovs.exception.InvalidVlanId;
import com.futurewei.alcoragent.ovs.exception.VlanIdExhaust;
import com.futurewei.alcoragent.ovs.exception.VlanIdHasAllocated;

import java.util.BitSet;

public class VlanAllocatorImpl implements VlanAllocator {
    private int startVlanId;
    private int endVlanId;
    private int totalVlanIds;
    private int usedVlanIds;
    private BitSet bitSet;

    public VlanAllocatorImpl(int startVlanId, int endVlanId) {
        this.startVlanId = startVlanId;
        this.endVlanId = endVlanId;
        this.totalVlanIds = endVlanId - startVlanId + 1;
        bitSet = new BitSet();
    }

    @Override
    public int allocate(int vlanId) throws Exception {
        int freeBit;

        if (vlanId != 0) {
            if (vlanId < startVlanId || vlanId > endVlanId) {
                throw new InvalidVlanId();
            }

            if (bitSet.get(vlanId)) {
                throw new VlanIdHasAllocated();
            }

            return vlanId;
        } else {
            freeBit = bitSet.nextClearBit(0);

            if (freeBit < 0 || freeBit >= totalVlanIds) {
                throw new VlanIdExhaust();
            }
        }

        bitSet.set(freeBit);

        return startVlanId + freeBit;
    }

    @Override
    public void release(int vlanId) throws Exception {
        if (vlanId < startVlanId || vlanId > endVlanId) {
            throw new InvalidVlanId();
        }

        bitSet.clear(vlanId - startVlanId);
    }
}
