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
package com.futurewei.alcoragent.ovs.flow;

import java.util.List;
import java.util.Map;

public class OvsFlow {
    private int table;
    private int priority;
    private String inPort;
    private String dlVlan;
    private String action;

    private Map<String, String> matchFields;
    private List<String> actions;

    public OvsFlow(int table, int priority, String action) {
        this.table = table;
        this.priority = priority;
        this.action = action;
    }

    public OvsFlow(FLowTable fLowTable, int priority, FlowAction flowAction) {
        this.table = fLowTable.getTable();
        this.priority = priority;
        this.action = flowAction.getAction();
    }

    public OvsFlow(FLowTable fLowTable, int priority, String inPort, FlowAction flowAction) {
        this.table = fLowTable.getTable();
        this.priority = priority;
        this.inPort = inPort;
        this.action = flowAction.getAction();
    }

    public OvsFlow(FLowTable fLowTable, String dlVlan, FlowAction flowAction) {
        this.table = fLowTable.getTable();
        this.priority = priority;
        this.inPort = inPort;
        this.action = flowAction.getAction();
    }

    public OvsFlow(FLowTable fLowTable, Map<String, String> matchFields, List<String> actions) {
        this.table = fLowTable.getTable();
        this.matchFields = matchFields;
        this.actions = actions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\"").append("table=").append(table).append(",");
        if (matchFields != null && actions != null) {
            for (Map.Entry<String, String> entry: matchFields.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
            }

            sb.append("actions=");

            for (int i = 0; i < actions.size() - 1; i++) {
                sb.append(actions.get(i)).append(",");
            }

            sb.append(actions.get(actions.size() - 1));
        } else {
            if (inPort != null) {
                sb.append("in_port=").append(inPort).append(",");
            }

            sb.append("priority=").append(priority).append(",")
                    .append("action=").append(action);
        }

        sb.append("\"");

        return sb.toString();
    }
}
