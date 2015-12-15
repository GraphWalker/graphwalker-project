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
import org.graphwalker.core.model.Element;
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


public class WebSocketListner extends org.java_websocket.server.WebSocketServer implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketListner.class);

    private Machine machine;
    private Set<WebSocket> sockets;

    public WebSocketListner(InetSocketAddress address, Machine machine) {
        super(address);
        this.machine = machine;
        this.machine.addObserver(this);
        sockets = new HashSet<>();
    }

    @Override
    public void onOpen(WebSocket socket, ClientHandshake handshake) {
        logger.info(socket.getRemoteSocketAddress().getAddress().getHostAddress() + " is now connected");
        sockets.add(socket);
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        logger.info(socket.getRemoteSocketAddress().getAddress().getHostAddress() + " has disconnected");
        sockets.remove(socket);
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
            case "GETGW3": {
                response.put("command", "getGW3");
                response.put("success", false);

                JSONArray jsonModels = new JSONArray();
                for (Context context : machine.getContexts()){
                    jsonModels.put(new JsonContextFactory().getJsonFromContext(context));
                }
                JSONObject jsonMachine = new JSONObject();
                jsonMachine.put("name", "WebSocketListner");
                jsonMachine.put("models", jsonModels);
                response.put("gw3", jsonMachine);
                response.put("success", true);
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

    @Override
    public void onError(WebSocket socket, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void update(Machine machine, Element element, EventType type) {
        logger.info("Received an update from a GraphWalker machine");
        logger.info("Event: " + type);
        if (type == EventType.AFTER_ELEMENT) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("command", "visitedElement");
            jsonObject.put("modelId", machine.getCurrentContext().getModel().getId());
            jsonObject.put("elementId", element.getId());
            jsonObject.put("visitedCount", machine.getProfiler().getVisitCount(element));
            jsonObject.put("totalCount", machine.getProfiler().getTotalVisitCount());
            jsonObject.put("stopConditionFulfillment", machine.getCurrentContext().getPathGenerator().getStopCondition().getFulfilment());

            JSONObject data = new JSONObject();
            for (Map.Entry<String, String> k : machine.getCurrentContext().getKeys().entrySet()) {
                data.put(k.getKey(), k.getValue());
            }
            jsonObject.put("data", data);

            send(jsonObject);
        }
    }

    private void send(JSONObject jsonObject) {
        for (WebSocket socket : sockets) {
            logger.debug("Sending: " + jsonObject.toString());
            socket.send(jsonObject.toString());
        }
    }
}
