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
import com.futurewei.alcor.schema.Router.RouterState;
import com.futurewei.alcor.schema.Subnet.SubnetState;
import com.futurewei.alcor.schema.Vpc.VpcState;

public interface Agent {
    void createGoalState(GoalState goalState) throws Exception;
    void createVpcState(VpcState vpcState) throws Exception;
    void createSubnetState(SubnetState subnetState) throws Exception;
    void createPortState(PortState portState, GoalState goalState) throws Exception;
    void createNeighborState(NeighborState neighborState, GoalState goalState) throws Exception;
    void createRouterState(RouterState routerState, GoalState goalState) throws Exception;
}
