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
import com.futurewei.alcor.schema.Router.RouterConfiguration.Subnet;
import com.futurewei.alcor.schema.Router.RouterConfiguration.FloatingIp;
import com.futurewei.alcoragent.ovs.bridge.Bridge;
import com.futurewei.alcoragent.ovs.command.VsctlCommand;
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
    private ExternalPort externalPort;
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
                String command = String.format("ip link set %s address %s", portName, macAddress);
                CommandUtil.execute(command);
                break;
            } catch (Exception e) {}
        }
    }

    private void addPortToNamespace(String portName) throws Exception {
        String command = String.format("ip link set %s netns %s", portName, namespace.getName());
        CommandUtil.execute(command);
    }

    private void setPortMtu(String portName, int mtu) throws Exception {
        String command = String.format("ip link set %s mtu %d", portName, mtu);
        CommandUtil.execute(commandPrefix + command);
    }

    private void setPortStatus(String portName, String status) throws Exception {
        String command = String.format("ip link set %s %s", portName, status);
        CommandUtil.execute(command;
    }

    private void createRouterPort(String portName, String portId, String macAddress, int mtu, Bridge bridge) throws Exception {
        Map<String, String> externalIdsMap = new HashMap<>();
        externalIdsMap.put("iface-id", portId);
        externalIdsMap.put("iface-status", "active");
        externalIdsMap.put("attached-mac", macAddress);

        ObjectMapper mapper = new ObjectMapper();
        String externalIdsString = mapper.writeValueAsString(externalIdsMap);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("external_ids", externalIdsString);
        attributes.put("type", "internal");

        bridge.addPort(portName, attributes);

        //Set mac address for interface
        setPortMacAddress(portName, macAddress);

        //Add interface to namespace
        addPortToNamespace(portName);

        //Set MTU for interface
        setPortMtu(portName, mtu);

        //Set the interface's status to up
        setPortStatus(portName, "up");
    }

    private void createInternalPort(String portName, InternalPort internalPort, Bridge bridge) throws Exception {
        createRouterPort(portName, internalPort.getId(),
                internalPort.getMacAddress(), internalPort.getMtu(), bridge);
        internalPorts.put(portName, internalPort);
    }

    private void addPortIpAddress(String portName, String ipAddress) throws Exception {
        String command = String.format("ip address add %s dev %s scope global", portName, ipAddress);
        CommandUtil.execute(commandPrefix + command);
    }

    private void sendArping(String portName, String ipAddress) throws Exception {
        String command = String.format("arping -U -I %s -c 1 -w 1.5 %s", portName, ipAddress);
        CommandUtil.execute(commandPrefix + command);

        command = String.format("arping -A -I %s -c 1 -w 1.5 %s", portName, ipAddress);
        CommandUtil.execute(commandPrefix + command);
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

    private void createExternalPort(String portName, ExternalPort externalPort, Bridge bridge) throws Exception {
        createRouterPort(portName, externalPort.getId(),
                externalPort.getMacAddress(), externalPort.getMtu(), bridge);

        //Clear tag
        VsctlCommand.clearPortAttribute("Port", portName, "tag");

        this.externalPort = externalPort;
    }

    private void addAddressScopeRouting(String portName, String addressScope) throws Exception {
        String rule = String.format("-o %s -m conmark --mark 0x0/0xffff0000 -j CONNMARK " +
                "--save-mark --nfmask 0xffff0000 --ctmask 0xffff0000", portName);
        CommandUtil.execute(commandPrefix + "iptables -t nat -A POSTROUTING " + rule);

        if (addressScope != null) {
            rule = String.format("-o %s -m connmark --mark %s -j CONNMARK ", portName, addressScope);
            CommandUtil.execute(commandPrefix + "iptables -t nat -A snat " + rule);
        }
    }

    private void addSnatRule(String portName, String sourceIp) throws Exception {
        String rule = String.format("-o %s -j SNAT --to-source %s ", portName, sourceIp);
        CommandUtil.execute(commandPrefix + "iptables -t nat -A snat " + rule);

        rule = String.format("! -i %s -o %s  -m connrack ! --ctstate DNAT -j ACCEPT", portName, portName);
        CommandUtil.execute(commandPrefix + "iptables -t nat -A POSTROUTING " + rule);

        rule = "-m mark ! --mark xxx/xxx -m conntrack --ctstate DNAT -j SNAT --to-source";
        CommandUtil.execute(commandPrefix + "iptables -t nat -A snat " + rule);

        rule = String.format("-i %s  -j MARK --set-xmark xxx/xxx", portName);
        CommandUtil.execute(commandPrefix + "iptables -t mangle -A mark " + rule);
    }

    private void addSnatRules(String portName, ExternalPort externalPort) throws Exception {
        addAddressScopeRouting(portName, externalPort.getAddressScope());

        List<FixedIp> fixedIps = externalPort.getFixedIpList();
        for (FixedIp fixedIp: fixedIps) {
            addSnatRule(portName, fixedIp.getIpAddress());
        }
    }

    public void addExternalPort(ExternalPort externalPort, Bridge bridge) throws Exception {
        String portName = getExternalPortName(externalPort.getId());

        createExternalPort(portName, externalPort, bridge);

        //gateway not in subnet

        //Add ip addresses to interface
        List<FixedIp> fixedIps = externalPort.getFixedIpList();
        for (FixedIp fixedIp: fixedIps) {
            addPortIpAddress(portName, fixedIp.getIpAddress());
        }

        //Add default routes
        List<Subnet> subnets = externalPort.getSubnetList();
        if (subnets != null) {
            for (Subnet subnet: subnets) {
                addDefaultRoute(subnet.getGatewayIp());
            }
        }

        //Send arping to each fixedIP
        for (FixedIp fixedIp: fixedIps) {
            sendArping(portName, fixedIp.getIpAddress());
        }

        addAddressScopeRouting(portName, null);

        addSnatRules(portName, externalPort);
    }

    private void addDnatRule(String fixedIp, String floatingIp) throws Exception {
        String rule = String.format("-s %s/32 -j SNAT --to-source %s", fixedIp, floatingIp);

        CommandUtil.execute(commandPrefix + "iptables -t mangle -A float-snat " + rule);

        rule = String.format("-d %s/32 -j DNAT --to-destination %s", floatingIp, fixedIp);
        CommandUtil.execute(commandPrefix + "iptables -t mangle -A PREROUTING " + rule);
        CommandUtil.execute(commandPrefix + "iptables -t mangle -A OUTPUT " + rule);
    }

    private void addDnatRules(List<FloatingIp> floatingIps) throws Exception {
        for (FloatingIp floatingIp: floatingIps) {
            addDnatRule(floatingIp.getFixedIpAddress(), floatingIp.getFloatingIpAddress());
        }
    }

    public void addFloatingIps(List<FloatingIp> floatingIps) throws Exception {
        //Add dnat rules
        addDnatRules(floatingIps);

        //Add floating ips
    }


    private void addRoute(Route route) throws Exception {
        CommandUtil.execute(commandPrefix + "ip route replace to " +
                route.getDestination() + " via" + route.getNexthop());
    }

    private void addDefaultRoute(String gateway) throws Exception {
        CommandUtil.execute(commandPrefix + "ip route replace default via " + gateway);
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
