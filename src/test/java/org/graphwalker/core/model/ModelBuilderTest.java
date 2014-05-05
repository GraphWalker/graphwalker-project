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
import org.graphwalker.core.model.tree.Classification;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.graphwalker.core.model.ClassificationTree.ImmutableClassificationTree;
import static org.graphwalker.core.model.EFSM.ImmutableEFSM;
import static org.graphwalker.core.model.efsm.Edge.ImmutableEdge;
import static org.graphwalker.core.model.efsm.Vertex.ImmutableVertex;
import static org.graphwalker.core.model.tree.Classification.ImmutableClassification;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Nils Olsson
 */
public class ModelBuilderTest {

    @Test
    public void buildVertex() {
        ImmutableVertex vertex = new Vertex().setName("test").build();
        Assert.assertThat(vertex, notNullValue());
        Assert.assertThat(vertex.getName(), is("test"));
    }

    @Test
    public void buildEdge() {
        Vertex vertex1 = new Vertex().setName("vertex1");
        Vertex vertex2 = new Vertex().setName("vertex2");
        ImmutableEdge edge = new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2).setName("edge1").build();
        Assert.assertThat(edge, notNullValue());
        Assert.assertThat(edge.getSourceVertex(), notNullValue());
        Assert.assertThat(edge.getTargetVertex(), notNullValue());
        Assert.assertThat(edge.getSourceVertex().getName(), is("vertex1"));
        Assert.assertThat(edge.getTargetVertex().getName(), is("vertex2"));
        Assert.assertThat(edge.getName(), is("edge1"));
    }

    @Test
    public void buildEFSM() {
        Vertex vertex1 = new Vertex();
        Vertex vertex2 = new Vertex();
        ImmutableEFSM efsm = new EFSM()
                .addEdge(new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2))
                .build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(1));
        Assert.assertThat(efsm.getVertices().size(), is(2));
    }


    @Test
    public void updateBuilder() {
        Vertex vertex1 = new Vertex();
        Vertex vertex2 = new Vertex();
        Edge edge1 = new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2);
        EFSM efsm = new EFSM().addEdge(edge1);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(1));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
        Edge edge2 = new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2);
        efsm.addEdge(edge2);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(2));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
    }

    @Test
    public void singleVertex() {
        ImmutableEFSM efsm = new EFSM().addVertex(new Vertex().setName("test")).build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(0));
        Assert.assertThat(efsm.getVertices().size(), is(1));
        Assert.assertThat(efsm.getVertices().get(0).getName(), is("test"));
    }

    @Test
    public void buildClassification() {
        ImmutableClassification classification = new Classification()
                .setName("classification")
                .build();
        Assert.assertThat(classification, notNullValue());
        Assert.assertThat(classification.getName(), is("classification"));
    }

    @Test
    public void buildClassificationWithLeafs() {
        ImmutableClassification classification = new Classification()
                .setName("classification")
                .addClassification(new Classification().setName("leaf1"))
                .addClassification(new Classification().setName("leaf2"))
                .build();
        Assert.assertThat(classification, notNullValue());
        Assert.assertThat(classification.getName(), is("classification"));
        Assert.assertThat(classification.getClassifications(), notNullValue());
        Assert.assertThat(classification.getClassifications().size(), is(2));
    }

    @Test
    public void buildClassificationTree() {
        ImmutableClassificationTree classificationTree = new ClassificationTree().build();
        Assert.assertThat(classificationTree, notNullValue());
        Assert.assertThat(classificationTree.getRoot(), notNullValue());
        Assert.assertThat(classificationTree.getRoot().getClassifications(), notNullValue());
        Assert.assertThat(classificationTree.getRoot().getClassifications().size(), is(0));
    }

    @Test
    public void buildLargerClassificationTree() {
        Classification leaf1 = new Classification().setName("leaf1");
        Classification leaf2 = new Classification().setName("leaf2");
        ImmutableClassificationTree classificationTree = new ClassificationTree()
                .addClassification(leaf1)
                .addClassification(leaf2)
                .build();
        Assert.assertThat(classificationTree, notNullValue());
        Assert.assertThat(classificationTree.getRoot(), notNullValue());
        List<ImmutableClassification> classifications = classificationTree.getRoot().getClassifications();
        Assert.assertThat(classifications.size(), is(2));
    }

}
