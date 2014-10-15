package org.graphwalker.cli;

/*
 * #%L
 * GraphWalker Command Line Interface
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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

import org.graphwalker.cli.service.GraphWalkerWebSocketServer;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.example.GraphWalkerWebSocketClient;
import org.graphwalker.java.annotation.GraphWalker;
import org.java_websocket.WebSocket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by krikar on 10/10/14.
 */
@GraphWalker(value = "random(edge_coverage(100))", start = "e_Connect")
public class GraphWalkerWebSocketServerTest extends ExecutionContext implements WebSocketFlow {

    private static final Logger logger = LoggerFactory.getLogger(GraphWalkerWebSocketServerTest.class);
    GraphWalkerWebSocketClient client = new GraphWalkerWebSocketClient();
    GraphWalkerWebSocketServer server;
    int numOfConns = 0;
    int numOfModels = 0;

    @Before
    public void StartServer() throws Exception {
        server = new GraphWalkerWebSocketServer(8887);
        server.start();
    }

    @Test
    public void TestRun() throws Exception {
    }

    @Override
    public void v_ModelLoaded() {
        WebSocket conn = server.getConns().iterator().next();
        Assert.assertNull(server.getMachines().get(conn));
        //  Assert.assertThat(server.getContexts().get(conn).size(), is(numOfModels));
    }

    @Override
    public void e_StartMachine() {
        client.startMachine();
    }

    @Override
    public void e_AddModel() {
        numOfModels++;
        client.loadModel(Paths.get("json/SmallModel.json"));
    }

    @Override
    public void e_RestartMachine() {
        client.restartMachine();
    }

    @Override
    public void e_AddInitialModel() {
        numOfModels++;
        client.loadModel(Paths.get("json/SmallModel.json"));
    }

    @Override
    public void e_Connect() {
        numOfConns = server.getConns().size();
        client.run();
    }

    @Override
    public void v_MachineRunning() {
        WebSocket conn = server.getConns().iterator().next();
        Assert.assertNotNull(server.getMachines().get(conn));
        Assert.assertThat(server.getContexts().get(conn).size(), is(numOfModels));
    }

    @Override
    public void e_HasNext() {
        client.hasNext();
    }

    @Override
    public void v_EmptyMachine() {
        Assert.assertThat("Before we connected, we should have no connections", numOfConns, is(0));
        Assert.assertThat("We should now haw 1 connection", server.getConns().size(), is(1));

        Assert.assertThat(server.getMachines().size(), is(1));
        Assert.assertThat(server.getContexts().size(), is(1));

        WebSocket conn = server.getConns().iterator().next();
        Assert.assertNull(server.getMachines().get(conn));
        Assert.assertThat(server.getContexts().get(conn).size(), is(0));
    }

    @Override
    public void e_GetData() {
        client.getData();
    }

    @Override
    public void e_GetNext() {
        client.getNext();
    }
}
