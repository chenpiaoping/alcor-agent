package com.futurewei.alcoragent.ovs.flow;

public enum FLowTable {
    LOCAL_SWITCHING(0),
    VXLAN_TUN_TO_LV(4),
    LEARN_FROM_TUN(10),
    UCAST_TO_TUN(20),
    ARP_RESPONDER(20),
    FLOOD_TO_TUN(22),
    CANARY_TABLE(23),
    ARP_SPOOF_TABLE(24),
    BASE_EGRESS_TABLE(71),
    RULES_EGRESS_TABLE(72),
    ACCEPT_OR_INGRESS_TABLE(73),
    BASE_INGRESS_TABLE(81),
    RULES_INGRESS_TABLE(82);

    private int table;

    FLowTable(int table) {
        this.table = table;
    }

    public int getTable() {
        return table;
    }

    public void setTable(int table) {
        this.table = table;
    }
}
