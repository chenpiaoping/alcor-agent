package com.futurewei.alcoragent.ovs.bridge;

import com.futurewei.alcoragent.ovs.flow.OvsFlow;

import java.util.List;

public interface Bridge {
    void createBridge() throws Exception;
    void addPort(String portName) throws Exception;
    void deletePort(String portName) throws Exception;
    List<String> listPort() throws Exception;
    void deleteFlows() throws Exception;
    void addFlows(OvsFlow flow) throws Exception;
    void modifyFlows(OvsFlow flow) throws Exception;
}
