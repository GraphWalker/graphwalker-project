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
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.factory.json.JsonEdge;
import org.graphwalker.io.factory.json.JsonVertex;
import org.graphwalker.java.annotation.BeforeExecution;
import org.graphwalker.java.annotation.GraphWalker;
import org.graphwalker.java.test.Result;
import org.graphwalker.java.test.TestExecutor;
import org.java_websocket.WebSocket;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-10-31.
 */
@GraphWalker(value = "random(edge_coverage(100))", start = "v_ModelHandler")
public class EditorTest extends ExecutionContext implements Editor {
    private static final Logger logger = LoggerFactory.getLogger(EditorTest.class);

    private Random random = new Random(System.currentTimeMillis());
    private WebSocketClient client = new WebSocketClient("localhost", 8888);
    private WebSocketServer server;
    private Model activeModel = null;
    private WebSocket mySessionSocket = null;
    private List<Model> models = new ArrayList<>();

    @BeforeExecution
    public void StartServer() throws Exception {
        server = new WebSocketServer(8888);
        server.start();
        client.run();
        mySessionSocket = server.getSockets().iterator().next();
    }

    @Test
    public void TestRun() {
        Result result = new TestExecutor(getClass()).execute(true);
        if (result.hasErrors()) {
            for (String error : result.getErrors()) {
                System.out.println(error);
            }
            Assert.fail("Test run failed.");
        }
    }

    @Override
    public void e_RemoveEdge() {
        Edge edge = activeModel.getEdges().get(random.nextInt(activeModel.getEdges().size()));
        logger.debug("Will remove edge with id: " + edge.getId());
        activeModel.deleteEdge(edge);
        client.removeEdge(activeModel.getId(), edge.getId());
    }

    @Override
    public void e_AddModel() {
        activeModel = new Model().setId(UUID.randomUUID().toString());
        models.add(activeModel);
        client.addModel(activeModel.getId());
    }

    @Override
    public void e_RemoveModel() {
        Model removedModel = models.remove(random.nextInt(models.size()));
        client.removeModel(removedModel.getId());
    }

    @Override
    public void e_ChangeVertex() {
        Vertex vertex = activeModel.getVertices().get(random.nextInt(activeModel.getVertices().size()));
        logger.debug("Will change vertex with id: " + vertex.getId());

        // Create change in properties
        vertex.setName(UUID.randomUUID().toString());
        JsonVertex jsonVertex = new JsonVertex();
        jsonVertex.setVertex(vertex);
        client.updateVertex(activeModel.getId(), new Gson().toJson(jsonVertex));
    }

    @Override
    public void e_RemoveVertex() {
        Vertex vertex = activeModel.getVertices().get(random.nextInt(activeModel.getVertices().size()));
        logger.debug("Will remove vertex with id: " + vertex.getId());
        activeModel.deleteVertex(vertex);
        client.removeVertex(activeModel.getId(), vertex.getId());

        // Set the new number of edges in the model attribute: num_of_edges
        // Removing a vertex, also removes all its in and out edges is rather
        // done here. Having that logic in the model makes the model too
        // cluttered. Having it here does not affect the models purpose for the test.
        // However, dry running the model and running the test will generate different
        // path sequences.
        setAttribute("num_of_edges", ((Integer) activeModel.getEdges().size()).doubleValue());
    }

    @Override
    public void e_ChangeEdge() {
        Edge edge = activeModel.getEdges().get(random.nextInt(activeModel.getEdges().size()));
        logger.debug("Will change edge with id: " + edge.getId());

        // Create change in properties
        edge.setName(UUID.randomUUID().toString());
        JsonEdge jsonEdge = new JsonEdge();
        jsonEdge.setEdge(edge);
        client.updateEdge(activeModel.getId(), new Gson().toJson(jsonEdge));
    }

    @Override
    public void v_ActiveModel() {
        logger.debug("Active model: " + activeModel.getId());
        Model serverModel = server.getModelById(server.getModels().get(mySessionSocket), activeModel.getId());
        Assert.assertNotNull(serverModel);
        Assert.assertThat(serverModel.getId(), is(activeModel.getId()));
        Assert.assertThat(serverModel.getVertices().size(), is(activeModel.getVertices().size()));
        Assert.assertThat(serverModel.getEdges().size(), is(activeModel.getEdges().size()));

        int num_of_vertices = ((Double) getAttribute("num_of_vertices")).intValue();
        logger.debug("Number of vertices: " + num_of_vertices);
        Assert.assertThat(serverModel.getVertices().size(), is(num_of_vertices));

        int num_of_edges = ((Double) getAttribute("num_of_edges")).intValue();
        logger.debug("Number of edges: " + num_of_edges);
        Assert.assertThat(serverModel.getEdges().size(), is(num_of_edges));
    }

    @Override
    public void e_SelectModel() {
        activeModel = models.get(random.nextInt(models.size()));

        // We are handling several models. Set the correct number of vertices and
        // edges in the model.
        setAttribute("num_of_vertices", ((Integer) activeModel.getVertices().size()).doubleValue());
        setAttribute("num_of_edges", ((Integer) activeModel.getEdges().size()).doubleValue());
    }

    @Override
    public void e_AddVertex() {
        Vertex vertex = new Vertex().setId(UUID.randomUUID().toString());
        activeModel.addVertex(vertex);
        client.addVertex(activeModel.getId(), vertex.getId());
    }

    @Override
    public void v_ModelHandler() {
        int num_of_models = ((Double) getAttribute("num_of_models")).intValue();
        logger.debug("Number of models: " + num_of_models);
        if (num_of_models == 0) {
            Assert.assertNull(server.getMachines().get(mySessionSocket));
        }
        Assert.assertThat(server.getContexts().get(mySessionSocket).size(), is(0));
        Assert.assertThat(server.getModels().get(mySessionSocket).size(), is(num_of_models));
    }

    @Override
    public void e_AddEdge() {
        Edge edge = new Edge().setId(UUID.randomUUID().toString());
        edge.setSourceVertex(activeModel.getVertices().get(random.nextInt(activeModel.getVertices().size())));
        edge.setTargetVertex(activeModel.getVertices().get(random.nextInt(activeModel.getVertices().size())));
        activeModel.addEdge(edge);
        client.addEdge(activeModel.getId(), edge.getId(), edge.getSourceVertex().getId(), edge.getTargetVertex().getId());
    }
}
