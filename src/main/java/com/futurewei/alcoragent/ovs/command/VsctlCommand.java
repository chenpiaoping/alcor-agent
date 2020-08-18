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
package com.futurewei.alcoragent.ovs.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcoragent.ovs.PortAttribute;
import org.apache.maven.shared.utils.StringUtils;

import java.io.IOException;
import java.util.*;

public class VsctlCommand extends OvsCommand {
    private static final String VSCTL_PROCESS = "ovs-vsctl";
    private static final String ADD_BR_COMMAND = "add-br";
    private static final String LIST_BR_COMMAND = "list-br";
    private static final String SET_FAIL_MODE_COMMAND = "set-fail-mode";
    private static final String ADD_PORT_COMMAND = "add-port";
    private static final String DEL_PORT_COMMAND = "del-port";
    private static final String LIST_PORT_COMMAND = "del-port";
    private static final String LIST_COMMAND = "list";
    private static final String SET_COMMAND = "set";

    private static final String MAY_EXIST_OPTION = "--may-exist";
    private static final String IF_EXIST_OPTION = "--if-exists";

    public static void addBridge(String bridgeName, String args) throws IOException, InterruptedException {
        execute(VSCTL_PROCESS, "--may-exist add-br " + bridgeName + " " + args);
    }

    public static void setFailMode(String bridgeName, String mode) throws IOException, InterruptedException {
        execute(VSCTL_PROCESS, "set-fail-mode " + bridgeName + " " + mode);
    }

    public static void deleteController(String bridgeName) throws IOException, InterruptedException {
        execute(VSCTL_PROCESS, "set-fail-mode " + bridgeName);
    }

    public static void addPort(String bridgeName, String portName) throws IOException, InterruptedException {
        execute(VSCTL_PROCESS, "--if-exists add-port " + bridgeName +  " " + portName);
    }

    public static void deletePort(String bridgeName, String portName) throws IOException, InterruptedException {
        execute(VSCTL_PROCESS, "--if-exists del-port " + bridgeName +  " " + portName);
    }

    public static List<String> listPort(String bridgeName) throws IOException, InterruptedException {
        String result = execute(VSCTL_PROCESS, " list-port " + bridgeName);
        return Arrays.asList(result.split("\n"));
    }

    public static List<String> listBridge() throws IOException, InterruptedException {
        String result = execute(VSCTL_PROCESS, "--if-exists list-br ");
        return Arrays.asList(result.split("\n"));
    }

    private static PortAttribute.ExternalId parseExternalId(String input) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(input.getBytes(), PortAttribute.ExternalId.class);
    }

    public static PortAttribute getPortAttribute(String table, String port, List<String> columns) throws IOException, InterruptedException {
        String options = StringUtils.join(columns.toArray(), ",");
        String args = table + port;

        String output = execute(VSCTL_PROCESS, options, LIST_COMMAND, args);
        String[] fields = output.split("\n");
        String name = null;
        PortAttribute.ExternalId externalId = null;
        int ofPort = 0;
        for (String field: fields) {
            //portAttribute.put(field.split(":")[0], field.split(":")[1]);
            String key = field.split(":")[0];
            String value = field.split(":")[1];
            if ("name".equals(key)) {
                name = value;
            } else if ("ofport".equals(key)){
                ofPort = Integer.parseInt(value);
            } else if ("external_ids".equals(key)) {
                externalId = parseExternalId(value);
            }
        }

        return new PortAttribute(name, externalId, ofPort);
    }

    public static List<PortAttribute> getPortAttribute(String table, List<String> ports, List<String> columns) throws IOException, InterruptedException {
        String options = StringUtils.join(columns.toArray(), ",");
        String args = table + StringUtils.join(ports.toArray(), " ");

        String output = execute(VSCTL_PROCESS, options, LIST_COMMAND, args);
        List<PortAttribute> portAttributes = new ArrayList<>();
        String[] lines = output.split("\r\n");
        for (String line: lines) {
            String[] fields = line.split("\n");
            String name = null;
            PortAttribute.ExternalId externalId = null;
            int ofPort = 0;
            for (String field: fields) {
                //portAttribute.put(field.split(":")[0], field.split(":")[1]);
                String key = field.split(":")[0];
                String value = field.split(":")[1];
                if ("name".equals(key)) {
                    name = value;
                } else if ("ofport".equals(key)){
                    ofPort = Integer.parseInt(value);
                } else if ("external_ids".equals(key)) {
                    externalId = parseExternalId(value);
                }
            }

            PortAttribute portAttribute = new PortAttribute(name, externalId, ofPort);
            portAttributes.add(portAttribute);
        }

        return portAttributes;
    }

    public static void setPortAttribute(String table, String port, String column, String value) throws IOException, InterruptedException {
        String args = table + port + column + "=" + value;
        execute(VSCTL_PROCESS, SET_COMMAND, args);
    }

    public static List<PortAttribute> getPortConfig(String table, List<String> ports, List<String> columns) throws IOException, InterruptedException {
        String options = StringUtils.join(columns.toArray(), ",");
        String args = table + StringUtils.join(ports.toArray(), " ");

        String output = execute(VSCTL_PROCESS, options, LIST_COMMAND, args);
        return null;
    }
}
