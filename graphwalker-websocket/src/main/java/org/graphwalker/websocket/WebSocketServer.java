package org.graphwalker.websocket;

/*
 * #%L
 * GraphWalker As A Service
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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Value;
import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Element;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.io.factory.yed.YEdContextFactory;
import org.graphwalker.modelchecker.ContextsChecker;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WebSocketServer with an API for working with GraphWalker as a service.
 */

public class WebSocketServer extends org.java_websocket.server.WebSocketServer implements Observer {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

  private Set<WebSocket> sockets;
  private Map<WebSocket, Machine> machines;
  private Machine machine = null;
  private MODE mode;

  enum MODE {
    EDITOR,
    PLAYBACK
  }

  ;

  public WebSocketServer(int port) {
    super(new InetSocketAddress(port));
    sockets = new HashSet<>();
    machines = new HashMap<>();
    mode = MODE.EDITOR;
  }

  public WebSocketServer(InetSocketAddress address) {
    super(address);
    sockets = new HashSet<>();
    machines = new HashMap<>();
    mode = MODE.EDITOR;
  }

  /**
   * This is used for connecting to an execution, where a user can real time
   * information of the run.
   */
  public WebSocketServer(int port, Machine machine) {
    super(new InetSocketAddress(port));
    sockets = new HashSet<>();
    machines = new HashMap<>();
    this.machine = machine;
    this.machine.addObserver(this);
    sockets = new HashSet<>();
    mode = MODE.PLAYBACK;
  }

  @Override
  public void onOpen(WebSocket socket, ClientHandshake handshake) {
    sockets.add(socket);
    machines.put(socket, machine);
    logger.info(socket.getRemoteSocketAddress().getAddress().getHostAddress() + " is now connected");
  }

  @Override
  public void onClose(WebSocket socket, int code, String reason, boolean remote) {
    sockets.remove(socket);
    machines.remove(socket);
    logger.info(socket.getRemoteSocketAddress().getAddress().getHostAddress() + " has disconnected");
  }

  @Override
  public void onMessage(WebSocket socket, String message) {
    logger.debug("Received message from: "
                 + socket.getRemoteSocketAddress().getAddress().getHostAddress()
                 + " : "
                 + message);

    JSONObject response = new JSONObject();
    JSONObject root;
    try {
      root = new JSONObject(message);
    } catch (JSONException e) {
      logger.error(e.getMessage());
      response.put("message", "Unknown command: " + e.getMessage());
      response.put("success", false);
      socket.send(response.toString());
      return;
    }

    String command = root.getString("command").toUpperCase();
    switch (command) {
      case "MODE":
        response.put("command", "mode");
        switch (mode) {
          case EDITOR:
            response.put("mode", "EDITOR");
            break;
          case PLAYBACK:
            response.put("mode", "PLAYBACK");
            break;
        }
        response.put("version", getVersionString());
        response.put("success", true);

        break;
      case "START":
        response.put("command", "start");
        response.put("success", false);
        List<Context> contexts = null;
        try {
          try {
            contexts = new JsonContextFactory().create(root.getJSONObject("gw").toString());
          } catch (RuntimeException e) {
            contexts = new JsonContextFactory().create(root.getJSONObject("gw3").toString());
          }
          Machine machine = new SimpleMachine(contexts);
          machine.addObserver(this);
          machines.put(socket, machine);
          response.put("success", true);
        } catch (Exception e) {
          logger.error(e.getMessage());
          sendIssue(socket, e.getMessage());
        }

        break;
      case "GETNEXT": {
        response.put("command", "getNext");
        response.put("success", false);
        Machine machine = machines.get(socket);
        if (machine != null) {
          try {
            machine.getNextStep();
            response.put("modelId", machine.getCurrentContext().getModel().getId());
            response.put("elementId", machine.getCurrentContext().getCurrentElement().getId());
            response.put("name", machine.getCurrentContext().getCurrentElement().getName());
            response.put("modelName", machine.getCurrentContext().getModel().getName());
            response.put("success", true);
          } catch (Exception e) {
            logger.error(e.getMessage());
            sendIssue(socket, e.getMessage());
          }
        } else {
          response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
        }

        break;
      }
      case "HASNEXT": {
        response.put("command", "hasNext");
        response.put("success", false);
        Machine machine = machines.get(socket);
        try {
          if (machine == null) {
            response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
          } else if (machine.hasNextStep()) {
            response.put("success", true);
            response.put("hasNext", true);
          } else {
            response.put("success", true);
            response.put("hasNext", false);
          }
        } catch (Exception e) {
          logger.error(e.getMessage());
          sendIssue(socket, e.getMessage());
        }

        break;
      }
      case "GETDATA": {
        response.put("command", "getData");
        response.put("success", false);
        Machine machine = machines.get(socket);
        if (machine != null) {
          JSONObject obj = new JSONObject();
          try {
            Value bindings = machine.getCurrentContext().getExecutionEnvironment().getBindings("js");
            JSONObject data = new JSONObject();
            for (String key : bindings. getMemberKeys() ) {
              data.put(key, bindings.getMember(key));
            }
            obj.put("modelId", machine.getCurrentContext().getModel().getId());
            obj.put("data", data);
            obj.put("result", "ok");
            response.put("data", data);
            response.put("success", true);
          } catch (Exception e) {
            logger.error(e.getMessage());
            sendIssue(socket, e.getMessage());
          }
        } else {
          response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
        }
        break;
      }
      case "SETDATA": {
        response.put("command", "setData");
        response.put("success", false);
        Machine machine = machines.get(socket);
        if (machine != null) {
          JSONObject obj = new JSONObject();
          try {
            machine.getCurrentContext().execute(new Action(root.getString("action")));
            obj.put("result", "ok");
            response.put("success", true);
          } catch (Exception e) {
            logger.error(e.getMessage());
            sendIssue(socket, e.getMessage());
          }
        } else {
          response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
        }
        break;
      }
      case "GETMODEL": {
        response.put("command", "getmodel");
        response.put("success", false);
        Machine machine = machines.get(socket);
        if (machine != null) {
          try {
            response.put("models", new JsonContextFactory().getAsString(machine.getContexts()));
            response.put("success", true);
          } catch (Exception e) {
            logger.error(e.getMessage());
            sendIssue(socket, e.getMessage());
          }
        } else {
          response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
        }
        break;
      }
      case "UPDATEALLELEMENTS": {
        response.put("command", "updateallelements");
        response.put("success", false);
        Machine machine = machines.get(socket);
        if (machine != null) {
          try {
            JSONArray jsonElements = new JSONArray();
            for (Context context : machine.getContexts()) {
              for (Element element : context.getModel().getElements()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("modelId", context.getModel().getId());
                jsonObject.put("elementId", element.getId());
                jsonObject.put("visitedCount", machine.getProfiler().getVisitCount(context, element));
                jsonElements.put(jsonObject);
              }
            }
            response.put("elements", jsonElements);
            response.put("success", true);
          } catch (Exception e) {
            logger.error(e.getMessage());
            sendIssue(socket, e.getMessage());
          }
        } else {
          response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
        }
        break;
      }
      case "CONVERTGRAPHML": {
        response.put("command", "convertGraphml");
        response.put("success", false);
        try {
          List<Context> yedContexts = new YEdContextFactory().create(root.getString("graphml"));
          response.put("models", new JsonContextFactory().getAsString(yedContexts));
          response.put("success", true);
        } catch (Exception e) {
          logger.error(e.getMessage());
          sendIssue(socket, e.getMessage());
        }
        break;
      }
      default:
        response.put("message", "Unknown command");
        response.put("success", false);
        break;
    }
    logger.debug("Sending response to: "
                 + socket.getRemoteSocketAddress().getAddress().getHostAddress()
                 + " : "
                 + response.toString());
    socket.send(response.toString());
  }

