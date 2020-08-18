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
package com.futurewei.alcoragent.ovs;

public class PortAttribute {
    private String name;
    private ExternalId externalId;
    private int ofPort;

    public static class ExternalId {
        private String attachedMac;
        private String IfaceId;
        private String IfaceStatus;

        public ExternalId(String attachedMac, String ifaceId, String ifaceStatus) {
            this.attachedMac = attachedMac;
            IfaceId = ifaceId;
            IfaceStatus = ifaceStatus;
        }

        public String getAttachedMac() {
            return attachedMac;
        }

        public void setAttachedMac(String attachedMac) {
            this.attachedMac = attachedMac;
        }

        public String getIfaceId() {
            return IfaceId;
        }

        public void setIfaceId(String ifaceId) {
            IfaceId = ifaceId;
        }

        public String getIfaceStatus() {
            return IfaceStatus;
        }

        public void setIfaceStatus(String ifaceStatus) {
            IfaceStatus = ifaceStatus;
        }
    }

    public PortAttribute(String name, ExternalId externalId, int ofPort) {
        this.name = name;
        this.externalId = externalId;
        this.ofPort = ofPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExternalId getExternalId() {
        return externalId;
    }

    public void setExternalId(ExternalId externalId) {
        this.externalId = externalId;
    }

    public int getOfPort() {
        return ofPort;
    }

    public void setOfPort(int ofPort) {
        this.ofPort = ofPort;
    }
}
