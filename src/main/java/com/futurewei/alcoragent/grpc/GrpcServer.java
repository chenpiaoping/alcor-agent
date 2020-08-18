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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class GrpcServer {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);

    @Value("${grpc.server.host:#{localhost}}")
    private String host;

    @Value("${grpc.server.port:#{8899}}")
    private int port;
    private Server grpcServer;
    private Thread grpcThread;

    @PostConstruct
    public void init() {
        grpcThread = new Thread(() -> {
            try {
                start();
                await();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                LOG.error("Start gRPC server error: ", e);
            }
        });

        grpcThread.start();
    }

    public void start() throws IOException {
        this.grpcServer = ServerBuilder.forPort(port).addService(new GoalStateRpcImpl()).build().start();

        LOG.info("grpc server started!");

        Runtime.getRuntime().addShutdownHook(new Thread(GrpcServer.this::stop));
    }

    public void stop() {
        if(this.grpcServer != null) {
            this.grpcServer.shutdown();
            LOG.info("grpc server stopped!");
        }
    }

    public void await() throws InterruptedException {
        if(this.grpcServer != null) {
            this.grpcServer.awaitTermination();
        }
    }
}
