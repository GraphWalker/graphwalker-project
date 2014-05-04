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

import static org.graphwalker.core.model.ClassificationTree.ClassificationTreeBuilder;
import static org.graphwalker.core.model.EFSM.EFSMBuilder;
import static org.graphwalker.core.model.efsm.Edge.EdgeBuilder;
import static org.graphwalker.core.model.efsm.Vertex.VertexBuilder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Nils Olsson
 */
public class ModelBuilderTest {


    @Test
    public void buildVertex() {
        Vertex vertex = new VertexBuilder().setName("test").build();
        Assert.assertThat(vertex, notNullValue());
        Assert.assertThat(vertex.getName(), is("test"));

    }

    @Test
    public void buildEdge() {
        VertexBuilder vertex1 = new VertexBuilder().setName("vertex1");
        VertexBuilder vertex2 = new VertexBuilder().setName("vertex2");
        Edge edge = new EdgeBuilder().setSourceVertex(vertex1).setTargetVertex(vertex2).setName("edge1").build();
        Assert.assertThat(edge, notNullValue());
        Assert.assertThat(edge.getSourceVertex(), notNullValue());
        Assert.assertThat(edge.getTargetVertex(), notNullValue());
        Assert.assertThat(edge.getSourceVertex().getName(), is("vertex1"));
        Assert.assertThat(edge.getTargetVertex().getName(), is("vertex2"));
        Assert.assertThat(edge.getName(), is("edge1"));
    }

    @Test
    public void buildEFSM() {
        VertexBuilder vertex1 = new VertexBuilder();
        VertexBuilder vertex2 = new VertexBuilder();
        EFSM efsm = new EFSMBuilder()
                .add(new EdgeBuilder().setSourceVertex(vertex1).setTargetVertex(vertex2))
                .build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(1));
        Assert.assertThat(efsm.getVertices().size(), is(2));
    }


    @Test
    public void updateBuilder() {
        VertexBuilder vertex1 = new VertexBuilder();
        VertexBuilder vertex2 = new VertexBuilder();
        EdgeBuilder edge1 = new EdgeBuilder().setSourceVertex(vertex1).setTargetVertex(vertex2);
        EFSMBuilder efsm = new EFSMBuilder().add(edge1);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(1));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
        EdgeBuilder edge2 = new EdgeBuilder().setSourceVertex(vertex1).setTargetVertex(vertex2);
        efsm.add(edge2);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(2));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
    }

    @Test
    public void singleVertex() {
        EFSM efsm = new EFSMBuilder().add(new VertexBuilder().setName("test")).build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(0));
        Assert.assertThat(efsm.getVertices().size(), is(1));
        Assert.assertThat(efsm.getVertices().get(0).getName(), is("test"));
    }

    @Test
    public void buildClassificationTree() {
        ClassificationTree classificationTree = new ClassificationTreeBuilder().build();
        Assert.assertThat(classificationTree, notNullValue());
    }
}
