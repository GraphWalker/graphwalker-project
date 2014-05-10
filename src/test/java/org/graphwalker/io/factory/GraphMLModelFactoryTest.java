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
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class GraphMLModelFactoryTest {

    @Test(expected = ModelFactoryException.class)
    public void fileDoesNotExistsOnFileSystem() {
        GraphMLModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("graphml/LKHDIODSOSUBD.graphml");
    }
/*
    @Test
    public void fileExistsOnFileSystem() {
        GraphMLModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("src/test/resources/graphml/UC01.graphml");
        //Assert.assertThat("Number of vertices", model.getVertices().size(), is(8));
        //Assert.assertThat("Number of edges", model.getEdges().size(), is(12));
    }
*/

    @Test
    public void uc01() {
        GraphMLModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("graphml/UC01.graphml");
        int i = 0;
        //Assert.assertThat("Number of vertices", model.getVertices().size(), is(8));
        //Assert.assertThat("Number of edges", model.getEdges().size(), is(12));
    }
/*
    @Test
    public void efsmWithReqtags() {
        GraphMLModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("graphml/EFSM_with_REQTAGS.graphml");
        Assert.assertThat("Number of vertices", model.getVertices().size(), is(7));
        Assert.assertThat("Number of edges", model.getEdges().size(), is(19));
    }

    @Test
    public void guards() {
        GraphMLModelFactory factory = new GraphMLModelFactory();
        Model model = factory.create("graphml/Guards.graphml");
        Assert.assertThat("Number of vertices", model.getVertices().size(), is(3));
        Assert.assertThat("Number of edges", model.getEdges().size(), is(2));
        Vertex startVertex = model.getStartVertices().get(0);
        Edge edgeWithGuard = model.getEdges(startVertex).get(0);
        Assert.assertEquals(null, edgeWithGuard.getName());
        Assert.assertNotEquals(edgeWithGuard.getGuard(), new Guard("true"));
    }
   */
}
