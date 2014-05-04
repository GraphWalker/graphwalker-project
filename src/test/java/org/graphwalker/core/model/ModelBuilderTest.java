package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
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

import org.graphwalker.core.model.efsm.Edge;
import org.graphwalker.core.model.efsm.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Nils Olsson
 */
public class ModelBuilderTest {


    @Test
    public void buildVertex() {
        Vertex vertex = new Vertex.Builder().build();
        Assert.assertThat(vertex, notNullValue());
    }

    @Test
    public void buildEdge() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        Edge edge = new Edge.Builder().setSource(vertex1).setTarget(vertex2).build();
        Assert.assertThat(edge, notNullValue());
        Assert.assertThat(edge.getSource(), notNullValue());
        Assert.assertThat(edge.getTarget(), notNullValue());
    }

    @Test
    public void buildEFSM() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        EFSM efsm = new EFSM.Builder()
                .add(new Edge.Builder().setSource(vertex1).setTarget(vertex2))
                .build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(1));
        Assert.assertThat(efsm.getVertices().size(), is(2));
    }


    @Test
    public void updateBuilder() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        Edge.Builder edge1 = new Edge.Builder().setSource(vertex1).setTarget(vertex2);
        EFSM.Builder efsm = new EFSM.Builder().add(edge1);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(1));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
        Edge.Builder edge2 = new Edge.Builder().setSource(vertex1).setTarget(vertex2);
        efsm.add(edge2);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(2));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
    }

    @Test
    public void singleVertex() {
        EFSM efsm = new EFSM.Builder().add(new Vertex.Builder()).build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(0));
        Assert.assertThat(efsm.getVertices().size(), is(1));
    }
}