  private List<String> checkContexts(WebSocket socket, List<Context> contexts) {
    if (contexts == null) {
      return new ArrayList<>();
    }
    return ContextsChecker.hasIssues(contexts);
  }

  private void sendIssue(WebSocket socket, String issue) {
    List<String> issues = new ArrayList<>();
    issues.add(issue);
    sendIssues(socket, issues);
  }

  private void sendIssues(WebSocket socket, List<String> issues) {
    if (!issues.isEmpty()) {
      JSONObject jsonIssue = new JSONObject();
      jsonIssue.put("command", "issues");

      JSONArray jsonIssues = new JSONArray();
      for (String issue : issues) {
        jsonIssues.put(issue);
      }
      jsonIssue.put("issues", jsonIssues);
      logger.debug("Sending response to: "
                   + socket.getRemoteSocketAddress().getAddress().getHostAddress()
                   + " : "
                   + jsonIssue.toString());
      socket.send(jsonIssue.toString());
    } else {
      JSONObject jsonIssue = new JSONObject();
      jsonIssue.put("command", "noIssues");
      logger.debug("Sending response to: "
                   + socket.getRemoteSocketAddress().getAddress().getHostAddress()
                   + " : "
                   + jsonIssue.toString());
      socket.send(jsonIssue.toString());
    }
  }

  @Override
  public void onError(WebSocket socket, Exception ex) {
    ex.printStackTrace();
  }

  @Override
  public void onStart() {
  }

  @Override
  public void update(Machine machine, Element element, EventType type) {
    logger.info("Received an update from a GraphWalker machine");
    for (Object o : machines.entrySet()) {
      Map.Entry pairs = (Map.Entry) o;
      if (machine == pairs.getValue()) {
        logger.info("Event: " + type);
        WebSocket conn = (WebSocket) pairs.getKey();
        if (type == EventType.AFTER_ELEMENT) {
          JSONObject jsonObject = new JSONObject();
          jsonObject.put("command", "visitedElement");
          jsonObject.put("modelId", machine.getCurrentContext().getModel().getId());
          jsonObject.put("elementId", element.getId());
          jsonObject.put("visitedCount", machine.getProfiler().getVisitCount(machine.getCurrentContext(), element));
          jsonObject.put("totalCount", machine.getProfiler().getTotalVisitCount());
          jsonObject.put("stopConditionFulfillment", machine.getCurrentContext().getPathGenerator().getStopCondition().getFulfilment());

          Value bindings = machine.getCurrentContext().getExecutionEnvironment().getBindings("js");
          JSONObject data = new JSONObject();
          for (String key : bindings.getMemberKeys()) {
            data.put(key, bindings.getMember(key));
          }
          jsonObject.put("data", data);

          conn.send(jsonObject.toString());
        }
      }
    }
  }

  public void startService() {
    start();

    logger.info("GraphWalkerServer started on port: " + getPort());

    // Shutdown event
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println();
      System.out.println("GraphWalkerServer shutting down");
      System.out.println();
      logger.info("GraphWalkerServer shutting down");
    }));

    while (true) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException i) {
        break;
      }
    }
  }

  public Set<WebSocket> getSockets() {
    return sockets;
  }

  public Map<WebSocket, Machine> getMachines() {
    return machines;
  }

  private String getVersionString() {
    Properties properties = new Properties();
    InputStream inputStream = getClass().getResourceAsStream("/version.properties");
    if (null != inputStream) {
      try {
        properties.load(inputStream);
      } catch (IOException e) {
        logger.error("An error occurred when trying to get the version string", e);
        return "unknown";
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    }
    return properties.getProperty("graphwalker.version");
  }
}
