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

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OfctlCommand extends OvsCommand {
    private static final String OFCTL_PROCESS = "ovs-ofctl";
    private static final String ADD_FLOWS_COMMAND = "add-flows";
    private static final String DELETE_FLOWS_COMMAND = "del-flows";
    private static final String MODIFY_FLOWS_COMMAND = "mod-flows";

    public static void deleteFlows(String bridgeName) throws IOException, InterruptedException {
        execute(OFCTL_PROCESS, DELETE_FLOWS_COMMAND, bridgeName);
    }

    public static void addFlows(String bridgeName, String flow) throws IOException, InterruptedException {
        execute(OFCTL_PROCESS, ADD_FLOWS_COMMAND, bridgeName + " " + flow);
    }

    public static void modifyFlows(String bridgeName, String flow) throws IOException, InterruptedException {
        execute(OFCTL_PROCESS, MODIFY_FLOWS_COMMAND, bridgeName + " " + flow);
    }
}
