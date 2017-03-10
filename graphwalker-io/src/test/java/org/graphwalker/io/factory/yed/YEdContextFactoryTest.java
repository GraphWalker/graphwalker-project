package org.graphwalker.io.factory.yed;

/*
 * #%L
 * GraphWalker Input/Output
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

import org.apache.commons.io.IOUtils;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Edge.RuntimeEdge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Model.RuntimeModel;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.model.Vertex.RuntimeVertex;
import org.graphwalker.io.TestExecutionContext;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class YEdContextFactoryTest {

  @Test(expected = ContextFactoryException.class)
  public void fileDoesNotExistsOnFileSystem() throws IOException {
    new YEdContextFactory().create(Paths.get("graphml/LKHDIODSOSUBD.graphml"));
  }

  @Test
  public void shared() throws IOException {
    List<Context> sharedAContexts = new YEdContextFactory().create(Paths.get("graphml/SharedA.graphml"));
    Assert.assertNotNull(sharedAContexts);
    Assert.assertThat(sharedAContexts.size(), is(1));

    Assert.assertThat(sharedAContexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(sharedAContexts.get(0).getModel().getEdges().size(), is(6));

    List<Context> sharedBContexts = new YEdContextFactory().create(Paths.get("graphml/SharedB.graphml"));
    Assert.assertNotNull(sharedBContexts);
    Assert.assertThat(sharedBContexts.size(), is(1));

    Assert.assertThat(sharedBContexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(sharedBContexts.get(0).getModel().getEdges().size(), is(1));
  }

  @Test
  public void login() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/Login.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().findVertices("v_ClientNotRunning").size(), is(1));

    Vertex.RuntimeVertex vertex = contexts.get(0).getModel().findVertices("v_ClientNotRunning").get(0);
    Assert.assertNotNull(vertex);
    Assert.assertThat((String) vertex.getProperty("description"), is("Start the client process"));

    Edge.RuntimeEdge edge = contexts.get(0).getModel().findEdges("e_ValidPremiumCredentials").get(0);
    Assert.assertNotNull(edge);
    Assert.assertThat((String) edge.getProperty("description"), is("Log in a s Premium user, using valid credentials"));
  }

  @Test
  public void uc01() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/UC01.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    // Since the model id the Model.RuntimeModel,the Start vertex is removed from the graph.
    Assert.assertThat(context.getModel().getVertices().size(), is(7)); // one of the vertices is the start vertex and that shouldn't be a part of the model
    Assert.assertThat(context.getModel().getEdges().size(), is(12));
    Assert.assertThat(context.getModel().findEdges("e_init").size(), is(1));
    Assert.assertThat(context.getModel().findEdges("e_AddBookToCart").size(), is(1));
    Assert.assertThat(context.getModel().findEdges("e_ClickBook").size(), is(1));
    Assert.assertThat(context.getModel().findEdges("e_EnterBaseURL").size(), is(1));
    Assert.assertThat(context.getModel().findEdges("e_SearchBook").size(), is(4));
    Assert.assertThat(context.getModel().findEdges("e_ShoppingCart").size(), is(3));
    Assert.assertThat(context.getModel().findEdges("e_StartBrowser").size(), is(1));
    Assert.assertNull(context.getModel().findEdges(""));

    Assert.assertThat(context.getModel().findVertices("v_BaseURL").size(), is(1));
    Assert.assertThat(context.getModel().findVertices("v_BookInformation").size(), is(1));
    Assert.assertThat(context.getModel().findVertices("v_BrowserStarted").size(), is(1));
    Assert.assertThat(context.getModel().findVertices("v_BrowserStopped").size(), is(1));
    Assert.assertThat(context.getModel().findVertices("v_OtherBoughtBooks").size(), is(1));
    Assert.assertThat(context.getModel().findVertices("v_SearchResult").size(), is(1));
    Assert.assertThat(context.getModel().findVertices("v_ShoppingCart").size(), is(1));

    Assert.assertThat(context.getModel().findEdges("e_init").get(0).getTargetVertex().getName(), is("v_BrowserStopped"));

    Assert.assertThat(context.getModel().findEdges("e_StartBrowser").get(0).getSourceVertex().getName(), is("v_BrowserStopped"));
    Assert.assertThat(context.getModel().findEdges("e_StartBrowser").get(0).getTargetVertex().getName(), is("v_BrowserStarted"));
  }

  @Test
  public void efsmWithReqtags1() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/reqtags/ShoppingCart.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat("Number of vertices", contexts.get(0).getModel().getVertices().size(), is(6));
    Assert.assertThat("Number of edges", contexts.get(0).getModel().getEdges().size(), is(11));
    // TODO Fix req
    //Assert.assertThat("Number of requirements", context.getRequirements().size(), is(5));
  }

  @Test
  public void efsmWithReqtags2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/reqtags/EFSM_with_REQTAGS.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat("Number of vertices", contexts.get(0).getModel().getVertices().size(), is(7));
    Assert.assertThat("Number of edges", contexts.get(0).getModel().getEdges().size(), is(19));
    // TODO Fix req
    //Assert.assertThat("Number of requirements", context.getRequirements().size(), is(6));
  }

  @Test
  public void guards() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/Guards.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat("Number of vertices", contexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat("Number of edges", contexts.get(0).getModel().getEdges().size(), is(2));
  }

  @Test
  public void singleEdge() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/singleEdge.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(2));
  }

  @Test
  public void singleVertex() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(1));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(1));
  }

  @Test
  public void singleVertex2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex2.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(1));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(1));
  }

  @Test
  public void readInit() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/init/init.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getActions().size(), is(2));
  }

  @Test
  public void readLoginAndCrashModels() throws IOException {
    ContextFactory factory = new YEdContextFactory();
    List<Context> logins = factory.create(Paths.get("graphml/shared_state/Login.graphml"));
    List<Context> crashs = factory.create(Paths.get("graphml/shared_state/Crash.graphml"));
    for (RuntimeEdge edge : crashs.get(0).getModel().getEdges()) {
      Assert.assertNotNull(edge.getSourceVertex());
      Assert.assertNotNull(edge.getTargetVertex());
    }
  }

  @Test
  public void blockedBranch1() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedBranch1.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(3));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(3));
  }

  @Test
  public void blockedBranch2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedBranch2.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(3));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(4));
  }

  @Test
  public void blockedVertex1() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex1.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(2));
  }

  @Test
  public void blockedVertex2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex2.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(1));
  }

  @Test
  public void blockedVertex3() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex3.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(1));
  }

  @Test
  public void dryRun() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/UC01.graphml"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    contexts.get(0).setPathGenerator(new RandomPath(new VertexCoverage(100)));
    Machine machine = new SimpleMachine(contexts);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void writeFile() throws IOException {
    Vertex v_Start = new Vertex().setName("Start").setId("n0");
    Vertex v_BrowserStarted = new Vertex().setName("v_BrowserStarted").setId("n1");
    Vertex v_BaseURL = new Vertex().setName("v_BaseURL").setId("n2");
    Vertex v_SearchResult = new Vertex().setName("v_SearchResult").setId("n3");
    Vertex v_BrowserStopped = new Vertex().setName("v_BrowserStopped").setId("n4");
    Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5");
    Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
    Vertex v_ShoppingCart = new Vertex().setName("v_ShoppingCart").setId("n7");

    Model model = new Model();
    model.addEdge(new Edge().setSourceVertex(v_Start).setTargetVertex(v_BrowserStopped).setName("e_init")).setId("e0");
    model.addEdge(new Edge().setSourceVertex(v_BrowserStarted).setTargetVertex(v_BaseURL).setName("e_EnterBaseURL")).setId("e1");
    model.addEdge(new Edge().setSourceVertex(v_BaseURL).setTargetVertex(v_SearchResult).setName("e_SearchBook")).setId("e2");
    model.addEdge(new Edge().setSourceVertex(v_BrowserStopped).setTargetVertex(v_BrowserStarted).setName("e_StartBrowser")).setId("e3");
    model.addEdge(new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_BookInformation).setName("e_ClickBook")).setId("e4");
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart")).setId("e5");
    model.addEdge(new Edge().setSourceVertex(v_OtherBoughtBooks).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart")).setId("e6");
    model.addEdge(new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart")).setId("e7");
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart")).setId("e8");
    model.addEdge(new Edge().setSourceVertex(v_ShoppingCart).setTargetVertex(v_SearchResult).setName("e_SearchBook")).setId("e9");
    model.addEdge(new Edge().setSourceVertex(v_OtherBoughtBooks).setTargetVertex(v_SearchResult).setName("e_SearchBook")).setId("e10");
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_SearchResult).setName("e_SearchBook")).setId("e11");

    Context writeContext = new TestExecutionContext().setModel(model.build());
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    // Write the graphml file
    Path tmpFolder = testFolder.getRoot().toPath();
    new YEdContextFactory().write(writeContexts, tmpFolder);

    // Read the graphml file
    List<Context> readCContexts = new YEdContextFactory().create(tmpFolder);

    // Compare
    Assert.assertThat(writeContext.getModel().getVertices().size() - 1, // The start vertex is removed automatically
      is(readCContexts.get(0).getModel().getVertices().size()));
    Assert.assertThat(writeContext.getModel().getEdges().size(),
      is(readCContexts.get(0).getModel().getEdges().size()));
  }

  @Test
  public void createfromString() throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(ResourceUtils.getResourceAsStream("graphml/blocked/blockedVertex2.graphml"), writer, StandardCharsets.UTF_8);
    String grapmlStr = writer.toString();

    List<Context> contexts = new YEdContextFactory().create(grapmlStr);
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));

    Assert.assertThat(contexts.get(0).getModel().getVertices().size(), is(2));
    Assert.assertThat(contexts.get(0).getModel().getEdges().size(), is(1));
  }
  
  @Test
  public void customProperties_01() throws IOException{
    /*
     * node: 		y
     * edge: 		n
     * default: 	y
     * overRide: 	y
     * desc:		node: y edge: y
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph01.graphml");
    RuntimeModel m = ctx.getModel();

    for (RuntimeVertex v : m.getVertices()){
      assertGeometryPropertiesAreOk(v);
      Assert.assertTrue("Node description is missing.", v.hasProperty("description"));
      Assert.assertEquals("The description", v.getProperty("description").toString());
      Assert.assertTrue("CustomNodeProperty is not on Node.", v.hasProperty("CustomNodeProperty"));
      Assert.assertEquals("OverRideNodeValue", v.getProperty("CustomNodeProperty").toString());
    }

    for (RuntimeEdge e : m.getEdges()){
      Assert.assertTrue("Edge description is missing.", e.hasProperty("description"));
      Assert.assertEquals("The edge description", e.getProperty("description").toString());
      Assert.assertFalse("CustomNodeProperty is erroneously on Edge.", e.hasProperty("CustomNodeProperty"));
    }
  }
 
  @Test
  public void customProperties_02() throws IOException{
    /*
     * node: 		n
     * edge: 		y
     * default: 	n
     * overRide: 	n
     * desc:		node: n edge: n
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph02.graphml");
    RuntimeModel m = ctx.getModel();

    for (RuntimeVertex v : m.getVertices()){
      assertGeometryPropertiesAreOk(v);
      Assert.assertFalse("CustomEdgeProperty is erroneously on Node.", v.hasProperty("CustomEdgeProperty"));
      Assert.assertFalse("Description is erroneously on Node.", v.hasProperty("description"));
    }

    for (RuntimeEdge e : m.getEdges()){
      Assert.assertTrue("CustomEdgeProperty is missing from Edge.", e.hasProperty("CustomEdgeProperty"));
      Assert.assertEquals("", e.getProperty("CustomEdgeProperty").toString());
      Assert.assertFalse("Description is erroneously on Edge.", e.hasProperty("description"));
    }
  }
  
  @Test
  public void customProperties_03() throws IOException{
    /*
     * node: 		y
     * edge: 		y
     * default: 	y
     * overRide: 	n
     * desc:		node: n edge: n
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph03.graphml");
    RuntimeModel m = ctx.getModel();

    for (RuntimeVertex v : m.getVertices()){
      assertGeometryPropertiesAreOk(v);
      Assert.assertTrue("CustomNodeProperty is missing from Node.", v.hasProperty("CustomNodeProperty"));
      Assert.assertEquals("DefaultNodeValue", v.getProperty("CustomNodeProperty").toString());
      Assert.assertFalse("Description is erroneously on Node.", v.hasProperty("description"));
      Assert.assertFalse("CustomEdgeProperty is erroneously on Node.", v.hasProperty("CustomEdgeProperty"));
    }

    for (RuntimeEdge e : m.getEdges()){
      Assert.assertTrue("CustomEdgeProperty is missing from  Edge.", e.hasProperty("CustomEdgeProperty"));
      Assert.assertEquals("DefaultEdgeValue", e.getProperty("CustomEdgeProperty").toString());
      Assert.assertFalse("Description is erroneously on Edge.", e.hasProperty("description"));
      Assert.assertFalse("CustomNodeProperty is erroneously on Edge.", e.hasProperty("CustomNodeProperty"));
    }
  }

  @Test
  public void customProperties_04() throws IOException{
    /*
     * node: 		y
     * edge: 		y
     * default: 	n
     * overRide: 	y
     * desc:		node: n edge: n
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph04.graphml");
    RuntimeModel m = ctx.getModel();

    for (RuntimeVertex v : m.getVertices()){
      assertGeometryPropertiesAreOk(v);
      Assert.assertTrue("CustomNodeProperty is missing from Node.", v.hasProperty("CustomNodeProperty"));
      Assert.assertEquals("OverRideNodeValue", v.getProperty("CustomNodeProperty").toString());
      Assert.assertFalse("Description is erroneously on Node.", v.hasProperty("description"));
      Assert.assertFalse("CustomEdgeProperty is erroneously on Node.", v.hasProperty("CustomEdgeProperty"));
    }

    for (RuntimeEdge e : m.getEdges()){
      Assert.assertTrue("CustomEdgeProperty is missing from  Edge.", e.hasProperty("CustomEdgeProperty"));
      Assert.assertEquals("OverRideEdgeValue", e.getProperty("CustomEdgeProperty").toString());
      Assert.assertFalse("Description is erroneously on Edge.", e.hasProperty("description"));
      Assert.assertFalse("CustomNodeProperty is erroneously on Edge.", e.hasProperty("CustomNodeProperty"));
    }
  }

  @Test
  public void customProperties_05() throws IOException{
    /*
     * node: 		n
     * edge: 		n
     * default: 	n
     * overRide: 	n
     * desc:		node: y edge: y
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph05.graphml");
    RuntimeModel m = ctx.getModel();

    for (RuntimeVertex v : m.getVertices()){
      assertGeometryPropertiesAreOk(v);
      Assert.assertTrue("Node description is missing.", v.hasProperty("description"));
      Assert.assertEquals("Node Description", v.getProperty("description").toString());
      Assert.assertFalse("CustomNodeProperty is erroneously on Node.", v.hasProperty("CustomNodeProperty"));
      Assert.assertFalse("CustomEdgeProperty is erroneously on Node.", v.hasProperty("CustomEdgeProperty"));
    }

    for (RuntimeEdge e : m.getEdges()){
      Assert.assertTrue("Edge description is missing.", e.hasProperty("description"));
      Assert.assertEquals("Edge Description", e.getProperty("description").toString());
      Assert.assertFalse("CustomEdgeProperty is erroneously on  Edge.", e.hasProperty("CustomEdgeProperty"));
      Assert.assertFalse("CustomNodeProperty is erroneously on Edge.", e.hasProperty("CustomNodeProperty"));
    }
  }

  @Test
  public void customProperties_06() throws IOException{
    /*
     * node: 		n
     * edge: 		y
     * default: 	y
     * overRide: 	y
     * desc:		node: n edge: n
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph06.graphml");
    RuntimeModel m = ctx.getModel();

    for (RuntimeVertex v : m.getVertices()){
      assertGeometryPropertiesAreOk(v);
      Assert.assertFalse("Description is erroneously on Node.", v.hasProperty("description"));
      Assert.assertFalse("CustomEdgeProperty is erroneously on Node.", v.hasProperty("CustomEdgeProperty"));
    }

    for (RuntimeEdge e : m.getEdges()){
      Assert.assertTrue("CustomEdgeProperty is missing from Edge.", e.hasProperty("CustomEdgeProperty"));
      Assert.assertEquals("OverRideEdgeValue", e.getProperty("CustomEdgeProperty").toString());
      Assert.assertFalse("Description is erroneously on Edge.", e.hasProperty("description"));
    }
  }

  @Test
  public void customProperties_07() throws IOException{
    /*
     * Graph with a subgraph. Nodes and edges with 3 custom properties each.
     * Interestingly the node default values appear in yED but the <data key=xx> does not appear in the .graphml
     *
     * node: 		y
     * edge: 		y
     * default: 	y CNP 1 & CNP 3; CEP 1 & CEP 3
     * overRide: 	y CNP 2; CEP 2
     * desc:		y
     * edge:      y
     */
    Context ctx = getContextFromGraphml("graphml/customProp/Issue25_Graph07.graphml");
    RuntimeModel m = ctx.getModel();

    List<RuntimeVertex> vertices = m.getVertices();
    Assert.assertThat(vertices.size(),is(3));

    for (RuntimeVertex v : vertices){
      assertGeometryPropertiesAreOk(v);
      Assert.assertTrue("CustomNodeProperty 1 is missing from Node.", v.hasProperty("CNP 1"));
      Assert.assertEquals("Default Value 1", v.getProperty("CNP 1").toString());
      Assert.assertTrue("CustomNodeProperty 2 is missing from Node.", v.hasProperty("CNP 2"));
      Assert.assertEquals("OverRide 2", v.getProperty("CNP 2").toString());
      Assert.assertTrue("CustomNodeProperty 3 is missing from Node.", v.hasProperty("CNP 3"));
      Assert.assertEquals("OverRide 3", v.getProperty("CNP 3").toString());

      Assert.assertTrue("Description is missing from Node.", v.hasProperty("description"));
      Assert.assertEquals("Node Description", v.getProperty("description").toString());

      Assert.assertFalse("CustomEdgeProperty 1 is erroneously on Node.", v.hasProperty("CEP 1"));
      Assert.assertFalse("CustomEdgeProperty 2 is erroneously on Node.", v.hasProperty("CEP 2"));
      Assert.assertFalse("CustomEdgeProperty 3 is erroneously on Node.", v.hasProperty("CEP 3"));
    }

    List<RuntimeEdge> edges = m.getEdges();
    Assert.assertThat(edges.size(),is(2));

    for (RuntimeEdge e : edges){
      Assert.assertTrue("CustomEdgeProperty 1 is missing from Edge.", e.hasProperty("CEP 1"));
      Assert.assertEquals("Default Value 1", e.getProperty("CEP 1").toString());
      Assert.assertTrue("CustomEdgeProperty 2 is missing from Edge.", e.hasProperty("CEP 2"));
      Assert.assertEquals("OverRide 2", e.getProperty("CEP 2").toString());
      Assert.assertTrue("CustomEdgeProperty 3 is missing from Edge.", e.hasProperty("CEP 3"));
      Assert.assertEquals("OverRide 3", e.getProperty("CEP 3").toString());

      Assert.assertTrue("Description is missing from Edge.", e.hasProperty("description"));
      Assert.assertEquals("Edge Description", e.getProperty("description").toString());

      Assert.assertFalse("CustomNodeProperty 1 is erroneously on Node.", e.hasProperty("CNP 1"));
      Assert.assertFalse("CustomNodeProperty 2 is erroneously on Node.", e.hasProperty("CNP 2"));
      Assert.assertFalse("CustomNodeProperty 3 is erroneously on Node.", e.hasProperty("CNP 3"));
    }
  }
  
  private Context getContextFromGraphml(String relPathToFile) throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(ResourceUtils.getResourceAsStream(relPathToFile), writer, StandardCharsets.UTF_8);
    String grapmlStr = writer.toString();

    List<Context> contexts = new YEdContextFactory().create(grapmlStr);
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));
    return contexts.get(0);
  }
  
  private void assertGeometryPropertiesAreOk(RuntimeVertex v){
  	Assert.assertTrue("X is missing.", v.hasProperty("x"));
  	Assert.assertTrue(Double.valueOf(v.getProperty("x").toString()) > 0 );
  	
  	Assert.assertTrue("Y is missing.", v.hasProperty("y"));
  	Assert.assertTrue(Double.valueOf(v.getProperty("y").toString()) > 0 );
  }
  
}
