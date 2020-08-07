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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.graalvm.polyglot.Value;
import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.statistics.Execution;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.java.test.Result;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.graphwalker.core.common.Objects.isNull;

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
  private Boolean blocked;

  public Restful(List<Context> contexts, Boolean verbose, Boolean unvisited, Boolean blocked) throws Exception {
    this.verbose = verbose;
    this.unvisited = unvisited;
    this.blocked = blocked;

    if (contexts == null || contexts.isEmpty()) {
      return;
    }
    setContexts(contexts);
  }

  public void setContexts(List<Context> contexts) {
    if (this.blocked) {
      org.graphwalker.io.common.Util.filterBlockedElements(contexts);
    }
    this.contexts = contexts;
    machine = new SimpleMachine(this.contexts);
  }

  @POST
  @Consumes("text/plain;charset=UTF-8")
  @Produces("text/plain;charset=UTF-8")
  @Path("load")
  public String load(String jsonGW3) {
    logger.debug("Received load with gw3: " + jsonGW3);
    JSONObject resultJson = new JSONObject();
    try {
      List<Context> contexts = new JsonContextFactory().create(jsonGW3);
      setContexts(contexts);
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
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
    JSONObject resultJson = new JSONObject();
    try {
      if (machine.hasNextStep()) {
        resultJson.put("hasNext", "true").toString();
      } else {
        resultJson.put("hasNext", "false").toString();
      }
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
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
    JSONObject resultJson;
    try {
      machine.getNextStep();
      resultJson = Util.getStepAsJSON(machine, verbose, unvisited);
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson = new JSONObject();
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
  }

  @GET
  @Produces("text/plain;charset=UTF-8")
  @Consumes("text/plain;charset=UTF-8")
  @Path("getData")
  public String getData() {
    logger.debug("Received getData");
    JSONObject resultJson = new JSONObject();
    try {
      Value bindings = machine.getCurrentContext().getExecutionEnvironment().getBindings("js");
      JSONObject data = new JSONObject();
      for (String key : bindings.getMemberKeys()) {
        data.put(key, bindings.getMember(key));
      }
      resultJson.put("data", data);
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
  }

  @PUT
  @Consumes("text/plain;charset=UTF-8")
  @Produces("text/plain;charset=UTF-8")
  @Path("setData/{script}")
  public String setData(@PathParam("script") String script) {
    logger.debug("Received setData with script: " + script);
    JSONObject resultJson = new JSONObject();
    try {
      machine.getCurrentContext().execute(new Action(script));
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
  }

  @PUT
  @Path("restart")
  @Produces("text/plain;charset=UTF-8")
  public String restart() {
    logger.debug("Received restart");
    JSONObject resultJson = new JSONObject();
    try {
      if ( isNull(machine) ) {
        throw new RuntimeException("No model(s) are loaded.");
      }

      if ( machine.getProfiler().getExecutionPath().size() > 0 ) {
        Execution execution = machine.getProfiler().getExecutionPath().get(0);
        contexts = contexts.stream().peek(context -> {
          if (execution.getContext().equals(context)) {
            context.setNextElement(execution.getElement());
          }
          context.setExecutionStatus(ExecutionStatus.NOT_EXECUTED);
        }).collect(Collectors.toList());
        machine = new SimpleMachine(contexts);
      }
      resultJson.put("result", "ok");

    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
  }

  @PUT
  @Consumes("text/plain;charset=UTF-8")
  @Produces("text/plain;charset=UTF-8")
  @Path("fail/{reason}")
  public String fail(@PathParam("reason") String reason) {
    logger.debug("Received fail with reason: " + reason);
    JSONObject resultJson = new JSONObject();
    try {
      FailFastStrategy failFastStrategy = new FailFastStrategy();
      failFastStrategy.handle(machine, new MachineException(machine.getCurrentContext(), new Throwable(reason)));
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
  }

  @GET
  @Produces("text/plain;charset=UTF-8")
  @Path("getStatistics")
  public String getStatistics() {
    logger.debug("Received getStatistics");
    JSONObject resultJson = new JSONObject();
    try {
      Result result = new Result();
      result.updateResults(machine, null);
      resultJson = result.getResults();
      resultJson.put("result", "ok");
    } catch (Exception e) {
      resultJson.put("result", "nok");
      resultJson.put("error", e.getMessage());
    }
    return resultJson.toString();
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
