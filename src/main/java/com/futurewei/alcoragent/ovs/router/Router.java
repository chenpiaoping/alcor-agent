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
package com.futurewei.alcoragent.ovs.router;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.schema.Router.RouterConfiguration.InternalPort;
import com.futurewei.alcor.schema.Router.RouterConfiguration.ExternalPort;
import com.futurewei.alcor.schema.Router.RouterConfiguration.FixedIp;
import com.futurewei.alcor.schema.Router.RouterConfiguration.Route;
import com.futurewei.alcoragent.ovs.bridge.Bridge;
import com.futurewei.alcoragent.util.CommandUtil;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private static final String INTERNAL_PORT_PREFIX = "qr-";
    private static final String EXTERNAL_PORT_PREFIX = "qr-";

    private String id;
    private RouterNamespace namespace;
    private IptablesManager iptablesManager;
    private String commandPrefix;
    private Map<String, InternalPort> internalPorts;
    private List<Route> routes;

    public Router(String id) throws IOException, InterruptedException {
        this.id = id;
        this.namespace = new RouterNamespace(id);
        this.namespace.create(false);
        this.commandPrefix = "ip netns exec " + namespace.getName();
        this.internalPorts = new HashMap<>();
        this.routes = new ArrayList<>();
    }

    private String getInternalPortName(String portId) {
        return INTERNAL_PORT_PREFIX + portId.substring(14);
    }

    private boolean portExists(String portName) throws Exception {
        String output = CommandUtil.execute(commandPrefix + "ip link show | grep " + portName);
        return !StringUtils.isEmpty(output);
    }

    private void setPortMacAddress(String portName, String macAddress) throws Exception {
        while (true) {
            try {
                CommandUtil.execute("ip link set " + portName + " address " + macAddress);
                break;
            } catch (Exception e) {}
        }
    }

    private void addPortToNamespace(String portName) throws Exception {
        CommandUtil.execute("ip link set " + portName + " netns " + namespace.getName());
    }

    private void setPortMtu(String portName, int mtu) throws Exception {
        CommandUtil.execute(commandPrefix + "ip link set " +
                portName + " mtu " + mtu);
    }

    private void setPortStatus(String portName, String status) throws Exception {
        CommandUtil.execute("ip link set " + portName + " " + status);
    }

    private void createInternalPort(String portName, InternalPort internalPort, Bridge bridge) throws Exception {
        Map<String, String> externalIdsMap = new HashMap<>();
        externalIdsMap.put("iface-id", internalPort.getId());
        externalIdsMap.put("iface-status", "active");
        externalIdsMap.put("attached-mac", internalPort.getMacAddress());

        ObjectMapper mapper = new ObjectMapper();
        String externalIdsString = mapper.writeValueAsString(externalIdsMap);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("external_ids", externalIdsString);
        attributes.put("type", "internal");

        bridge.addPort(portName, attributes);

        //Set mac address for interface
        setPortMacAddress(portName, internalPort.getMacAddress());

        //Add interface to namespace
        addPortToNamespace(portName);


        //Set MTU for interface
        setPortMtu(portName, internalPort.getMtu());

        //Set the interface's status to up
        setPortStatus(portName, "up");

        internalPorts.put(portName, internalPort);
    }

    private void addPortIpAddress(String portName, String ipAddress) throws Exception {
        CommandUtil.execute(commandPrefix + "ip address add " + ipAddress +
                " dev " + portName + " scope global");
    }

    private void sendArping(String portName, String ipAddress) throws Exception {
        CommandUtil.execute(commandPrefix + "arping -U -I " +
                portName + " -c 1 -w 1.5 " + ipAddress);

        CommandUtil.execute(commandPrefix + "arping -A -I " +
                portName + " -c 1 -w 1.5 " + ipAddress);
    }

    public void addInternalPort(InternalPort internalPort, Bridge bridge) throws Exception {
        String portName = getInternalPortName(internalPort.getId());

        //Create interface
        if (portExists(portName)) {
            createInternalPort(portName, internalPort, bridge);
        } else {
            setPortMtu(portName, internalPort.getMtu());
        }

        //Add ip addresses to interface
        List<FixedIp> fixedIps = internalPort.getFixedIpList();
        for (FixedIp fixedIp: fixedIps) {
            addPortIpAddress(portName, fixedIp.getIpAddress());
        }

        //Add/Delete onlink routes here if necessary

        //Send arping to each fixedIP
        for (FixedIp fixedIp: fixedIps) {
            sendArping(portName, fixedIp.getIpAddress());
        }
    }

    private String getExternalPortName(String portId) {
        return INTERNAL_PORT_PREFIX + portId.substring(14);
    }

    private void createExternalPort(String portName, ExternalPort internalPort) {

    }


    public void addExternalPort(ExternalPort externalPort) {
        String portName = getExternalPortName(externalPort.getId());

        createExternalPort(portName, externalPort);


    }

    private void addRoute(Route route) throws Exception {
        CommandUtil.execute(commandPrefix + "ip route replace to " +
                route.getDestination() + " via" + route.getNexthop());
    }

    private void deleteRoute(Route route) throws Exception {
        CommandUtil.execute(commandPrefix + "ip route delete to " +
                route.getDestination() + " via" + route.getNexthop());
    }

    public void addRoutes(List<Route> routes) throws Exception {
        for (Route route: routes) {
            addRoute(route);
        }

        this.routes.addAll(routes);
    }

    public void deleteRoutes(List<Route> routes) throws Exception {
        for (Route route: routes) {
            deleteRoute(route);
        }

        this.routes.removeAll(routes);
    }
}
