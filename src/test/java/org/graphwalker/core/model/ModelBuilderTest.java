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

import static org.graphwalker.core.model.EFSM.ImmutableEFSM;
import static org.graphwalker.core.model.efsm.Edge.ImmutableEdge;
import static org.graphwalker.core.model.efsm.Vertex.ImmutableVertex;

import org.graphwalker.core.model.tree.Classification;
import org.junit.Assert;
import org.junit.Test;

import static org.graphwalker.core.model.tree.Classification.*;
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
/*
    @Test
    public void buildClassificationTree() {
        Classification classification = new ClassificationBuilder()
                .setName("classification")
                .addClassification(new ClassificationBuilder().setName("leaf1"))
                .addClassification(new ClassificationBuilder().setName("leaf2"))
                .build();
        Assert.assertThat(classification, notNullValue());
        Assert.assertThat(classification.getName(), is("classification"));
        Assert.assertThat(classification.getClassifications(), notNullValue());
        Assert.assertThat(classification.getClassifications().size(), is(2));
        Assert.assertThat(classification.getClassifications().get(0).getName(), is("leaf1"));
        Assert.assertThat(classification.getClassifications().get(0).getParent(), is(classification));
        Assert.assertThat(classification.getClassifications().get(1).getName(), is("leaf2"));
        Assert.assertThat(classification.getClassifications().get(1).getParent(), is(classification));
    }

    @Test
    public void buildLargeClassificationTree() {
        Classification classification = createClassification(2).build();
        int i = 0;
    }

    private ClassificationBuilder createClassification(int level) {
        ClassificationBuilder builder = new ClassificationBuilder();
        if (0 < level) {
            for (int i = 0; i < level; i++) {
                builder.addClassification(createClassification(level-1));
            }
        }
        return builder;
    }
*/
/*
    @Test
    public void buildClassificationTree() {
        ClassificationTree classificationTree = new ClassificationTreeBuilder().build();
        Assert.assertThat(classificationTree, notNullValue());
    }
*/
    /*
    @Test
    public void buildLargerClassificationTree() {
        ClassificationBuilder leaf1 = new ClassificationBuilder().setName("leaf1");
        ClassificationBuilder leaf2 = new ClassificationBuilder().setName("leaf2");
        ClassificationTree classificationTree = new ClassificationTreeBuilder()
                .addClassification(leaf1)
                .addClassification(leaf2)
                .build();
        Assert.assertThat(classificationTree, notNullValue());
        Assert.assertThat(classificationTree.getClassifications(), notNullValue());
        List<Classification> classifications = classificationTree.getClassifications();
        Assert.assertThat(classifications.size(), is(2));
        Assert.assertThat(classifications.get(0).getParent(), is(classificationTree.getRoot()));
        Assert.assertThat(classifications.get(0).getName(), is("leaf1"));
        Assert.assertThat(classifications.get(1).getName(), is("leaf2"));
    }
    */
}
