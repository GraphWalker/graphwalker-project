package org.graphwalker.io.factory.yed;

/*
 * #%L
 * GraphWalker Input/Output
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

import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.yed.YEdContext;
import org.graphwalker.io.factory.yed.YEdContextFactory;
import org.graphwalker.io.factory.yed.YEdContextFactoryException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class YEdContextFactoryTest {

    @Test(expected = YEdContextFactoryException.class)
    public void fileDoesNotExistsOnFileSystem() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/LKHDIODSOSUBD.graphml"));
    }

    @Test
    public void shared() {
        Context sharedAContext = new YEdContextFactory().create(Paths.get("graphml/SharedA.graphml"));
        Assert.assertNotNull(sharedAContext);
        Assert.assertThat(sharedAContext.getModel().getVertices().size(), is(2));
        Assert.assertThat(sharedAContext.getModel().getEdges().size(), is(5));
        
        Context sharedBContext = new YEdContextFactory().create(Paths.get("graphml/SharedB.graphml"));
        Assert.assertNotNull(sharedBContext);
        Assert.assertThat(sharedBContext.getModel().getVertices().size(), is(2));
        Assert.assertThat(sharedBContext.getModel().getEdges().size(), is(1));
    }

    @Test
    public void login() {
        new YEdContextFactory().create(Paths.get("graphml/Login.graphml"));
    }

    @Test
    public void uc01() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/UC01.graphml"));

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

/*    @Test
    public void efsmWithReqtags() {
        ModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("graphml/EFSM_with_REQTAGS.graphml");
        Assert.assertThat("Number of vertices", model.getVertices().size(), is(7));
        Assert.assertThat("Number of edges", model.getEdges().size(), is(19));
    }

    @Test
    public void guards() {
        ModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("graphml/Guards.graphml");
        Assert.assertThat("Number of vertices", model.getVertices().size(), is(3));
        Assert.assertThat("Number of edges", model.getOutEdges().size(), is(2));
        Vertex startVertex = model.getStartVertices().get(0);
        Edge edgeWithGuard = model.getOutEdges(startVertex).get(0);
        Assert.assertEquals(null, edgeWithGuard.getName());
        Assert.assertNotEquals(edgeWithGuard.getGuard(), new Guard("true"));
    }
   */

    @Test
    public void singleEdge() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/singleEdge.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(2));
        Assert.assertThat(context.getModel().getEdges().size(), is(2));
    }

    @Test
    public void singleVertex() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(1));
        Assert.assertThat(context.getModel().getEdges().size(), is(1));
    }

    @Test
    public void singleVertex2() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex2.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(1));
        Assert.assertThat(context.getModel().getEdges().size(), is(1));
    }
}
