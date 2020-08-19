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

import com.futurewei.alcoragent.util.CommandUtil;

import java.io.IOException;

public class RouterNamespace {
    private static final String ROUTER_NAMESPACE_PREFIX = "qrouter-";
    private String name;

    public RouterNamespace(String routerId) {
        this.name = ROUTER_NAMESPACE_PREFIX + routerId;
    }

    public void create(boolean ipv6Forwarding) throws IOException, InterruptedException {
        CommandUtil.execute("ip netnas add " + name);
        CommandUtil.execute("sysctl -w net.ipv4.ip_forward=1");
        CommandUtil.execute("sysctl -w net.ipv4.conf.all.arp_ignore=1");
        CommandUtil.execute("sysctl -w net.ipv4.conf.all.arp_announce=2");
        CommandUtil.execute("sysctl -w net.ipv6.conf.all.forwarding=1");
        CommandUtil.execute("sysctl -w net.ipv4.ip_nonlocal_bind=0");
        CommandUtil.execute("sysctl -w net.netfilter.nf_conntrack_tcp_loose=0");
    }

    public String getName() {
        return name;
    }
}
