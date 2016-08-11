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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.graphwalker.io.TestExecutionContext;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;

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
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    Assert.assertThat(context.getModel().getVertices().size(), is(2));
    Assert.assertThat(context.getModel().getEdges().size(), is(4));

    Assert.assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getName(), is("v_VerifySomeAction"));
    Assert.assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getId(), is("n0"));

    Assert.assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getName(), is("v_VerifySomeOtherAction"));
    Assert.assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getId(), is("n1"));
  }

  @Test
  public void SmallModelWithSimpleMachine() throws IOException {
    SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/SmallModel.json")));
    while (machine.hasNextStep()) {
      logger.debug(machine.getNextStep().getCurrentElement().getName());
    }
  }

  @Test
  public void UC01WithSimpleMachine() throws IOException {
    SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/UC01.json")));
    while (machine.hasNextStep()) {
      logger.debug(machine.getNextStep().getCurrentElement().getName());
    }
  }

  @Test
  public void requirement() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/UC01.json"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    Assert.assertThat(context.getModel().findVertices("v_BaseURL").get(0).getRequirements().size(), is(1));
    Requirement requirement = context.getModel().findVertices("v_BaseURL").get(0).getRequirements().iterator().next();
    Assert.assertThat(requirement.getKey(), is("UC01 2.2.1"));
  }

  @Test
  public void property() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/UC01.json"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);

    Assert.assertThat(context.getModel().findVertices("v_BrowserStarted").get(0).getProperties().size(), is(1));
    String color = (String) context.getModel().findVertices("v_BrowserStarted").get(0).getProperty("color");
    Assert.assertTrue(color != null);
    Assert.assertThat(color, is("yellow"));
  }

  @Test
  public void acceptJsonTest() {
    ContextFactory factory = new JsonContextFactory();
    Assert.assertTrue(factory.accept(Paths.get("json/SmallModel.json")));
  }

  @Test(expected=ContextFactoryException.class)
  public void acceptJsonTestFailure() throws IOException {
    ContextFactory factory = new JsonContextFactory();
    factory.create(Paths.get("json/NonModel.json"));
  }

  @Test
  public void guard() throws IOException {
    Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5");
    Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
    Model model = new Model();
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5").setGuard(new Guard("num_of_books<=MAX_BOOKS")).addAction(new Action(" num_of_books++;")));

    Context writeContext = new TestExecutionContext();
    writeContext.setModel(model.build());
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    Path tmpFolder = testFolder.getRoot().toPath();
    ContextFactory factory = new JsonContextFactory();
    factory.write(writeContexts, tmpFolder);

    List<Context> readContexts = new JsonContextFactory().create(tmpFolder);
    Assert.assertNotNull(readContexts);
    Assert.assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    Assert.assertThat(model.getEdges().get(0).getGuard().getScript(), is(readContext.getModel().getEdges().get(0).getGuard().getScript()));
  }

  @Test
  public void actions() throws IOException {
    Vertex v_BrowserStopped = new Vertex().setName("v_BrowserStopped").setId("n4");
    Model model = new Model();
    model.addEdge(new Edge().setTargetVertex(v_BrowserStopped).setName("e_init").setId("e0").addAction(new Action(" num_of_books = 0;")).addAction(new Action(" MAX_BOOKS = 5;")));

    Context writeContext = new TestExecutionContext();
    writeContext.setModel(model.build());
    List<Context> writeContexts = new ArrayList<>();
    writeContexts.add(writeContext);

    Path tmpFolder = testFolder.getRoot().toPath();
    ContextFactory factory = new JsonContextFactory();
    factory.write(writeContexts, tmpFolder);

    List<Context> readContexts = new JsonContextFactory().create(tmpFolder);
    Assert.assertNotNull(readContexts);
    Assert.assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    Assert.assertThat(model.getEdges().get(0).getActions().size(), is(readContext.getModel().getEdges().get(0).getActions().size()));
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
    model.addEdge(new Edge().setTargetVertex(v_BrowserStopped).setName("e_init").setId("e0").addAction(new Action(" num_of_books = 0;")).addAction(new Action(" MAX_BOOKS = 5;")));
    model.addEdge(new Edge().setSourceVertex(v_BrowserStarted).setTargetVertex(v_BaseURL).setName("e_EnterBaseURL").setId("e1"));
    model.addEdge(new Edge().setSourceVertex(v_BaseURL).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e2"));
    model.addEdge(new Edge().setSourceVertex(v_BrowserStopped).setTargetVertex(v_BrowserStarted).setName("e_StartBrowser").setId("e3"));
    model.addEdge(new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_BookInformation).setName("e_ClickBook").setId("e4"));
    model.addEdge(new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5").setGuard(new Guard("num_of_books<=MAX_BOOKS")).addAction(new Action(" num_of_books++;")));
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
    Assert.assertNotNull(readContexts);
    Assert.assertThat(readContexts.size(), is(1));
    Context readContext = readContexts.get(0);

    Assert.assertThat(model.getVertices().size(), is(readContext.getModel().getVertices().size()));
    Assert.assertThat(model.getEdges().size(), is(readContext.getModel().getEdges().size()));
    Assert.assertThat(writeContext.getCurrentElement(), is(readContext.getCurrentElement()));
  }

  @Test
  public void PetClinic() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/petClinic.json"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(5));
  }

  @Test
  public void PetClinicWithSimpleMachine() throws IOException {
    SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/petClinic.json")));
    while (machine.hasNextStep()) {
      Element e = machine.getNextStep().getCurrentElement();
      logger.debug(e.getName());
    }
  }
  
  @Test
  public void acceptDependencyJsonTest() throws IOException {
	    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/DependencyModel.json"));
	    Assert.assertNotNull(contexts);
	    Assert.assertThat(contexts.size(), is(1));

	    Context context = contexts.get(0);

	    Assert.assertThat(context.getModel().getVertices().size(), is(2));
	    Assert.assertThat(context.getModel().getEdges().size(), is(4));

	    Edge.RuntimeEdge e = (Edge.RuntimeEdge)context.getModel().getElementById("e0");
	    Assert.assertThat(e.getDependency(), is(1d));

	    e = (Edge.RuntimeEdge)context.getModel().getElementById("e1");
	    Assert.assertThat(e.getDependency(), is(1d));

	    e = (Edge.RuntimeEdge)context.getModel().getElementById("e2");
	    Assert.assertThat(e.getDependency(), is(0.85d));

	    e = (Edge.RuntimeEdge)context.getModel().getElementById("e3");
	    Assert.assertThat(e.getDependency(), is(0.15d));
  }
}
