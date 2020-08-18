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
package com.futurewei.alcoragent.grpc;

import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateRequest;
import com.futurewei.alcor.schema.Goalstateprovisioner.GoalStateOperationReply;
import com.futurewei.alcoragent.agent.AlcorAgent;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoalStateRpcImpl extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(GoalStateRpcImpl.class);

    @Override
    public void pushNetworkResourceStates(GoalState request, StreamObserver<GoalStateOperationReply> responseObserver) {
        LOG.info("pushNetworkResourceStates request: {}", request);

        try {
            AlcorAgent.instance().createGoalState(request);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("create goal state error: ", e);
        }

        responseObserver.onNext(GoalStateOperationReply.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveNetworkResourceStates(GoalStateRequest request, StreamObserver<GoalState> responseObserver) {
        LOG.info("retrieveNetworkResourceStates request: {}", request);

        responseObserver.onNext(GoalState.newBuilder().build());
        responseObserver.onCompleted();
    }
}
