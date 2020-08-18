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
package com.futurewei.alcoragent.agent;

import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.schema.Neighbor.NeighborState;
import com.futurewei.alcor.schema.Port.PortState;
import com.futurewei.alcor.schema.Subnet.SubnetState;
import com.futurewei.alcor.schema.Vpc.VpcState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AlcorAgent {
    private static final Logger LOG = LoggerFactory.getLogger(AlcorAgent.class);
    private static AlcorAgent alcorAgent;

    @Autowired
    private Agent agent;

    @PostConstruct
    public void init() {
        alcorAgent = this;
    }

    public static AlcorAgent instance() {
        return alcorAgent;
    }

    public void createGoalState(GoalState goalState) throws Exception {
        int portStatesCount = goalState.getPortStatesCount();
        if (portStatesCount > 0) {
            for (int i = 0; i < portStatesCount; i++) {
                createPortState(goalState.getPortStates(i), goalState);
            }
        }

        int neighborStatesCount = goalState.getNeighborStatesCount();
        if (neighborStatesCount > 0) {
            for (int i = 0; i < neighborStatesCount; i++) {
                createPortState(goalState.getPortStates(i), goalState);
            }
        }
    }

    public void createVpcState(VpcState vpcState) {

    }

    public static void createSubnetState(SubnetState subnetState) {

    }

    public void createPortState(PortState portState, GoalState goalState) throws Exception {
        agent.createPortState(portState, goalState);
    }

    public void createNeighborState(NeighborState neighborState, GoalState goalState) throws Exception {
        agent.createNeighborState(neighborState, goalState);
    }
}
