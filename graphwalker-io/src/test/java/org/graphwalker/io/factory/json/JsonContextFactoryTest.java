package org.graphwalker.io.factory.json;

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

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Edge.RuntimeEdge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.TestExecutionContext;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kristian Karl
 */
public class JsonContextFactoryTest {

  private static final Logger logger = LoggerFactory.getLogger(JsonContextFactoryTest.class);

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void smallModel() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/SmallModel.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(4));

    assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getName(), is("v_VerifySomeAction"));
    assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getId(), is("n0"));

    assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getName(), is("v_VerifySomeOtherAction"));
    assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getId(), is("n1"));
  }

  @Test
  public void smallModelWithSimpleMachine() throws IOException {
    SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/SmallModel.json")));
    while (machine.hasNextStep()) {
      logger.debug(machine.getNextStep().getCurrentElement().getName());
    }
  }

  @Test
  public void loginWithSimpleMachine() throws IOException {
    SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/Login.json")));
    while (machine.hasNextStep()) {
      logger.debug(machine.getNextStep().getCurrentElement().getName());
    }
  }

  @Test
  public void requirement() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/UC01.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    assertThat(context.getModel().findVertices("v_BaseURL").get(0).getRequirements().size(), is(1));
    Requirement requirement = context.getModel().findVertices("v_BaseURL").get(0).getRequirements().iterator().next();
    assertThat(requirement.getKey(), is("UC01 2.2.1"));
  }

  @Test
  public void property() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/UC01.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    assertThat(context.getModel().findVertices("v_BrowserStarted").get(0).getProperties().size(), is(1));
    String color = (String) context.getModel().findVertices("v_BrowserStarted").get(0).getProperty("color");
    assertTrue(color != null);
    assertThat(color, is("yellow"));
  }

  @Test
  public void acceptJsonTest() {
    ContextFactory factory = new JsonContextFactory();
    assertTrue(factory.accept(Paths.get("json/SmallModel.json")));
  }

  @Test(expected = ContextFactoryException.class)
  public void acceptJsonTestFailure() throws IOException {
    ContextFactory factory = new JsonContextFactory();
    factory.create(Paths.get("json/NonModel.json"));
  }

  @Test
  public void guard() throws IOException {
    Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5");
    Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
    Model model = new Model();
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5")
                      .setGuard(new Guard("num_of_books<=MAX_BOOKS")).addAction(new Action(" num_of_books++;")));

    Context writeContext = new TestExecutionContext();
    writeContext.setModel(model.build());
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    Path tmpFolder = testFolder.getRoot().toPath();
    ContextFactory factory = new JsonContextFactory();
    factory.write(writeContexts, tmpFolder);

    List<Context> readContexts = new JsonContextFactory().create(tmpFolder);
    assertNotNull(readContexts);
    assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    assertThat(model.getEdges().get(0).getGuard().getScript(), is(readContext.getModel().getEdges().get(0).getGuard().getScript()));
  }

  @Test
  public void actions() throws IOException {
    Vertex v_BrowserStopped = new Vertex().setName("v_BrowserStopped").setId("n4").addAction(new Action("num_of_books = 0"));
    Model model = new Model();
    model.addEdge(new Edge().setTargetVertex(v_BrowserStopped).setName("e_init").setId("e0").addAction(new Action(" init = true"))
                      .addAction(new Action(" MAX_BOOKS = 5;")));

    Context writeContext = new TestExecutionContext();
    writeContext.setModel(model.build());
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    Path tmpFolder = testFolder.getRoot().toPath();
    ContextFactory factory = new JsonContextFactory();
    factory.write(writeContexts, tmpFolder);

    List<Context> readContexts = new JsonContextFactory().create(tmpFolder);
    assertNotNull(readContexts);
    assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    assertThat(model.getActions().size(), is(readContext.getModel().getActions().size()));
    assertThat(model.getEdges().get(0).getActions().size(), is(readContext.getModel().getEdges().get(0).getActions().size()));
    assertThat(model.getVertices().get(0).getActions().size(), is(readContext.getModel().getVertices().get(0).getActions().size()));
  }

  @Test
  public void uc01() throws IOException {
    Vertex v_BrowserStarted = new Vertex().setName("v_BrowserStarted").setId("n1").setProperty("Color", "yellow");
    Vertex v_BaseURL = new Vertex().setName("v_BaseURL").setId("n2").addRequirement(new Requirement("UC01 2.2.1"));
    Vertex v_SearchResult = new Vertex().setName("v_SearchResult").setId("n3").addRequirement(new Requirement("UC01 2.2.2"));
    Vertex v_BrowserStopped = new Vertex().setName("v_BrowserStopped").setId("n4");
    Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5").addRequirement(new Requirement("UC01 2.2.3"));
    Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
    Vertex v_ShoppingCart = new Vertex().setName("v_ShoppingCart").setId("n7").addRequirement(new Requirement("UC01 2.3"));

    Model model = new Model();
    model.addEdge(new Edge().setTargetVertex(v_BrowserStopped).setName("e_init").setId("e0").addAction(new Action(" num_of_books = 0;"))
                      .addAction(new Action(" MAX_BOOKS = 5;")));
    model.addEdge(new Edge().setSourceVertex(v_BrowserStarted).setTargetVertex(v_BaseURL).setName("e_EnterBaseURL").setId("e1"));
    model.addEdge(new Edge().setSourceVertex(v_BaseURL).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e2"));
    model.addEdge(new Edge().setSourceVertex(v_BrowserStopped).setTargetVertex(v_BrowserStarted).setName("e_StartBrowser").setId("e3"));
    model.addEdge(new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_BookInformation).setName("e_ClickBook").setId("e4"));
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5")
                      .setGuard(new Guard("num_of_books<=MAX_BOOKS")).addAction(new Action(" num_of_books++;")));
    model.addEdge(new Edge().setSourceVertex(v_OtherBoughtBooks).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart").setId("e6"));
    model.addEdge(new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart").setId("e7"));
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart").setId("e8"));
    model.addEdge(new Edge().setSourceVertex(v_ShoppingCart).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e9"));
    model.addEdge(new Edge().setSourceVertex(v_OtherBoughtBooks).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e10"));
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e11"));

    Context writeContext = new TestExecutionContext();
    writeContext.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
    writeContext.setNextElement(writeContext.getModel().findElements("e_init").get(0));
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    Path tmpFolder = testFolder.getRoot().toPath();
    ContextFactory factory = new JsonContextFactory();
    factory.write(writeContexts, tmpFolder);

    List<Context> readContexts = new JsonContextFactory().create(tmpFolder);
    assertNotNull(readContexts);
    assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    assertThat(model.getVertices().size(), is(readContext.getModel().getVertices().size()));
    assertThat(model.getEdges().size(), is(readContext.getModel().getEdges().size()));
    assertThat(writeContext.getCurrentElement(), is(readContext.getCurrentElement()));
  }

  @Test
  public void petClinic() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/petClinic.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(5));
  }

  @Test
  public void petClinicWithSimpleMachine() throws IOException {
    SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/petClinic.json")));
    while (machine.hasNextStep()) {
      Element e = machine.getNextStep().getCurrentElement();
      logger.debug(e.getName());
    }
  }

  @Test
  public void acceptDependencyJsonTest() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/DependencyModel.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);

    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(4));

    RuntimeEdge e = (RuntimeEdge) context.getModel().getElementById("e0");
    assertThat(e.getDependency(), is(100));

    e = (RuntimeEdge) context.getModel().getElementById("e1");
    assertThat(e.getDependency(), is(100));

    e = (RuntimeEdge) context.getModel().getElementById("e2");
    assertThat(e.getDependency(), is(85));

    e = (RuntimeEdge) context.getModel().getElementById("e3");
    assertThat(e.getDependency(), is(15));
  }

  @Test
  public void weightWriteReadJsonTest() throws IOException {
    Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5");
    Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
    Model model = new Model();
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5").setWeight(0.5));

    Context writeContext = new TestExecutionContext();
    writeContext.setModel(model.build());
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    Path tmpFolder = testFolder.getRoot().toPath();
    ContextFactory factory = new JsonContextFactory();
    factory.write(writeContexts, tmpFolder);

    List<Context> readContexts = new JsonContextFactory().create(tmpFolder);
    assertNotNull(readContexts);
    assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    assertThat(model.getEdges().get(0).getWeight(), is(readContext.getModel().getEdges().get(0).getWeight()));
  }

  @Test
  public void defaultWeightReadJsonTest() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/DependencyModel.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);

    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(4));

    RuntimeEdge e = (RuntimeEdge) context.getModel().getElementById("e0");
    assertThat(e.getWeight(), is(0.));
  }

  @Test
  public void readProperties() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/ModelWithProperties.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);

    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(4));

    assertTrue(context.getModel().hasProperty("color"));
    assertThat(context.getModel().getProperty("color"), is("grey"));

    RuntimeEdge e = (RuntimeEdge) context.getModel().getElementById("e0");
    assertTrue(e.hasProperty("color"));
    assertThat(e.getProperty("color"), is("green"));

    Vertex.RuntimeVertex v = (Vertex.RuntimeVertex) context.getModel().getElementById("n0");
    assertTrue(v.hasProperty("color"));
    assertThat(v.getProperty("color"), is("yellow"));
  }

  @Test
  public void petClinicWithSeed() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/petClinicWithSeed.json"));
    assertThat(SingletonRandomGenerator.nextInt(), is(1553932502));
    assertThat(SingletonRandomGenerator.nextInt(), is(-2090749135));
    assertThat(SingletonRandomGenerator.nextInt(), is(-287790814));
  }
}
