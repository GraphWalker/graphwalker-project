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
import org.graphwalker.core.model.*;
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

import java.util.*;

import static org.graphwalker.websocket.EditorTest.ChangeState.*;
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
    private ChangeState changeState = NONE;
    private Object changedValue = null;
    private String changedElementId = null;
    private HashMap<ChangeState, Boolean> edgeChangeData = new HashMap<>();
    private HashMap<ChangeState, Boolean> vertexChangeData = new HashMap<>();

    public enum ChangeState {
        NAME,
        ACTION,
        REQUIREMENT,
        POSITION,
        SHAPE,
        COLOR,
        NONE
    }

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

        // Create some change in some random property.
        changedValue = UUID.randomUUID().toString();
        changedElementId = vertex.getId();
        switch (ChangeState.values()[random.nextInt(values().length)]) {
            case NAME:
                changeState = NAME;
                vertex.setName((String) changedValue);
                logger.debug("Changing name to: " + changedValue);
                vertexChangeData.put(NAME, true);
                break;

            case REQUIREMENT:
                changeState = REQUIREMENT;
                vertex.addRequirement(new Requirement((String) changedValue));
                logger.debug("Adding the requirement: " + changedValue);
                vertexChangeData.put(REQUIREMENT, true);
                break;

            case POSITION:
                changeState = POSITION;
                vertex.setProperty("position", changedValue);
                logger.debug("Changing position to: " + changedValue);
                vertexChangeData.put(POSITION, true);
                break;

            case SHAPE:
                changeState = SHAPE;
                vertex.setProperty("shape", changedValue);
                logger.debug("Changing shape to: " + changedValue);
                vertexChangeData.put(SHAPE, true);
                break;

            case COLOR:
                changeState = COLOR;
                vertex.setProperty("color", changedValue);
                logger.debug("Changing color to: " + changedValue);
                vertexChangeData.put(COLOR, true);
                break;

            default:
                logger.error("This was unexpected...");
                break;
        }

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

        // Create some change in some random property.
        changedValue = UUID.randomUUID().toString();
        changedElementId = edge.getId();
        switch (ChangeState.values()[random.nextInt(values().length)]) {
            case NAME:
                changeState = NAME;
                edge.setName((String) changedValue);
                logger.debug("Changing name to: " + changedValue);
                edgeChangeData.put(NAME, true);
                break;

            case ACTION:
                changeState = ACTION;
                edge.addAction(new Action((String) changedValue));
                logger.debug("Adding the action: " + changedValue);
                edgeChangeData.put(ACTION, true);
                break;

            case REQUIREMENT:
                changeState = REQUIREMENT;
                edge.addRequirement(new Requirement((String) changedValue));
                logger.debug("Adding the requirement: " + changedValue);
                edgeChangeData.put(REQUIREMENT, true);
                break;

            case POSITION:
                changeState = POSITION;
                edge.setProperty("position", changedValue);
                logger.debug("Changing position to: " + changedValue);
                edgeChangeData.put(POSITION, true);
                break;

            case SHAPE:
                changeState = SHAPE;
                edge.setProperty("shape", changedValue);
                logger.debug("Changing shape to: " + changedValue);
                edgeChangeData.put(SHAPE, true);
                break;

            case COLOR:
                changeState = COLOR;
                edge.setProperty("color", changedValue);
                logger.debug("Changing color to: " + changedValue);
                edgeChangeData.put(COLOR, true);
                break;

            default:
                logger.error("This was unexpected...");
                break;
        }

        JsonEdge jsonEdge = new JsonEdge();
        jsonEdge.setEdge(edge);
        client.updateEdge(activeModel.getId(), new Gson().toJson(jsonEdge));
    }

    private RuntimeBase getElement(Model model, String id) {
        List<Object> elements = new ArrayList<>();
        elements.addAll(model.getVertices());
        elements.addAll(model.getEdges());
        for (Object o : elements) {
            if (o instanceof Vertex) {
                Vertex vertex = (Vertex) o;
                if (vertex.getId().equals(id)) {
                    return vertex.build();
                }
            } else if (o instanceof Edge) {
                Edge edge = (Edge) o;
                if (edge.getId().equals(id)) {
                    return edge.build();
                }
            }
        }
        return null;
    }

    @Override
    public void v_ActiveModel() {
        // Verify active model
        logger.debug("Active model: " + activeModel.getId());
        Model serverModel = server.getModelById(server.getModels().get(mySessionSocket), activeModel.getId());

        Assert.assertNotNull(serverModel);
        Assert.assertThat(serverModel.getId(), is(activeModel.getId()));
        Assert.assertThat(serverModel.getVertices().size(), is(activeModel.getVertices().size()));
        Assert.assertThat(serverModel.getEdges().size(), is(activeModel.getEdges().size()));


        // Verify number of vertices
        int num_of_vertices = ((Double) getAttribute("num_of_vertices")).intValue();
        logger.debug("Number of vertices: " + num_of_vertices);
        Assert.assertThat(serverModel.getVertices().size(), is(num_of_vertices));


        // Verify number of edges
        int num_of_edges = ((Double) getAttribute("num_of_edges")).intValue();
        logger.debug("Number of edges: " + num_of_edges);
        Assert.assertThat(serverModel.getEdges().size(), is(num_of_edges));


        if (changeState != NONE) {
            // Verify the change
            RuntimeBase element = getElement(serverModel, changedElementId);
            logger.debug("Verifying the element with id: " + element.getId());
            switch (changeState) {
                case NAME:
                    logger.debug("Verifying the name change: " + changedValue);
                    Assert.assertThat((String) changedValue, is(element.getName()));
                    break;

                case ACTION:
                    logger.debug("Verifying the added action: " + changedValue);
                    Assert.assertTrue(element.hasActions());
                    Iterator iter = element.getActions().iterator();
                    boolean foundValue = false;
                    while (iter.hasNext()) {
                        Action action = (Action) iter.next();
                        if (action.getScript().equals(changedValue)) {
                            foundValue = true;
                            break;
                        }
                    }
                    Assert.assertTrue(foundValue);
                    break;

                case REQUIREMENT:
                    logger.debug("Verifying the added requirement: " + changedValue);
                    Assert.assertTrue(element.hasRequirements());
                    iter = element.getRequirements().iterator();
                    foundValue = false;
                    while (iter.hasNext()) {
                        Requirement requirement = (Requirement) iter.next();
                        if (requirement.getKey().equals(changedValue)) {
                            foundValue = true;
                            break;
                        }
                    }
                    Assert.assertTrue(foundValue);
                    break;

                case POSITION:
                    logger.debug("Verifying the changed position: " + changedValue);
                    Assert.assertThat(element.getProperty("position"), is(changedValue));
                    break;

                case SHAPE:
                    logger.debug("Verifying the shape change: " + changedValue);
                    Assert.assertThat(element.getProperty("shape"), is(changedValue));
                    break;

                case COLOR:
                    logger.debug("Verifying the color change: " + changedValue);
                    Assert.assertThat(element.getProperty("color"), is(changedValue));
                    break;

                case NONE:
                    break;
                default:
                    Assert.fail("Shoot the developer!");
                    break;
            }
            changeState = NONE;
            changedValue = null;
            changedElementId = null;
        }

        // Set the model attribute allDataExhausted to true, if all data for changing
        // different attributes in edges and vertices has been executed.
        logger.debug("Data status: " + ((float) (edgeChangeData.size() + vertexChangeData.size()) / 11) * 100 + " % covered.");
        if (edgeChangeData.size() >= 6 && vertexChangeData.size() >= 5) {
            setAttribute("allDataExhuasted", true);
            logger.debug("allDataExhuasted: " + getAttribute("allDataExhuasted"));
        }
    }

    @Override
    public void v_AllChangesDone() {
        logger.debug("All tests are done!");
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
