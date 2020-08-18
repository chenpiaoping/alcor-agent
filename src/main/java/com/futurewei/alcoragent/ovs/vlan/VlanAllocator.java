package com.futurewei.alcoragent.ovs.vlan;

public interface VlanAllocator {
    int allocate(int vlanId) throws Exception;
    public void release(int vlanId) throws Exception;
}
