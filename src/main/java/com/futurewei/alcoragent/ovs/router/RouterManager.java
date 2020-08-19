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

import com.futurewei.alcoragent.ovs.exception.RouterExists;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RouterManager {

    private Map<String, Router> routers;

    public RouterManager() {
        this.routers = new HashMap<>();
    }

    public Router createRouter(String routerId) throws RouterExists, IOException, InterruptedException {
        if (routers.containsKey(routerId)) {
            throw new RouterExists();
        }

        Router router = new Router(routerId);
        routers.put(routerId, router);

        return router;
    }

    public Router getRouter(String routerId) {
        return routers.get(routerId);
    }
}
