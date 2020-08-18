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
package com.futurewei.alcoragent.ovs.tunnel;

public class TunnelOptions {
    private int DstPort;
    private boolean fragment;
    private String localIp;
    private String remoteIp;
    private String inKey;
    private String outKey;
    private boolean checksum;

    public TunnelOptions() {

    }

    public TunnelOptions(int dstPort, boolean fragment, String localIp, String remoteIp, String inKey, String outKey, boolean checksum) {
        DstPort = dstPort;
        this.fragment = fragment;
        this.localIp = localIp;
        this.remoteIp = remoteIp;
        this.inKey = inKey;
        this.outKey = outKey;
        this.checksum = checksum;
    }

    public int getDstPort() {
        return DstPort;
    }

    public void setDstPort(int dstPort) {
        DstPort = dstPort;
    }

    public boolean isFragment() {
        return fragment;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getInKey() {
        return inKey;
    }

    public void setInKey(String inKey) {
        this.inKey = inKey;
    }

    public String getOutKey() {
        return outKey;
    }

    public void setOutKey(String outKey) {
        this.outKey = outKey;
    }

    public boolean isChecksum() {
        return checksum;
    }

    public void setChecksum(boolean checksum) {
        this.checksum = checksum;
    }
}
