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

import com.google.gson.Gson;
import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.common.Util;
import org.graphwalker.io.factory.gw3.GW3ContextFactory;
import org.graphwalker.io.factory.json.*;
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
    private Map<WebSocket, List<Context>> contexts;

    public WebSocketServer(int port) {
        super(new InetSocketAddress(port));
        sockets = new HashSet<>();
        machines = new HashMap<>();
        contexts = new HashMap<>();
    }

    public WebSocketServer(InetSocketAddress address) {
        super(address);
        sockets = new HashSet<>();
        machines = new HashMap<>();
        contexts = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        sockets.add(socket);
        machines.put(socket, null);
        contexts.put(socket, new ArrayList<Context>());
        logger.info(socket.getRemoteSocketAddress().getAddress().getHostAddress() + " is now connected");
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        sockets.remove(socket);
        machines.remove(socket);
        contexts.remove(socket);
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
            case "LOADGW3":
                response.put("command", "loadgw3");
                try {
                    List<Context> gw3Contexts = new GW3ContextFactory().createMultiple(root.getJSONObject("gw3").toString());
                    List<Context> executionContexts = contexts.get(socket);
                    executionContexts.addAll(gw3Contexts);
                    response.put("success", true);
                    checkContexts(socket, gw3Contexts);
                } catch (JSONException e) {
                    response.put("success", false);
                    response.put("message", "Could not parse the model: " + e.getMessage());
                }

                break;
            case "LOADMODEL":
                response.put("command", "loadModel");
                try {
                    Context context = new JsonContextFactory().create(root.getJSONObject("model").toString());
                    List<Context> executionContexts = contexts.get(socket);
                    executionContexts.add(context);
                    response.put("success", true);
                    checkContext(socket, context);
                } catch (JSONException e) {
                    response.put("success", false);
                    response.put("message", "Could not parse the model: " + e.getMessage());
                }

                break;
            case "GETMODEL":
                response.put("command", "getModel");
                try {
                    List<Context> executionContexts = contexts.get(socket);
                    Context context = getContextById(executionContexts, root.getString("modelId"));
                    response.put("modelId", context.getModel().getId());
                    response.put("model", new JsonContextFactory().getJsonFromContext(context));
                    response.put("success", true);
                } catch (JSONException e) {
                    response.put("success", false);
                    response.put("message", "Could not parse the model: " + e.getMessage());
                }

                break;
            case "START": {
                response.put("command", "start");
                response.put("success", false);
                List<Context> executionContexts = contexts.get(socket);
                Util.filterBlockedElements(executionContexts);
                try {
                    Machine machine = new SimpleMachine(executionContexts);
                    machine.addObserver(this);
                    machines.put(socket, machine);
                    response.put("success", true);
                } catch (MachineException e) {
                    response.put("message", e.getMessage());
                }
                break;
            }
            case "GETNEXT": {
                response.put("command", "getNext");
                response.put("success", false);
                Machine machine = machines.get(socket);
                if (machine != null) {
                    machine.getNextStep();
                    response.put("success", true);
                    response.put("id", machine.getCurrentContext().getCurrentElement().getId());
                    response.put("name", machine.getCurrentContext().getCurrentElement().getName());
                } else {
                    response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
                }

                break;
            }
            case "HASNEXT": {
                response.put("command", "hasNext");
                response.put("success", false);
                try {
                    Machine machine = machines.get(socket);
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
                    response.put("success", false);
                    response.put("message", e.getMessage());
                }

                break;
            }
            case "RESTART":
                response.put("command", "restart");
                response.put("success", true);
                machines.put(socket, null);
                List<Context> executionContexts = contexts.get(socket);
                for (Context context : executionContexts) {
                    context.reset();
                }


                break;
            case "GETDATA": {
                response.put("command", "getData");
                response.put("success", false);
                Machine machine = machines.get(socket);
                if (machine != null) {
                    JSONArray jsonKeys = new JSONArray();
                    for (Map.Entry<String, String> key : machine.getCurrentContext().getKeys().entrySet()) {
                        JSONObject jsonKey = new JSONObject();
                        jsonKey.put(key.getKey(), key.getValue());
                        jsonKeys.put(jsonKey);
                    }
                    response.put("data", jsonKeys);
                    response.put("success", true);
                } else {
                    response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
                }

                break;
            }
            case "ADDMODEL": {
                response.put("command", "addModel");
                response.put("success", false);
                Model model = new Model().setId(root.getString("id"));
                Context context = new JsonContext().setModel(model.build());
                executionContexts = contexts.get(socket);
                executionContexts.add(context);
                response.put("model", new JsonContextFactory().getJsonFromContext(context));
                response.put("success", true);
                checkContexts(socket, contexts.get(socket));

                break;
            }
            case "REMOVEMODEL": {
                response.put("command", "removeModel");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("id"));

                if (context != null) {
                    contexts.get(socket).remove(context);
                    response.put("success", true);
                    checkContexts(socket, contexts.get(socket));
                } else {
                    response.put("message", "Did not find a model with id: " + root.getString("id"));
                }

                break;
            }
            case "SETGENERATOR": {
                response.put("command", "setGenerator");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    try {
                        String generatorString = root.getString("generator");
                        context.setPathGenerator(GeneratorFactory.parse(generatorString));
                        response.put("success", true);
                        checkContext(socket, context);
                    } catch (Exception e) {
                        response.put("message", "Could not set the path generator: " + e.getMessage());
                    }
                } else {
                    response.put("message", "Did not find a model with id: " + root.getString("id"));
                }

                break;
            }
            case "SETNEXTELEMENT": {
                response.put("command", "setNextElement");
                response.put("success", false);

                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    try {
                        String nextElementString = root.getString("nextElementId");
                        Element nextElement = context.getModel().getElementById(nextElementString);
                        context.setNextElement(nextElement);
                        response.put("success", true);
                        checkContext(socket, context);
                    } catch (JSONException e) {
                        response.put("message", "Could not set the start element: " + e.getMessage());
                    }
                } else {
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "ADDVERTEX": {
                response.put("command", "addVertex");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    Vertex vertex = new Vertex().setId(root.getString("vertexId"));
                    Model model = new Model(context.getModel());
                    model.addVertex(vertex);
                    context.setModel(model.build());
                    response.put("success", true);
                    checkContext(socket, context);
                } else {
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "ADDEDGE": {
                response.put("command", "addEdge");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    Edge edge = new Edge().setId(root.getString("edgeId"));
                    Model model = new Model(context.getModel());
                    model.addEdge(edge);

                    for (Vertex src : model.getVertices()) {
                        if (src.getId().equals(new JSONObject(message).getString("sourceVertexId"))) {
                            edge.setSourceVertex(src);
                            break;
                        }
                    }
                    for (Vertex dst : model.getVertices()) {
                        if (dst.getId().equals(new JSONObject(message).getString("targetVertexId"))) {
                            edge.setTargetVertex(dst);
                            break;
                        }
                    }
                    context.setModel(model.build());
                    response.put("success", true);
                    checkContext(socket, context);
                } else {
                    response.put("message", "No models. You need to call addModel first.");
                }

                break;
            }
            case "UPDATEVERTEX": {
                response.put("command", "updateVertex");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    Model model = new Model(context.getModel());
                    JSONObject vertexJsonObject = root.getJSONObject("vertex");
                    Vertex vertex = null;
                    for (Vertex v : model.getVertices()) {
                        if (v.getId().equals(vertexJsonObject.getString("id"))) {
                            vertex = v;
                            break;
                        }
                    }

                    if (vertex == null) {
                        response.put("message", "Did not find a vertex with id: " + root.getString("vertexId"));
                    } else {
                        JsonVertex jsonVertex = new Gson().fromJson(vertexJsonObject.toString(), JsonVertex.class);
                        jsonVertex.copyValues(vertex);
                        context.setModel(model.build());
                        response.put("success", true);
                        checkContext(socket, context);
                    }
                } else {
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "UPDATEEDGE": {
                response.put("command", "updateEdge");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    Model model = new Model(context.getModel());
                    JSONObject edgeJsonObject = root.getJSONObject("edge");
                    Edge edge = null;
                    for (Edge e : model.getEdges()) {
                        if (e.getId().equals(edgeJsonObject.getString("id"))) {
                            edge = e;
                            break;
                        }
                    }

                    if (edge == null) {
                        response.put("message", "Did not find an edge with id: " + root.getString("egdeId"));
                    } else {
                        JsonEdge jsonEdge = new Gson().fromJson(edgeJsonObject.toString(), JsonEdge.class);
                        jsonEdge.copyValues(edge);
                        context.setModel(model.build());
                        response.put("success", true);
                        checkContext(socket, context);
                    }
                } else {
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "REMOVEVERTEX": {
                response.put("command", "removeVertex");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    Model model = new Model(context.getModel());
                    for (Vertex v : model.getVertices()) {
                        if (v.getId().equals(new JSONObject(message).getString("vertexId"))) {
                            model.deleteVertex(v);
                            context.setModel(model.build());
                            response.put("success", true);
                            break;
                        }
                    }
                    checkContext(socket, context);
                } else {
                    response.put("message", "No models. You need to call newModel first.");
                }

                break;
            }
            case "REMOVEEDGE": {
                response.put("command", "removeEdge");
                response.put("success", false);
                Context context = getContextById(contexts.get(socket), root.getString("modelId"));

                if (context != null) {
                    Model model = new Model(context.getModel());
                    for (Edge e : model.getEdges()) {
                        if (e.getId().equals(new JSONObject(message).getString("edgeId"))) {
                            model.deleteEdge(e);
                            context.setModel(model.build());
                            response.put("success", true);
                            break;
                        }
                    }
                    checkContext(socket, context);
                } else {
                    response.put("message", "No models. You need to call newModel first.");
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

    private void checkContexts(WebSocket socket, List<Context> contexts) {
        List<String> issues = ContextsChecker.hasIssues(contexts);
        sendIssues(socket, issues);
    }

    private void checkContext(WebSocket socket, Context context){
        List<String> issues = ContextChecker.hasIssues(context);
        sendIssues(socket, issues);
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

    public Context getContextById(List<Context> contextsList, String id) {
        for (Context c : contextsList) {
            if (c.getModel().getId().equals(id)) {
                return c;
            }
        }
        return null;
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

    public Map<WebSocket, List<Context>> getContexts() {
        return contexts;
    }
}
