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

import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.graphwalker.io.factory.gw3.GW3ContextFactory;
import org.graphwalker.modelchecker.ContextChecker;
import org.graphwalker.modelchecker.ContextsChecker;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * A WebSocketServer with an API for working with GraphWalker as a service.
 * <p>
 * The websocket API has the following methods:
 * <ul>
 * <li> <strong><code>loadModel</code></strong><br>
 * Loads a model into the service. The model must use JSON notation for a GraphWalker model.
 * The notation has the following format:<br>
 * <code><pre>
 * {
 * "command":"loadModel",
 * "model":{
 * "name":"Small model",
 * "id":"m1",
 * "generator":"random(edge_coverage(100))",
 * "startElementId":"e0",
 * "vertices":[
 * {
 * "name":"v_VerifySomeAction",
 * "id":"n0",
 * "requirements":[
 * "UC01 2.2.1"
 * ]
 * },
 * {
 * "name":"v_VerifySomeOtherAction",
 * "id":"n1"
 * }
 * ],
 * "edges":[
 * {
 * "name":"e_FirstAction",
 * "id":"e0",
 * "actions":[
 * "index = 0;",
 * "str = '';"
 * ],
 * "targetVertexId":"n0"
 * },
 * {
 * "name":"e_AnotherAction",
 * "id":"e1",
 * "guard":"index <= 3",
 * "sourceVertexId":"n0",
 * "targetVertexId":"n1"
 * },
 * {
 * "name":"e_SomeOtherAction",
 * "id":"e2",
 * "actions":[
 * "index++;"
 * ],
 * "sourceVertexId":"n1",
 * "targetVertexId":"n1"
 * },
 * {
 * "id":"e3",
 * "sourceVertexId":"n1",
 * "targetVertexId":"n0"
 * }
 * ]
 * }
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>getModel</code></strong><br>
 * Will return the model with the given id <code><strong>modelId</code></strong> from the service.<br>
 * <code><pre>
 * {
 * "command":"getModel",
 * "modelId":"someId"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>start</code></strong><br>
 * Tells the service to get the machine ready to execute the model(s).
 * <code><pre>
 * {
 * "command":"start"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>getNext</code></strong><br>
 * Asks the service for the next element to be executed.
 * <code><pre>
 * {
 * "command":"getNext"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>hasNext</code></strong><br>
 * Asks the service if all conditions for all generators has been met or not.
 * <code><pre>
 * {
 * "command":"hasNext"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>restart</code></strong><br>
 * Requests the service to reset the execution of the the models to the initial state.
 * <code><pre>
 * {
 * "command":"restart"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>getData</code></strong><br>
 * Asks the service for the value of the given attribute.
 * <code><pre>
 * {
 * "command":"getData"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>addModel</code></strong><br>
 * Asks the service to create a new empty model with the given <strong><code>id</code></strong>.
 * <code><pre>
 * {
 * "command": "addModel",
 * "id": "someModelId"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>removeModel</code></strong><br>
 * Removes the model with the given modelId from the service.
 * <code><pre>
 * {
 * "command":"removeModel"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>addVertex</code></strong><br>
 * Adds a vertex to the model with the given <strong><code>modelId</code></strong> and
 * <strong><code>vertexId</code></strong> to the service.
 * <code><pre>
 * {
 * "command": "addVertex",
 * "modelId": "somModelId",
 * "vertexId": "someVertexId"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>addEdge</code></strong><br>
 * Adds an edge to the model with the given modelId to the service.
 * <code><pre>
 * {
 * "command":"addEdge"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>updateVertex</code></strong><br>
 * Updates attribute(s) to the vertex with given id and modelId from the service.
 * <code><pre>
 * {
 * "command":"updateVertex"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>updateEdge</code></strong><br>
 * Updates attribute(s) to the edge with given id and modelId from the service.
 * <code><pre>
 * {
 * "command":"updateEdge"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>removeVertex</code></strong><br>
 * Removes the vertex with the given id from and modelId from the service.
 * <code><pre>
 * {
 * "command":"removeVertex"
 * }
 *      </pre></code>
 * <p>
 * <li> <strong><code>removeEdge</code></strong><br>
 * Removes the edge with the given id from and modelId from the service.
 * <code><pre>
 * {
 * "command":"removeEdge"
 * }
 *      </pre></code>
 * <p>
 * </ul>
 */

public class WebSocketServer extends org.java_websocket.server.WebSocketServer implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private Set<WebSocket> sockets;
    private Map<WebSocket, Machine> machines;

    public WebSocketServer(int port) {
        super(new InetSocketAddress(port));
        sockets = new HashSet<>();
        machines = new HashMap<>();
    }

    public WebSocketServer(InetSocketAddress address) {
        super(address);
        sockets = new HashSet<>();
        machines = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        sockets.add(socket);
        machines.put(socket, null);
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
            response.put("message", "Unknown command: " + e.getMessage());
            response.put("success", false);
            socket.send(response.toString());
            return;
        }

        String command = root.getString("command").toUpperCase();
        switch (command) {
            case "START":
                response.put("command", "start");
                response.put("success", false);
                List<Context> gw3Contexts = null;
                try {
                    gw3Contexts = new GW3ContextFactory().createMultiple(root.getJSONObject("gw3").toString());
                    Machine machine = new SimpleMachine(gw3Contexts);
                    machine.addObserver(this);
                    machines.put(socket, machine);
                    response.put("success", true);
                } catch (Exception e) {
                    List<String> issues = checkContexts(socket, gw3Contexts);
                    issues.add(e.getMessage());
                    sendIssues(socket, issues);
                }

                break;
            case "GETNEXT": {
                response.put("command", "getNext");
                response.put("success", false);
                Machine machine = machines.get(socket);
                if (machine != null) {
                    try {
                        machine.getNextStep();
                        response.put("id", machine.getCurrentContext().getCurrentElement().getId());
                        response.put("name", machine.getCurrentContext().getCurrentElement().getName());
                        response.put("success", true);
                    } catch (Exception e) {
                        List<String> issues = checkContexts(socket, machine.getContexts());
                        issues.add(e.getMessage());
                        sendIssues(socket, issues);
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
                    List<String> issues = checkContexts(socket, machine.getContexts());
                    issues.add(e.getMessage());
                    sendIssues(socket, issues);
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
                        JSONObject data = new JSONObject();
                        for (Map.Entry<String, String> k : machine.getCurrentContext().getKeys().entrySet()) {
                            data.put(k.getKey(), k.getValue());
                        }
                        obj.put("data", data);
                        obj.put("result", "ok");
                        response.put("data", data);
                        response.put("success", true);
                    } catch (Exception e) {
                        List<String> issues = checkContexts(socket, machine.getContexts());
                        issues.add(e.getMessage());
                        sendIssues(socket, issues);
                    }
                } else {
                    response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
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
        if (contexts==null) {
            return new ArrayList<>();
        }
        return ContextsChecker.hasIssues(contexts);
    }

    private void sendIssues(WebSocket socket, List<String> issues) {
        if (issues.size() > 0) {
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
    public void update(Machine machine, Element element, EventType type) {
        logger.info("Received an update from a GraphWalker machine");
        Iterator it = machines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            if (machine == pairs.getValue()) {
                logger.info("Event: " + type);
                WebSocket conn = (WebSocket) pairs.getKey();
                if (type == EventType.AFTER_ELEMENT) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", "visitedElement");
                    jsonObject.put("id", element.getId());
                    jsonObject.put("visitedCount", machine.getProfiler().getVisitCount(element));
                    conn.send(jsonObject.toString());
                }
            }
        }
    }

    public void startService() {
        start();

        logger.info("GraphWalkerServer started on port: " + getPort());

        // Shutdown event
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println();
                System.out.println("GraphWalkerServer shutting down");
                System.out.println();
                logger.info("GraphWalkerServer shutting down");
            }
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
}
