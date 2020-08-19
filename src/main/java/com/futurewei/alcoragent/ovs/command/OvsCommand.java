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
package com.futurewei.alcoragent.ovs.command;

import com.futurewei.alcoragent.util.CommandUtil;

import java.io.IOException;

public class OvsCommand {
    private String command;

    public static String execute(String command) throws IOException, InterruptedException {
        return CommandUtil.execute(command);
    }

    public static String execute(String command, String args) throws IOException, InterruptedException {
        return CommandUtil.execute(command + " " + args);
    }

    public static String execute(String process, String command, String args) throws IOException, InterruptedException {
        return CommandUtil.execute(process + " " + command + " " + args);
    }

    public static String execute(String process, String options, String command, String args) throws IOException, InterruptedException {
        return CommandUtil.execute(process + " " + options + " " +command + " " + args);
    }
}
