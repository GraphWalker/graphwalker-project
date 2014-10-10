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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.example.GraphWalkerWebSocketClient;
import org.graphwalker.java.annotation.GraphWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * Created by krikar on 10/10/14.
 */
@GraphWalker(value = "random(edge_coverage(100))", start = "e_Connect")
public class GraphWalkerWebSocketServerTest  extends ExecutionContext implements WebSocketFlow {

    private static final Logger logger = LoggerFactory.getLogger(GraphWalkerWebSocketServerTest.class);
    GraphWalkerWebSocketClient client = new GraphWalkerWebSocketClient();

    @Override
    public void v_ModelLoaded() {
        logger.info("v_ModelLoaded");
    }

    @Override
    public void e_StartMachine() {
        logger.info("e_StartMachine");
        client.startMachine();
    }

    @Override
    public void e_AddModel() {
        logger.info("e_AddModel");
        client.loadModel(Paths.get("json/SmallModel.json"));
    }

    @Override
    public void e_RestartMachine() {
        logger.info("e_RestartMachine");
        client.restartMachine();
    }

    @Override
    public void e_AddInitialModel() {
        logger.info("e_AddInitialModel");
        client.loadModel(Paths.get("json/SmallModel.json"));
    }

    @Override
    public void e_Connect() {
        logger.info("e_Connect");
        client.run();
    }

    @Override
    public void v_MachineRunning() {
        logger.info("v_MachineRunning");
    }

    @Override
    public void e_HasNext() {
        logger.info("e_HasNext");
        client.hasNext();
    }

    @Override
    public void v_EmptyMachine() {
        logger.info("v_EmptyMachine");
    }

    @Override
    public void e_GetData() {
        logger.info("e_GetData");
        client.getData();
    }

    @Override
    public void e_GetNext() {
        logger.info("e_GetNext");
        client.getNext();
    }
}
