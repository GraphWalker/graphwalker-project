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

import org.graphwalker.io.common.ResourceUtils;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class WebSocketClient {

    private org.java_websocket.client.WebSocketClient wsc;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);

    private enum RX_STATE {
        NONE,
        HASNEXT,
        LOADMODEL,
        START,
        RESTART,
        GETNEXT,
        GETDATA,
        VISITEDELEMENT
    }

    private final String START = "{\"type\": \"start\"}";
    private final String RESTART = "{\"type\": \"restart\"}";
    private final String GET_NEXT = "{\"type\": \"getNext\"}";
    private final String HAS_NEXT = "{\"type\": \"hasNext\"}";
    private final String GET_DATA = "{\"type\": \"getData\"}";

    public boolean connected = false;
    public RX_STATE rxState = RX_STATE.NONE;
    public boolean cmd = false;
    public boolean hasNext = false;
    private int port = 8887;
    private String host = "localhost";
    private WebSocketClient client;

    /**
     * Creates an instance of the GraphWalker WebSocket client.
     * No connections will be made until the  {@link #run() run}  method is called.
     * The default hostname is localhost. The default port is 8887
     */
    public WebSocketClient() {
    }

    /**
     * Creates an instance of the GraphWalker WebSocket client.
     * No connections will be made until the  {@link #run() run}  method is called.
     *
     * @param host the host to which connect this client
     * @param port the port to which connect this client
     */
    public WebSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connects the GraphWalker client to the GraphWalker WebSocket
     * server on ws://hostname:port
     * When connected, a GraphWalker machine will be created on the server
     * which will serve this client only.
     */
    public void run() {
        client = new WebSocketClient(host, port);
        client.connect();
        while (!client.connected) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() {
        try {
            wsc = new org.java_websocket.client.WebSocketClient(new URI("ws://" + host + ":" + port), new Draft_10()) {

                @Override
                public void onMessage(String message) {
                    logger.debug("Got message: " + message );
                    JSONObject root;
                    try {
                        root = new JSONObject(message);
                    } catch (JSONException e) {
                        logger.error("Message is not JSON formatted: " + e.getMessage());
                        return;
                    }

                    rxState = RX_STATE.NONE;
                    cmd = false;
                    String type = root.getString("type").toUpperCase();
                    if (type.equals("HASNEXT")) {
                        hasNext = false;
                        rxState = RX_STATE.HASNEXT;
                        if (root.getBoolean("success")) {
                            cmd = true;
                            if (root.getBoolean("hasNext")) {
                                hasNext = true;
                            }
                        }
                    } else if (type.equals("LOADMODEL")) {
                        rxState = RX_STATE.LOADMODEL;
                        if (root.getBoolean("success")) {
                            cmd = true;
                        }
                    } else if (type.equals("START")) {
                        rxState = RX_STATE.START;
                        if (root.getBoolean("success")) {
                            cmd = true;
                        }
                    } else if (type.equals("RESTART")) {
                        rxState = RX_STATE.RESTART;
                        if (root.getBoolean("success")) {
                            cmd = true;
                        }
                    } else if (type.equals("GETNEXT")) {
                        rxState = RX_STATE.GETNEXT;
                        if (root.getBoolean("success")) {
                            cmd = true;
                        }
                    } else if (type.equals("GETDATA")) {
                        rxState = RX_STATE.GETDATA;
                        if (root.getBoolean("success")) {
                            cmd = true;
                        }
                    } else if (type.equals("VISITEDELEMENT")) {
                        rxState = RX_STATE.VISITEDELEMENT;
                    } else {
                        logger.info("Message type is not implemented: " + type);
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    logger.info("Connected to: " + getURI() );
                    connected = true;
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.info("You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason);
                    System.out.println();
                    connected = false;
                }

                @Override
                public void onError(Exception ex) {
                    logger.error("Exception occured ...\n" + ex);
                    ex.printStackTrace();
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        wsc.connect();
    }

    private void wait(WebSocketClient client, RX_STATE state) {
        while (client.rxState != state) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!client.cmd) {
            throw new RuntimeException("Failed to execute command");
        }
    }

    /**
     * Loads a model into the GraphWalker machine. The first model loaded, is where
     * the execution will start.
     * Several models can be loaded. Every model which is loaded, will have it's own
     * context in the machine.
     *
     * @param model a JSON formatted GraphWalker model as a string
     */
    public void loadModel(String model) {
        logger.debug("Loading model: " + model);
        client.wsc.send(model);
        wait(client, RX_STATE.LOADMODEL);
    }

    /**
     * Loads a model into the GraphWalker machine. The first model loaded, is where
     * the execution will start.
     * Several models can be loaded. Every model which is loaded, will have it's own
     * context in the machine.
     *
     * @param path  a JSON formatted GraphWalker model as a file
     */
    public void loadModel(Path path) {
        logger.debug("Loading model file: " + path.toString());
        BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(path.toString())));
        StringBuilder out = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadModel(out.toString());
    }

    /**
     * Closes the connection with the GraphWalker server.
     */
    public void close() {
        logger.debug("Will close");
        client.wsc.close();
    }

    /**
     * Starts the machine. No more loadModel calls are allowed.
     */
    public void startMachine() {
        logger.debug("Start the machine");
        client.wsc.send(START);
        wait(client, RX_STATE.START);
    }

    /**
     * Restarts the machine. All previously loaded models will be discared.
     */
    public void restartMachine() {
        logger.debug("Restart the machine");
        client.wsc.send(RESTART);
        wait(client, RX_STATE.RESTART);
    }

    /**
     * Gets the next element from the the GraphWalker machine
     */
    public void getNext() {
        logger.debug("Get next step");
        client.wsc.send(GET_NEXT);
        wait(client, RX_STATE.GETNEXT);
    }

    /**
     * Checks if the machine has more steps to generate.
     *
     * @return If all stop conditions are fulfilled for the machine, true is returned. Otherwise false.
     */
    public boolean hasNext() {
        logger.debug("Have next step?");
        client.wsc.send(HAS_NEXT);
        wait(client, RX_STATE.HASNEXT);
        return client.hasNext;
    }

    /**
     * Asks the machine to return all data from the current model context.
     */
    public void getData() {
        logger.debug("Get data");
        client.wsc.send(GET_DATA);
        wait(client, RX_STATE.GETDATA);
    }
}