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

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.nio.file.Paths;

import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.java.annotation.BeforeExecution;
import org.graphwalker.java.annotation.GraphWalker;
import org.graphwalker.java.test.TestExecutionException;
import org.graphwalker.java.test.TestExecutor;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by krikar on 10/10/14.
 */
@GraphWalker(value = "random(edge_coverage(100))", start = "e_Connect")
public class WebSocketServerTest extends ExecutionContext implements WebSocketFlow {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketServerTest.class);

  private WebSocketClient client = new WebSocketClient();
  private WebSocketServer server;
  private int numberOfConnections = 0;
  private int expectedValue = 54686327;

  @BeforeExecution
  public void startServer() throws Exception {
    SingletonRandomGenerator.setSeed(222930684376058L);
    server = new WebSocketServer(8887);
    server.start();
  }

  @Test
  public void testRun() throws IOException {
    TestExecutor testExecutor = new TestExecutor(getClass());
    try {
      testExecutor.execute(false);
    } catch (TestExecutionException e) {
      if (e.hasErrors()) {
        for (String error : e.getResult().getErrors()) {
          System.err.println(error);
        }
        Assert.fail("Did not expect any errors");
      }
    }
  }

  @Override
  public void e_StartMachine() {
    client.startMachine(Paths.get("json/SmallModel.json"));
  }

  @Override
  public void e_Connect() {
    numberOfConnections = server.getSockets().size();
    client.run();
  }

  @Override
  public void v_MachineRunning() {
    WebSocket socket = server.getSockets().iterator().next();
    Assert.assertNotNull(server.getMachines().get(socket));
  }

  @Override
  public void e_HasNext() {
    client.hasNext();
  }

  @Override
  public void v_EmptyMachine() {
    Assert.assertThat("Before we connected, we should have no connections", numberOfConnections, is(0));
    Assert.assertThat("We should now haw 1 connection", server.getSockets().size(), is(1));
    Assert.assertThat(server.getMachines().size(), is(1));
    WebSocket socket = server.getSockets().iterator().next();
    Assert.assertNull(server.getMachines().get(socket));
  }

  @Override
  public void e_GetData() {
    String response =  client.getData();
    JSONObject jsonObject = new JSONObject(response);
    Assert.assertEquals(expectedValue, jsonObject.getInt("value"));
  }

  @Override
  public void e_SetData() {
    expectedValue = SingletonRandomGenerator.nextInt();
    client.setData("value=" + expectedValue);
  }

  @Override
  public void e_GetNext() {
    client.getNext();
  }
}
