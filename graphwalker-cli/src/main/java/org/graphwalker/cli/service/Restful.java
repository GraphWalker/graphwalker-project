package org.graphwalker.cli.service;

/*
 * #%L
 * GraphWalker Command Line Interface
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.graphwalker.cli.CLI;
import org.graphwalker.cli.Util;
import org.graphwalker.core.machine.FailFastStrategy;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Action;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Iterator;

/**
 * Created by krikar on 5/30/14.
 */
@Path("graphwalker")
public class Restful {
    CLI cli;
    private SimpleMachine machine;

    public Restful(CLI cli, Iterator itr) throws Exception {
        this.cli = cli;
        this.machine = new SimpleMachine(cli.getContextsWithPathGenerators(itr));
    }

    @GET
    @Produces("text/plain")
    @Path("hasNext")
    public String hasNext() {
        if (machine.hasNextStep()) {
            if (cli.getOnline().json) {
                JSONObject obj = new JSONObject();
                return obj.put("HasNext", "true").toString();
            } else {
                return "true";
            }
        } else {
            if (cli.getOnline().json) {
                JSONObject obj = new JSONObject();
                return obj.put("HasNext", "false").toString();
            } else {
                return "false";
            }
        }
    }

    @GET
    @Produces("text/plain")
    @Path("getNext")
    public String getNext() {
        try {
            machine.getNextStep();
            if (cli.getOnline().json) {
                return Util.getStepAsJSON(machine, cli.getOnline().verbose, cli.getOnline().unvisited).toString();
            } else {
                return Util.getStepAsString(machine, cli.getOnline().verbose, cli.getOnline().unvisited);
            }
        } catch (MachineException e) {
            throw e;
        }
    }

    @GET
    @Produces("text/plain")
    @Path("getData")
    public String getData(@QueryParam("key") String key) {
        String value = machine.getCurrentContext().getKeys().get(key);
        if (cli.getOnline().json) {
            JSONObject obj = new JSONObject();
            return obj.put("Value", value).toString();
        } else {
            return value;
        }
    }

    @GET
    @Produces("text/plain")
    @Path("setData")
    public String setData(@QueryParam("script") String script) {
        try {
            machine.getCurrentContext().execute(new Action(script));
            if (cli.getOnline().json) {
                JSONObject obj = new JSONObject();
                return obj.put("Result", "Ok").toString();
            } else {
                return "Ok";
            }
        } catch (Exception e) {
            if (cli.getOnline().json) {
                JSONObject obj = new JSONObject();
                return obj.put("Result", e.getMessage()).toString();
            } else {
                return e.getMessage();
            }
        }
    }

    @GET
    @Produces("text/plain")
    @Path("restart")
    public String restart() throws Exception {
        machine = new SimpleMachine(cli.getContextsWithPathGenerators(cli.getOnline().model.iterator()));
        if (cli.getOnline().json) {
            JSONObject obj = new JSONObject();
            return obj.put("Restart", "Ok").toString();
        } else {
            return "Ok";
        }
    }

    @GET
    @Produces("text/plain")
    @Path("fail")
    public String fail() {
        try {
            FailFastStrategy failFastStrategy = new FailFastStrategy();
            failFastStrategy.handle(machine, new MachineException(machine.getCurrentContext(), new Throwable()));
        } catch (Throwable e) {
            ;
        }
        if (cli.getOnline().json) {
            JSONObject obj = new JSONObject();
            return obj.put("Fail", "Ok").toString();
        } else {
            return "Ok";
        }
    }

    @GET
    @Produces("text/plain")
    @Path("getStatistics")
    public String getStatistics() {
        if (cli.getOnline().json) {
            return Util.getStatisticsAsJSON(machine).toString();
        } else {
            return Util.getStatisticsAsString(machine);
        }
    }
}