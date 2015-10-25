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
import org.graphwalker.io.factory.json.JsonContextFactory;
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
 * A WebSocketServer implementation.
 */
public class WebSocketServer extends org.java_websocket.server.WebSocketServer implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private Set<WebSocket> sockets;
    private Map<WebSocket, Machine> machines;
    private Map<WebSocket, List<Context>> contexts;
    private Map<org.java_websocket.WebSocket, List<Model>> models;

    public WebSocketServer(int port) {
        super(new InetSocketAddress(port));
        sockets = new HashSet<>();
        machines = new HashMap<>();
        contexts = new HashMap<>();
        models = new HashMap<>();
    }

    public WebSocketServer(InetSocketAddress address) {
        super(address);
        sockets = new HashSet<>();
        machines = new HashMap<>();
        contexts = new HashMap<>();
        models = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        sockets.add(socket);
        machines.put(socket, null);
        contexts.put(socket, new ArrayList<Context>());
        models.put(socket, new ArrayList<Model>());
        logger.info(socket.getRemoteSocketAddress().getAddress().getHostAddress() + " is now connected");
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        sockets.remove(socket);
        machines.remove(socket);
        contexts.remove(socket);
        models.remove(socket);
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

        String type = root.getString("type").toUpperCase();
        switch (type) {
            case "LOADMODEL":
                response.put("type", "loadModel");
                try {
                    Context context = new JsonContextFactory().create(root.getJSONObject("model").toString());
                    List<Context> executionContexts = contexts.get(socket);
                    executionContexts.add(context);
                    response.put("success", true);
                } catch (JSONException e) {
                    response.put("success", false);
                    response.put("message", "Could not parse the model: " + e.getMessage());
                }

                break;
            case "GETMODEL":
                response.put("type", "getModel");
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
                List<Context> executionContexts = contexts.get(socket);
                Machine machine;
                try {
                    machine = new SimpleMachine(executionContexts);
                    machine.addObserver(this);
                    machines.put(socket, machine);
                    response.put("type", "start");
                    response.put("success", true);
                } catch (MachineException e) {
                    response.put("type", "start");
                    response.put("success", false);
                    response.put("message", e.getMessage());
                }
                break;
            }
            case "GETNEXT": {
                Machine machine = machines.get(socket);
                response.put("type", "getNext");
                if (machine != null) {
                    machine.getNextStep();
                    response.put("success", true);
                    response.put("id", machine.getCurrentContext().getCurrentElement().getId());
                    response.put("name", machine.getCurrentContext().getCurrentElement().getName());
                } else {
                    response.put("success", false);
                    response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
                }

                break;
            }
            case "HASNEXT": {
                Machine machine = machines.get(socket);
                response.put("type", "hasNext");
                if (machine == null) {
                    response.put("success", false);
                    response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
                } else if (machine.hasNextStep()) {
                    response.put("success", true);
                    response.put("hasNext", true);
                } else {
                    response.put("success", true);
                    response.put("hasNext", false);
                }

                break;
            }
            case "RESTART":
                machines.put(socket, null);
                contexts.put(socket, null);
                contexts.put(socket, new ArrayList<Context>());
                response.put("type", "restart");
                response.put("success", true);

                break;
            case "GETDATA": {
                response.put("type", "getData");
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
                    response.put("success", false);
                    response.put("message", "The GraphWalker state machine is not initiated. Is a model loaded, and started?");
                }

                break;
            }
            case "ADDMODEL": {
                response.put("command", "addModel");
                Model model = new Model().setId(root.getString("id"));
                List<Model> modelsList = models.get(socket);
                modelsList.add(model);
                response.put("model", new JsonContextFactory().getJsonFromModel(model));
                response.put("success", true);

                break;
            }
            case "ADDVERTEX": {
                response.put("command", "addVertex");
                List<Model> modelsList = models.get(socket);
                Model model = getModelById(modelsList, root.getString("modelId"));

                if (model != null) {
                    Vertex vertex = new Vertex().setId(root.getString("vertexId"));
                    model.addVertex(vertex);
                    response.put("modelId", model.getId());
                    response.put("vertex", new Gson().toJson(vertex.build()));
                    response.put("success", true);

                } else {
                    response.put("success", false);
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "ADDEDGE": {
                response.put("command", "addEdge");
                List<Model> modelsList = models.get(socket);
                Model model = getModelById(modelsList, root.getString("modelId"));

                if (model != null) {
                    Edge edge = new Edge().setId(root.getString("edgeId"));
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

                    response.put("modelId", model.getId());
                    response.put("edge", new Gson().toJson(edge.build()));
                    response.put("success", true);

                } else {
                    response.put("success", false);
                    response.put("message", "No models. You need to call newModel first.");
                }

                break;
            }
            case "UPDATEVERTEX": {
                response.put("command", "updateVertex");
                List<Model> modelsList = models.get(socket);
                Model model = getModelById(modelsList, root.getString("modelId"));

                if (model != null) {
                    Vertex vertex = null;
                    for (Vertex v : model.getVertices()) {
                        if (v.getId().equals(root.getString("vertexId"))) {
                            vertex = v;
                            break;
                        }
                    }

                    if (vertex == null) {
                        response.put("success", false);
                        response.put("message", "Did not find a vertex with id: " + root.getString("vertexId"));
                    } else {
                        try {
                            JSONObject object = root.getJSONObject("properties");
                            String[] keys = JSONObject.getNames(object);
                            for (String key : keys) {
                                Object value = object.get(key);
                                if (key.equals("name")) {
                                    vertex.setName(value.toString());
                                } else {
                                    vertex.setProperty(key, value);
                                }
                            }
                        } catch (JSONException e) {
                            logger.debug("Caught exception when parsing vertex: " + vertex.getId() + ", " + e.getMessage());
                        }

                        response.put("modelId", model.getId());
                        response.put("vertex", new Gson().toJson(vertex.build()));
                        response.put("success", true);
                    }
                } else {
                    response.put("success", false);
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "UPDATEEDGE": {
                response.put("command", "updateEdge");
                List<Model> modelsList = models.get(socket);
                Model model = getModelById(modelsList, root.getString("modelId"));

                if (model != null) {
                    Edge edge = null;
                    for (Edge e : model.getEdges()) {
                        if (e.getId().equals(root.getString("edgeId"))) {
                            edge = e;
                            break;
                        }
                    }

                    if (edge == null) {
                        response.put("success", false);
                        response.put("message", "Did not find an edge with id: " + root.getString("egdeId"));
                    } else {
                        try {
                            JSONObject object = root.getJSONObject("properties");
                            String[] keys = JSONObject.getNames(object);
                            for (String key : keys) {
                                Object value = object.get(key);
                                if (key.equals("name")) {
                                    edge.setName(value.toString());
                                } else {
                                    edge.setProperty(key, value);
                                }
                            }
                        } catch (JSONException e) {
                            logger.debug("Caught exception when parsing edge: " + edge.getId() + ", " + e.getMessage());
                        }

                        response.put("modelId", model.getId());
                        response.put("edge", new Gson().toJson(edge.build()));
                        response.put("success", true);
                    }
                } else {
                    response.put("success", false);
                    response.put("message", "Did not find a model with id: " + root.getString("modelId"));
                }

                break;
            }
            case "DELETEVERTEX": {
                response.put("command", "deleteVertex");
                List<Model> modelsList = models.get(socket);
                Model model = getModelById(modelsList, root.getString("modelId"));

                if (model != null) {
                    for (Vertex v : model.getVertices()) {
                        if (v.getId().equals(new JSONObject(message).getString("vertexId"))) {
                            model.deleteVertex(v);
                        }
                    }
                    response.put("success", true);

                } else {
                    response.put("success", false);
                    response.put("message", "No models. You need to call newModel first.");
                }

                break;
            }
            case "DELETEEDGE": {
                response.put("command", "deleteEdge");
                List<Model> modelsList = models.get(socket);
                Model model = getModelById(modelsList, root.getString("modelId"));

                if (model != null) {
                    for (Edge e : model.getEdges()) {
                        if (e.getId().equals(new JSONObject(message).getString("edgeId"))) {
                            model.deleteEdge(e);
                        }
                    }

                } else {
                    response.put("success", false);
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

    private Context getContextById(List<Context> contextsList, String id) {
        for (Context c : contextsList) {
            if (c.getModel().getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private Model getModelById(List<Model> modelsList, String id) {
        for (Model m : modelsList) {
            if (m.getId().equals(id)) {
                return m;
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
                    jsonObject.put("type", "visitedElement");
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