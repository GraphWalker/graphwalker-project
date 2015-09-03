package org.graphwalker.restful;

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

import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.Action;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * JAX-RS (Java API for RESTful Services (JAX-RS)) service implementation.
 * Created by krikar on 5/30/14.
 */
@Path("graphwalker")
public class Restful {

    private List<Context> contexts;
    private Machine machine;
    private Boolean json;
    private Boolean verbose;
    private Boolean unvisited;

    public Restful(List<Context> contexts, Boolean json, Boolean verbose, Boolean unvisited) throws Exception {
        this.contexts = contexts;
        this.machine = new SimpleMachine(this.contexts);
        this.json = json;
        this.verbose = verbose;
        this.unvisited = unvisited;
    }

    /**
     * Returns the next step (element) to be executed on the model. An element can
     * be either a vertex or an edge,
     * @return The label of the next step as a plain or a JSON formatted string. If the label is empty or
     * non-existent, the label of the step is an empty string.
     */
    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("hasNext")
    public String hasNext() {
        if (machine.hasNextStep()) {
            if (json) {
                JSONObject obj = new JSONObject();
                return obj.put("HasNext", "true").toString();
            } else {
                return "true";
            }
        } else {
            if (json) {
                JSONObject obj = new JSONObject();
                return obj.put("HasNext", "false").toString();
            } else {
                return "false";
            }
        }
    }

    /**
     * Gets the next step (element) to be executed on the model. An element can
     * be either a vertex or an edge,
     * @return The label of the next step as a plain or a JSON formatted string. If the label is empty or
     * non-existent, the label of the step is an empty string.
     */
    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("getNext")
    public String getNext() {
        machine.getNextStep();
        if (json) {
            return Util.getStepAsJSON(machine, verbose, unvisited).toString();
        } else {
            return Util.getStepAsString(machine, verbose, unvisited);
        }
    }

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("getData")
    public String getData(@QueryParam("key") String key) {
        String value = machine.getCurrentContext().getKeys().get(key);
        if (json) {
            JSONObject obj = new JSONObject();
            return obj.put("Value", value).toString();
        } else {
            return value;
        }
    }

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("setData")
    public String setData(@QueryParam("script") String script) {
        try {
            machine.getCurrentContext().execute(new Action(script));
            if (json) {
                JSONObject obj = new JSONObject();
                return obj.put("Result", "Ok").toString();
            } else {
                return "Ok";
            }
        } catch (Exception e) {
            if (json) {
                JSONObject obj = new JSONObject();
                return obj.put("Result", e.getMessage()).toString();
            } else {
                return e.getMessage();
            }
        }
    }

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("restart")
    public String restart() throws Exception {
        machine = new SimpleMachine(contexts);
        if (json) {
            JSONObject obj = new JSONObject();
            return obj.put("Restart", "Ok").toString();
        } else {
            return "Ok";
        }
    }

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("fail")
    public String fail(@QueryParam("reason") String reason) {
        try {
            FailFastStrategy failFastStrategy = new FailFastStrategy();
            failFastStrategy.handle(machine, new MachineException(machine.getCurrentContext(), new Throwable(reason)));
        } catch (Throwable e) {
            // ignore
        }
        if (json) {
            JSONObject obj = new JSONObject();
            return obj.put("Fail", "Ok").toString();
        } else {
            return "Ok";
        }
    }

    @GET
    @Produces("text/plain;charset=UTF-8")
    @Path("getStatistics")
    public String getStatistics() {
        if (json) {
            return Util.getStatisticsAsJSON(machine).toString();
        } else {
            return Util.getStatisticsAsString(machine);
        }
    }
}