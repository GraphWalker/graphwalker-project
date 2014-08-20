package org.graphwalker.io.factory;

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

import org.graphwalker.core.model.Model;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class YEdModelFactoryTest {

    @Test(expected = ModelFactoryException.class)
    public void fileDoesNotExistsOnFileSystem() {
        ModelFactory factory = new YEdModelFactory();
        Model model = factory.create("graphml/LKHDIODSOSUBD.graphml");
    }

    @Test
    public void shared() {
        ModelFactory factory = new YEdModelFactory();
        Model sharedA = factory.create("graphml/SharedA.graphml");
        Assert.assertNotNull(sharedA);
        Assert.assertThat(sharedA.getVertices().size(), is(2));
        Assert.assertThat(sharedA.getEdges().size(), is(5));
        Model sharedB = factory.create("graphml/SharedB.graphml");
        Assert.assertNotNull(sharedB);
        Assert.assertThat(sharedB.getVertices().size(), is(2));
        Assert.assertThat(sharedB.getEdges().size(), is(1));
    }

    @Test
    public void login() {
        ModelFactory factory = new YEdModelFactory();
        Model model = factory.create("graphml/Login.graphml");
    }

    @Test
    public void uc01() {
        ModelFactory factory = new YEdModelFactory();
        Model.RuntimeModel model = factory.create("graphml/UC01.graphml").build();

        // Since the model id the Model.RuntimeModel,the Start vertex is removed from the graph.
        Assert.assertThat(model.getVertices().size(), is(7));
        Assert.assertThat(model.getEdges().size(), is(12));
        Assert.assertThat(model.findEdges("e_init").size(), is(1));
        Assert.assertThat(model.findEdges("e_AddBookToCart").size(), is(1));
        Assert.assertThat(model.findEdges("e_ClickBook").size(), is(1));
        Assert.assertThat(model.findEdges("e_EnterBaseURL").size(), is(1));
        Assert.assertThat(model.findEdges("e_SearchBook").size(), is(4));
        Assert.assertThat(model.findEdges("e_ShoppingCart").size(), is(3));
        Assert.assertThat(model.findEdges("e_StartBrowser").size(), is(1));
        Assert.assertNull(model.findEdges(""));

        Assert.assertThat(model.findVertices("v_BaseURL").size(), is(1));
        Assert.assertThat(model.findVertices("v_BookInformation").size(), is(1));
        Assert.assertThat(model.findVertices("v_BrowserStarted").size(), is(1));
        Assert.assertThat(model.findVertices("v_BrowserStopped").size(), is(1));
        Assert.assertThat(model.findVertices("v_OtherBoughtBooks").size(), is(1));
        Assert.assertThat(model.findVertices("v_SearchResult").size(), is(1));
        Assert.assertThat(model.findVertices("v_ShoppingCart").size(), is(1));

        Assert.assertThat(model.findEdges("e_init").get(0).getSourceVertex().isStartVertex(), is(true));
        Assert.assertThat(model.findEdges("e_init").get(0).getTargetVertex().getName(), is("v_BrowserStopped"));

        Assert.assertThat(model.findEdges("e_StartBrowser").get(0).getSourceVertex().getName(), is("v_BrowserStopped"));
        Assert.assertThat(model.findEdges("e_StartBrowser").get(0).getTargetVertex().getName(), is("v_BrowserStarted"));
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
        ModelFactory factory = new YEdModelFactory();
        Model model = factory.create("graphml/blocked/singleEdge.graphml");
        Assert.assertThat(model.getVertices().size(), is(3));
        Assert.assertThat(model.getEdges().size(), is(2));
    }

    @Test
    public void singleVertex() {
        ModelFactory factory = new YEdModelFactory();
        Model model = factory.create("graphml/blocked/singleVertex.graphml");
        Assert.assertThat(model.getVertices().size(), is(2));
        Assert.assertThat(model.getEdges().size(), is(1));
    }

    @Test
    public void singleVertex2() {
        ModelFactory factory = new YEdModelFactory();
        Model model = factory.create("graphml/blocked/singleVertex2.graphml");
        Assert.assertThat(model.getVertices().size(), is(2));
        Assert.assertThat(model.getEdges().size(), is(1));
    }
}
