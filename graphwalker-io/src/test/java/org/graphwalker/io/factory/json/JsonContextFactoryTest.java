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
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;

/**
 * @author Kristian Karl
 */
public class JsonContextFactoryTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void smallModel() {
        Context context = new JsonContextFactory().create(Paths.get("json/SmallModel.json"));
        Assert.assertThat(context.getModel().getVertices().size(), is(2));
        Assert.assertThat(context.getModel().getEdges().size(), is(4));

        Assert.assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getName(), is("v_VerifySomeAction"));
        Assert.assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getId(), is("n0"));

        Assert.assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getName(), is("v_VerifySomeOtherAction"));
        Assert.assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getId(), is("n1"));
    }

    @Test
    public void smallModelSimpleMachine() {
        SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/SmallModel.json")));
        while (machine.hasNextStep()) {
            System.out.println(machine.getNextStep().getCurrentElement().getName());
        }
    }

    @Test
    public void acceptJsonTest() {
        ContextFactory factory = new JsonContextFactory();
        Assert.assertTrue(factory.accept(Paths.get("json/SmallModel.json")));
    }

    @Test(expected = JSONException.class)
    public void acceptJsonTestFailure() {
        ContextFactory factory = new JsonContextFactory();
        factory.create(Paths.get("json/NonModel.json"));
        Assert.fail("Expected a JSONException");
    }

    @Test
    public void guard() throws IOException {
        Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5");
        Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
        Model model = new Model();
        model.addEdge( new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5").setGuard(new Guard("num_of_books<=MAX_BOOKS")).addAction(new Action(" num_of_books++;")));

        Context writeContext = new TestExecutionContext();
        writeContext.setModel(model.build());
        File testFile = testFolder.newFile("guard.json");
        ContextFactory factory = new JsonContextFactory();
        factory.write(writeContext, testFile.toPath());

        Context readContext = new JsonContextFactory().create(testFile.toPath());
        Assert.assertThat(model.getEdges().get(0).getGuard().getScript(), is(readContext.getModel().getEdges().get(0).getGuard().getScript()));
    }

    @Test
    public void actions() throws IOException {
        Vertex v_BrowserStopped = new Vertex().setName("v_BrowserStopped").setId("n4");
        Model model = new Model();
        model.addEdge( new Edge().setTargetVertex(v_BrowserStopped).setName("e_init").setId("e0").addAction(new Action(" num_of_books = 0;")).addAction(new Action(" MAX_BOOKS = 5;")));

        Context writeContext = new TestExecutionContext();
        writeContext.setModel(model.build());
        File testFile = testFolder.newFile("actions.json");
        ContextFactory factory = new JsonContextFactory();
        factory.write(writeContext, testFile.toPath());

        Context readContext = new JsonContextFactory().create(testFile.toPath());
        Assert.assertThat(model.getEdges().get(0).getActions().size(), is(readContext.getModel().getEdges().get(0).getActions().size()));
    }

    @Test
    public void uc01() throws IOException {
        Vertex v_BrowserStarted = new Vertex().setName("v_BrowserStarted").setId("n1");
        Vertex v_BaseURL = new Vertex().setName("v_BaseURL").setId("n2");
        Vertex v_SearchResult = new Vertex().setName("v_SearchResult").setId("n3");
        Vertex v_BrowserStopped = new Vertex().setName("v_BrowserStopped").setId("n4");
        Vertex v_BookInformation = new Vertex().setName("v_BookInformation").setId("n5");
        Vertex v_OtherBoughtBooks = new Vertex().setName("v_OtherBoughtBooks").setId("n6");
        Vertex v_ShoppingCart = new Vertex().setName("v_ShoppingCart").setId("n7");

        Model model = new Model();
        model.addEdge( new Edge().setTargetVertex(v_BrowserStopped).setName("e_init").setId("e0").addAction(new Action(" num_of_books = 0;")).addAction(new Action(" MAX_BOOKS = 5;")));
        model.addEdge( new Edge().setSourceVertex(v_BrowserStarted).setTargetVertex(v_BaseURL).setName("e_EnterBaseURL").setId("e1"));
        model.addEdge( new Edge().setSourceVertex(v_BaseURL).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e2"));
        model.addEdge( new Edge().setSourceVertex(v_BrowserStopped).setTargetVertex(v_BrowserStarted).setName("e_StartBrowser").setId("e3"));
        model.addEdge( new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_BookInformation).setName("e_ClickBook").setId("e4"));
        model.addEdge( new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_OtherBoughtBooks).setName("e_AddBookToCart").setId("e5").setGuard(new Guard("num_of_books<=MAX_BOOKS")).addAction(new Action(" num_of_books++;")));
        model.addEdge( new Edge().setSourceVertex(v_OtherBoughtBooks).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart").setId("e6"));
        model.addEdge( new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart").setId("e7"));
        model.addEdge( new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_ShoppingCart).setName("e_ShoppingCart").setId("e8"));
        model.addEdge( new Edge().setSourceVertex(v_ShoppingCart).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e9"));
        model.addEdge( new Edge().setSourceVertex(v_OtherBoughtBooks).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e10"));
        model.addEdge( new Edge().setSourceVertex(v_BookInformation).setTargetVertex(v_SearchResult).setName("e_SearchBook").setId("e11"));

        Context writeContext = new TestExecutionContext();
        writeContext.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
        writeContext.setNextElement(writeContext.getModel().findElements("e_init").get(0));

        File testFile = testFolder.newFile("uc01.json");
        ContextFactory factory = new JsonContextFactory();
        factory.write(writeContext, testFile.toPath());

        Context readContext = new JsonContextFactory().create(testFile.toPath());

        Assert.assertThat(model.getVertices().size(), is(readContext.getModel().getVertices().size()));
        Assert.assertThat(model.getEdges().size(), is(readContext.getModel().getEdges().size()));
        Assert.assertThat(writeContext.getCurrentElement(), is(readContext.getCurrentElement()));
    }
}
