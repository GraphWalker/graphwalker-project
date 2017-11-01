package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.graphwalker.core.model.Model.RuntimeModel;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ModelTest {

  @Test
  public void getElementById() throws Exception {
    Vertex v1 = new Vertex().setId("ONE");
    Vertex v2 = new Vertex().setId("TWO");
    Edge e1 = new Edge().setId("THREE");
    Edge e2 = new Edge().setId("FOUR");
    RuntimeModel model = new Model().addEdge(e1.setSourceVertex(v1).setTargetVertex(v2))
        .addEdge(e2.setSourceVertex(v1).setTargetVertex(v2)).build();
    Element element = model.getElementById("THREE");
    assertTrue(element.equals(e1.build()));
  }

  @Test
  public void buildVertex() throws Exception {
    Vertex vertex = new Vertex().setName("test");
    assertThat(vertex, notNullValue());
    assertThat(vertex.getName(), is("test"));
    assertThat(vertex.build(), notNullValue());
    assertThat(vertex.build().getName(), is("test"));
  }

  @Test
  public void buildEdge() throws Exception {
    Vertex vertex1 = new Vertex().setName("vertex1");
    Vertex vertex2 = new Vertex().setName("vertex2");
    Edge edge = new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2).setName("edge1");
    assertThat(edge, notNullValue());
    assertThat(edge.getSourceVertex(), notNullValue());
    assertThat(edge.getTargetVertex(), notNullValue());
    assertThat(edge.getSourceVertex().getName(), is("vertex1"));
    assertThat(edge.getTargetVertex().getName(), is("vertex2"));
    assertThat(edge.getName(), is("edge1"));
    assertThat(edge.build(), notNullValue());
    assertThat(edge.build().getSourceVertex(), notNullValue());
    assertThat(edge.build().getTargetVertex(), notNullValue());
    assertThat(edge.build().getSourceVertex().getName(), is("vertex1"));
    assertThat(edge.build().getTargetVertex().getName(), is("vertex2"));
    assertThat(edge.build().getName(), is("edge1"));
  }

  @Test
  public void buildEFSM() throws Exception {
    Model efsm = new Model().addEdge(new Edge().setSourceVertex(new Vertex()).setTargetVertex(new Vertex()));
    assertThat(efsm, notNullValue());
    assertThat(efsm.getEdges().size(), is(1));
    assertThat(efsm.getVertices().size(), is(2));
    assertThat(efsm.build(), notNullValue());
    assertThat(efsm.build().getEdges().size(), is(1));
    assertThat(efsm.build().getVertices().size(), is(2));
  }

  @Test
  public void deleteElement() throws Exception {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge();
    Edge e2 = new Edge();
    Model model = new Model().addEdge(e1.setSourceVertex(v1).setTargetVertex(v2))
        .addEdge(e2.setSourceVertex(v1).setTargetVertex(v2));
    assertThat(model, notNullValue());
    assertThat(model.getEdges().size(), is(2));
    assertThat(model.getVertices().size(), is(2));

    model.deleteEdge(e1);
    assertThat(model, notNullValue());
    assertThat(model.getEdges().size(), is(1));
    assertThat(model.getVertices().size(), is(2));

    model.deleteVertex(v2);
    assertThat(model, notNullValue());
    assertThat(model.getEdges().size(), is(0));
    assertThat(model.getVertices().size(), is(1));

    model.addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v1));
    model.addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));
    model.addEdge(new Edge().setSourceVertex(v2).setTargetVertex(v1));
    assertThat(model.getEdges().size(), is(3));
    assertThat(model.getVertices().size(), is(2));
    model.deleteVertex(v2);
    assertThat(model.getEdges().size(), is(1));
    assertThat(model.getVertices().size(), is(1));
  }

  @Test
  public void deleteVertex() throws Exception {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Vertex v3 = new Vertex();
    Edge e1 = new Edge();
    Edge e2 = new Edge();
    Edge e3 = new Edge();
    Edge e4 = new Edge();
    Edge e5 = new Edge();
    Edge e6 = new Edge();
    Model model = new Model().addEdge(e1.setSourceVertex(v1).setTargetVertex(v2))
        .addEdge(e2.setSourceVertex(v1).setTargetVertex(v3))
        .addEdge(e3.setSourceVertex(v1).setTargetVertex(v1))
        .addEdge(e4.setSourceVertex(v2).setTargetVertex(v2))
        .addEdge(e5.setSourceVertex(v3).setTargetVertex(v3))
        .addEdge(e6.setSourceVertex(v3).setTargetVertex(v1));
    assertThat(model, notNullValue());
    assertThat(model.getVertices().size(), is(3));
    assertThat(model.getEdges().size(), is(6));

    model.deleteVertex(v2);
    assertThat(model, notNullValue());
    assertThat(model.getVertices().size(), is(2));
    assertThat(model.getEdges().size(), is(4));
  }

  @Test
  public void recreateModel() throws Exception {
    Model model1 = new Model()
      .addAction(new Action("test"))
      .addRequirement(new Requirement("REQ1"))
      .setProperty("key", "value").setId("m1")
      .addEdge(new Edge().setSourceVertex(new Vertex()).setTargetVertex(new Vertex()));
    Model model2 = new Model(model1.build());
    assertThat(model1.getId(), is(model2.getId()));
    assertThat(model1.getName(), is(model2.getName()));
    assertThat(model1.getProperties(), is(model2.getProperties()));
    assertThat(model1.getRequirements(), is(model2.getRequirements()));
    assertThat(model1.getActions(), is(model2.getActions()));
    assertThat(model1.build(), is(model2.build()));
  }

  @Test
  public void updateBuilder() throws Exception {
    Vertex vertex1 = new Vertex();
    Vertex vertex2 = new Vertex();
    Edge edge1 = new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2);
    Model efsm = new Model().addEdge(edge1);
    assertThat(efsm.build(), notNullValue());
    assertThat(efsm.build().getEdges().size(), is(1));
    assertThat(efsm.build().getVertices().size(), is(2));
    Edge edge2 = new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2);
    efsm.addEdge(edge2);
    assertThat(efsm.build(), notNullValue());
    assertThat(efsm.build().getEdges().size(), is(2));
    assertThat(efsm.build().getVertices().size(), is(2));
  }

  @Test
  public void singleVertex() throws Exception {
    Model efsm = new Model().addVertex(new Vertex().setName("test"));
    assertThat(efsm, notNullValue());
    assertThat(efsm.getEdges().size(), is(0));
    assertThat(efsm.getVertices().size(), is(1));
    assertThat(efsm.build(), notNullValue());
    assertThat(efsm.build().getEdges().size(), is(0));
    assertThat(efsm.build().getVertices().size(), is(1));
    assertThat(efsm.build().getVertices().get(0).getName(), is("test"));
  }

  @Test
  public void buildClassification() throws Exception {
    Classification classification = new Classification().setName("classification");
    assertThat(classification, notNullValue());
    assertThat(classification.getName(), is("classification"));
    assertThat(classification.build(), notNullValue());
    assertThat(classification.build().getName(), is("classification"));
  }

  @Test
  public void buildClassificationWithLeafs() throws Exception {
    Classification classification = new Classification()
        .setName("classification")
        .addClassification(new Classification().setName("leaf1"))
        .addClassification(new Classification().setName("leaf2"));
    assertThat(classification, notNullValue());
    assertThat(classification.getName(), is("classification"));
    assertThat(classification.getClassifications(), notNullValue());
    assertThat(classification.getClassifications().size(), is(2));
    assertThat(classification.build(), notNullValue());
    assertThat(classification.build().getName(), is("classification"));
    assertThat(classification.build().getClassifications(), notNullValue());
    assertThat(classification.build().getClassifications().size(), is(2));
  }

  @Test
  public void buildClassificationTree() throws Exception {
    ClassificationTree classificationTree = new ClassificationTree();
    assertThat(classificationTree, notNullValue());
    assertThat(classificationTree.getRoot(), notNullValue());
    assertThat(classificationTree.getRoot().getClassifications(), notNullValue());
    assertThat(classificationTree.getRoot().getClassifications().size(), is(0));
    assertThat(classificationTree.build(), notNullValue());
    assertThat(classificationTree.build().getRoot(), notNullValue());
    assertThat(classificationTree.build().getRoot().getClassifications(), notNullValue());
    assertThat(classificationTree.build().getRoot().getClassifications().size(), is(0));
  }

  @Test
  public void buildLargerClassificationTree() throws Exception {
    Classification leaf1 = new Classification().setName("leaf1");
    Classification leaf2 = new Classification().setName("leaf2");
    ClassificationTree classificationTree = new ClassificationTree()
        .addClassification(leaf1)
        .addClassification(leaf2);
    assertThat(classificationTree, notNullValue());
    assertThat(classificationTree.getRoot(), notNullValue());
    assertThat(classificationTree.getRoot().getClassifications(), notNullValue());
    assertThat(classificationTree.getRoot().getClassifications().size(), is(2));
    assertThat(classificationTree.build(), notNullValue());
    assertThat(classificationTree.build().getRoot(), notNullValue());
    assertThat(classificationTree.build().getRoot().getClassifications().size(), is(2));
  }

  @Test
  public void createLargeModel() throws Exception {
    final long startTime = System.nanoTime();
    final Random random = new Random(System.nanoTime());
    final Model model = new Model();
    for (int i = 0; i < 50000; i++) {
      model.addVertex(new Vertex());
    }
    for (int i = 0; i < 50000; i++) {
      int source = random.nextInt(model.getVertices().size());
      int target = random.nextInt(model.getVertices().size());
      model.addEdge(new Edge()
                        .setSourceVertex(model.getVertices().get(source))
                        .setTargetVertex(model.getVertices().get(target)));
    }
    final long stopTime = System.nanoTime();
    RuntimeModel runtimeModel = model.build();
    assertThat(model.getVertices().size(), is(50000));
    assertThat(model.getEdges().size(), is(50000));
    assertThat(runtimeModel.getVertices().size(), is(50000));
    assertThat(runtimeModel.getEdges().size(), is(50000));
    assertTrue(TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS) < 30000);
  }

  @Test
  public void modelWithSharedState() throws Exception {
    Model model = new Model();
    assertFalse(model.build().hasSharedStates());
    assertFalse(model.build().hasSharedState("SHARED"));
    assertTrue(model.build().getSharedStates().isEmpty());
    assertTrue(model.addVertex(new Vertex().setSharedState("SHARED")).build().hasSharedStates());
    assertTrue(model.addVertex(new Vertex().setSharedState("SHARED")).build().hasSharedState("SHARED"));
    assertFalse(model.build().getSharedStates().isEmpty());
  }

  @Test
  public void modelWithAction() throws Exception {
    Model model = new Model();
    assertFalse(model.build().hasActions());
    assertTrue(model.build().getActions().isEmpty());
    assertTrue(model.addAction(new Action("")).build().hasActions());
    assertTrue(model.addActions(new Action("")).build().hasActions());
    assertTrue(model.setActions(Arrays.asList(new Action(""))).build().hasActions());
    assertFalse(model.build().getActions().isEmpty());
  }

  @Test
  public void nonExistingElement() throws Exception {
    Model model = new Model();
    assertNull(model.build().getElementById("NOT_EXISTING"));
  }

  @Test
  public void modelId() throws Exception {
    Model model = new Model().setId("id");
    assertTrue(model.build().hasId());
    assertTrue(new Model().build().hasId());
  }

  @Test
  public void modelProperty() throws Exception {
    Model model = new Model().setProperty("key", "value");
    assertTrue(model.hasProperty("key"));
    assertTrue(model.build().hasProperties());
    assertTrue(model.build().hasProperty("key"));
    assertFalse(model.build().hasProperty("unknown_key"));
    assertFalse(new Model().build().hasProperties());
  }

  @Test
  public void noInEdges() throws Exception {
    Vertex vertex = new Vertex();
    Model model = new Model().addVertex(vertex);
    assertTrue(model.build().getInEdges(vertex.build()).isEmpty());
  }

  @Test
  public void inEdges() throws Exception {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
    assertThat(model.build().getInEdges(vertex.build()).size(), is(1));
  }

  @Test
  public void testEquality() throws Exception {
    Model model1 = new Model().setId("ID1");
    Model model2 = new Model().setId("ID1");
    assertThat(model1.build(), is(model2.build()));
    assertThat(model1.build().hashCode(), is(model2.build().hashCode()));
    RuntimeModel model = model1.build();
    assertThat(model, is(model));
  }

  @Test
  public void testInequality() throws Exception {
    Model model1 = new Model().setId("ID1");
    Model model2 = new Model().setId("ID2");
    assertThat(model1.build(), not(model2.build()));
    assertFalse(model1.build().equals(null));
    assertThat(model1.build(), not(model1));
  }
}
