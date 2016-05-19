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
import org.graphwalker.io.factory.gw3.GW3ContextFactory;
import org.graphwalker.java.test.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS (Java API for RESTful Services (JAX-RS)) service implementation.
 * Created by krikar on 5/30/14.
 */
@Path("graphwalker")
public class Restful {
  private static final Logger logger = LoggerFactory.getLogger(Restful.class);
  private List<Context> contexts;
  private Machine machine;
  private Boolean verbose;
  private Boolean unvisited;

  public Restful(List<Context> contexts, Boolean verbose, Boolean unvisited) throws Exception {
    this.verbose = verbose;
    this.unvisited = unvisited;

    if (contexts == null || contexts.isEmpty()) {
      return;
    }
    setContexts(contexts);
  }

  public void setContexts(List<Context> contexts) {
    this.contexts = contexts;
    machine = new SimpleMachine(this.contexts);
  }

  @POST
  @Consumes("text/plain;charset=UTF-8")
  @Produces("text/plain;charset=UTF-8")
  @Path("load")
  public String load(String jsonGW3) {
    logger.debug("Received load with gw3: " + jsonGW3);
    JSONObject obj = new JSONObject();
    try {
      List<Context> contexts = new GW3ContextFactory().createMultiple(jsonGW3);
      setContexts(contexts);
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  /**
   * Returns the next step (element) to be executed on the model. An element can
   * be either a vertex or an edge,
   *
   * @return The label of the next step as a plain or a JSON formatted string. If the label is empty or
   * non-existent, the label of the step is an empty string.
   */
  @GET
  @Produces("text/plain;charset=UTF-8")
  @Path("hasNext")
  public String hasNext() {
    logger.debug("Received hasNext");
    JSONObject obj = new JSONObject();
    try {
      if (machine.hasNextStep()) {
        obj.put("hasNext", "true").toString();
      } else {
        obj.put("hasNext", "false").toString();
      }
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  /**
   * Gets the next step (element) to be executed on the model. An element can
   * be either a vertex or an edge,
   *
   * @return The label of the next step as a plain or a JSON formatted string. If the label is empty or
   * non-existent, the label of the step is an empty string.
   */
  @GET
  @Produces("text/plain;charset=UTF-8")
  @Path("getNext")
  public String getNext() {
    logger.debug("Received getNext");
    JSONObject obj;
    try {
      machine.getNextStep();
      obj = Util.getStepAsJSON(machine, verbose, unvisited);
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj = new JSONObject();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  @GET
  @Produces("text/plain;charset=UTF-8")
  @Consumes("text/plain;charset=UTF-8")
  @Path("getData")
  public String getData() {
    logger.debug("Received getData");
    JSONObject obj = new JSONObject();
    try {
      JSONObject data = new JSONObject();
      for (Map.Entry<String, String> k : machine.getCurrentContext().getKeys().entrySet()) {
        data.put(k.getKey(), k.getValue());
      }
      obj.put("data", data);
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  @PUT
  @Consumes("text/plain;charset=UTF-8")
  @Produces("text/plain;charset=UTF-8")
  @Path("setData/{script}")
  public String setData(@PathParam("script") String script) {
    logger.debug("Received setData with script: " + script);
    JSONObject obj = new JSONObject();
    try {
      machine.getCurrentContext().execute(new Action(script));
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  @PUT
  @Path("restart")
  @Produces("text/plain;charset=UTF-8")
  public String restart() {
    logger.debug("Received restart");
    JSONObject obj = new JSONObject();
    try {
      machine = new SimpleMachine(contexts);
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  @PUT
  @Consumes("text/plain;charset=UTF-8")
  @Produces("text/plain;charset=UTF-8")
  @Path("fail/{reason}")
  public String fail(@PathParam("reason") String reason) {
    logger.debug("Received fail with reason: " + reason);
    JSONObject obj = new JSONObject();
    try {
      FailFastStrategy failFastStrategy = new FailFastStrategy();
      failFastStrategy.handle(machine, new MachineException(machine.getCurrentContext(), new Throwable(reason)));
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  @GET
  @Produces("text/plain;charset=UTF-8")
  @Path("getStatistics")
  public String getStatistics() {
    logger.debug("Received getStatistics");
    JSONObject obj = new JSONObject();
    try {
      Result result = new Result();
      result.updateResults(machine, null);
      obj = result.getResults();
      obj.put("result", "ok");
    } catch (Exception e) {
      e.printStackTrace();
      obj = new JSONObject();
      obj.put("result", "nok");
      obj.put("error", e.getMessage());
    }
    return obj.toString();
  }

  public List<Context> getContexts() {
    return contexts;
  }

  public Machine getMachine() {
    return machine;
  }

  public Boolean getVerbose() {
    return verbose;
  }

  public Boolean getUnvisited() {
    return unvisited;
  }
}
