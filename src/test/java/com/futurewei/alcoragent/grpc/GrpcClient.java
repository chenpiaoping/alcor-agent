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

import com.futurewei.alcor.schema.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GrpcClient {
    private ManagedChannel managedChannel;

    @BeforeEach
    public void before() {
        managedChannel = ManagedChannelBuilder.forAddress("127.0.0.1",8899)
                .usePlaintext().build();
    }

    private Vpc.VpcState buildVpcState() {
        Vpc.VpcConfiguration.Builder vpcConfigBuilder = Vpc.VpcConfiguration.newBuilder();
        vpcConfigBuilder.setName("vpc1");
        vpcConfigBuilder.setCidr("10.10.10.0/24");
        vpcConfigBuilder.setId("vpc_id1");

        Vpc.VpcState.Builder vpcStateBuilder = Vpc.VpcState.newBuilder();
        vpcStateBuilder.setOperationType(Common.OperationType.CREATE);
        vpcStateBuilder.setConfiguration(vpcConfigBuilder.build());

        return vpcStateBuilder.build();
    }

    private Subnet.SubnetState buildSubnetState() {
        Subnet.SubnetConfiguration.Builder subnetConfigBuilder = Subnet.SubnetConfiguration.newBuilder();
        subnetConfigBuilder.setName("subnet1");
        subnetConfigBuilder.setCidr("10.10.10.0/24");
        subnetConfigBuilder.setId("subnet_id1");

        Subnet.SubnetState.Builder subnetStateBuilder = Subnet.SubnetState.newBuilder();
        subnetStateBuilder.setOperationType(Common.OperationType.CREATE);
        subnetStateBuilder.setConfiguration(subnetConfigBuilder.build());

        return subnetStateBuilder.build();
    }

    private Port.PortState buildPortState() {
        Port.PortConfiguration.Builder portConfigBuilder = Port.PortConfiguration.newBuilder();
        portConfigBuilder.setName("port1");
        portConfigBuilder.setVpcId("vpc_id1");
        portConfigBuilder.setId("port_id1");
        portConfigBuilder.setMacAddress("7E-04-D0-C9-12-6C");
        portConfigBuilder.addFixedIps(Port.PortConfiguration.FixedIp.newBuilder()
                .setIpAddress("10.10.10.2").setSubnetId("subnet_id1").build());

        Port.PortState.Builder portStateBuilder = Port.PortState.newBuilder();
        portStateBuilder.setOperationType(Common.OperationType.CREATE);
        portStateBuilder.setConfiguration(portConfigBuilder.build());

        return portStateBuilder.build();
    }

    @Test
    public void pushNetworkResourceStatesTest() {
        GoalStateProvisionerGrpc.GoalStateProvisionerBlockingStub blockingStub =
                GoalStateProvisionerGrpc.newBlockingStub(managedChannel);

        Goalstate.GoalState.Builder builder = Goalstate.GoalState.newBuilder();
        builder.setFormatVersion(1);
        builder.addVpcStates(buildVpcState());
        builder.addSubnetStates(buildSubnetState());
        builder.addPortStates(buildPortState());
        builder.addSecurityGroupStates(SecurityGroup.SecurityGroupState.newBuilder().build());

        Goalstateprovisioner.GoalStateOperationReply goalStateOperationReply
                = blockingStub.pushNetworkResourceStates(builder.build());

        System.out.println(goalStateOperationReply.toString());

        managedChannel.shutdown();
    }
}
