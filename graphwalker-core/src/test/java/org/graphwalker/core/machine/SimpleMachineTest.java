package org.graphwalker.core.machine;

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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Path;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Nils Olsson
 */
public class SimpleMachineTest {

  @Test
  public void simpleMachine() {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(new Vertex()));
    Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
  }

  @Test
  public void loopEdge() {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
    Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
  }

  @Test
  public void noStartVertex() {
    Edge edge = new Edge().setTargetVertex(new Vertex());
    Model model = new Model().addEdge(edge);
    Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
    context.setNextElement(edge);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
  }

  @Test
  public void executeAction() {
    Vertex vertex1 = new Vertex();
    Vertex vertex2 = new Vertex();
    Model model = new Model()
      .addEdge(new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2).addAction(new Action("var i = 1;")))
      .addEdge(new Edge().setSourceVertex(vertex2).setTargetVertex(vertex1).setGuard(new Guard("i != 0")));
    Context context = new TestExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
    context.setNextElement(vertex1);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
    assertThat(context.getProfiler().getTotalVisitCount(), is(5L));
  }

  @Test
  public void executeActionInitBlock() {
    Vertex vertex1 = new Vertex();
    Vertex vertex2 = new Vertex();
    Model model = new Model()
      .addEdge(new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2))
      .addEdge(new Edge().setSourceVertex(vertex2).setTargetVertex(vertex1).setGuard(new Guard("i != 0")));
    model.addAction(new Action("var i = 1;"));
    Context context = new TestExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
    context.setNextElement(vertex1);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
    assertThat(context.getProfiler().getTotalVisitCount(), is(5L));
  }

  @Test(expected = MachineException.class)
  public void honorGuard() {
    Vertex vertex1 = new Vertex();
    Vertex vertex2 = new Vertex();
    Model model = new Model()
      .addEdge(new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2).addAction(new Action("var i = 1;")))
      .addEdge(new Edge().setSourceVertex(vertex2).setTargetVertex(vertex1).setGuard(new Guard("i == 0")));
    Context context = new TestExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
    context.setNextElement(vertex1);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
    }
  }

  @Test
  public void sharedState() {
    Vertex start = new Vertex();
    Vertex shared1 = new Vertex().setSharedState("MyState");
    Edge edge1 = new Edge().setSourceVertex(start).setTargetVertex(shared1);
    Vertex shared2 = new Vertex().setSharedState("MyState");
    Vertex stop = new Vertex();
    Edge edge2 = new Edge().setSourceVertex(shared2).setTargetVertex(stop);
    Model model1 = new Model().addEdge(edge1);
    Model model2 = new Model().addEdge(edge2);
    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(model1, new RandomPath(new VertexCoverage(100))).setNextElement(start));
    contexts.add(new TestExecutionContext(model2, new RandomPath(new VertexCoverage(100))));
    Machine machine = new SimpleMachine(contexts);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    // the profiler path is a stack so the first element is the last visited, therefore we create the list in reverse order
    Path<Element> expectedPath = new Path<>(Arrays.<Element>asList(stop.build(), edge2.build(), shared2.build(), shared1.build(), edge1.build(), start.build()));
    assertArrayEquals(expectedPath.toArray(), machine.getProfiler().getPath().toArray());
  }

  @Test(expected = MachineException.class)
  public void singleSharedStates() {
    Vertex start = new Vertex();
    Vertex shared1 = new Vertex().setSharedState("MyState1");
    Edge edge1 = new Edge().setSourceVertex(start).setTargetVertex(shared1);
    Vertex shared2 = new Vertex().setSharedState("MyState2");
    Vertex stop = new Vertex();
    Edge edge2 = new Edge().setSourceVertex(shared2).setTargetVertex(stop);
    Model model1 = new Model().addEdge(edge1);
    Model model2 = new Model().addEdge(edge2);
    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(model1, new RandomPath(new VertexCoverage(100))).setNextElement(start));
    contexts.add(new TestExecutionContext(model2, new RandomPath(new VertexCoverage(100))));
    Machine machine = new SimpleMachine(contexts);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  @Test
  public void simpleShortestAllPaths() {
    Vertex start = new Vertex().setName("Start");
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3a = new Vertex().setName("v3");
    Vertex v3b = new Vertex().setName("v3");
    Edge e1 = new Edge().setName("e1").setSourceVertex(start).setTargetVertex(v2).addAction(new Action("x = -1"));
    Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v1).addAction(new Action("x = x + 1"));
    Edge e3 = new Edge().setName("e3").setSourceVertex(v1).setTargetVertex(v2);
    Edge e4 = new Edge().setName("e4").setSourceVertex(v2).setTargetVertex(v3a).setGuard(new Guard("x > 1"));
    Edge e5 = new Edge().setName("e5").setSourceVertex(v3a).setTargetVertex(v2);
    Edge e6 = new Edge().setName("e6").setSourceVertex(v2).setTargetVertex(v3b);
    Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5).addEdge(e6);
    Context context = new TestExecutionContext(model, new ShortestAllPaths(new VertexCoverage(100)));
    context.setNextElement(start);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.<Element>asList(start.build(), e1.build(), v2.build(), e2.build()
      , v1.build(), e3.build(), v2.build(), e4.build(), v3a.build()
      , e5.build(), v2.build(), e6.build(), v3b.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
  }

  @Test
  public void simpleShortestAllPaths2() {
    Vertex v1 = new Vertex().setName("v1");
    Edge e1 = new Edge().setName("e1").setTargetVertex(v1);
    Edge e2 = new Edge().setName("e2").setSourceVertex(v1).setTargetVertex(v1);
    Model model = new Model().addEdge(e1).addEdge(e2);
    Context context = new TestExecutionContext(model, new ShortestAllPaths(new EdgeCoverage(100)));
    context.setNextElement(e1);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.<Element>asList(e1.build(), v1.build(), e2.build(), v1.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
  }

  @Test
  public void simpleShortestAllPaths3() {
    Vertex start = new Vertex().setName("Start");
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3 = new Vertex().setName("v3");
    Vertex v4 = new Vertex().setName("v4");
    Edge e1 = new Edge().setName("e1").setSourceVertex(start).setTargetVertex(v1);
    Edge e2 = new Edge().setName("e2").setSourceVertex(v1).setTargetVertex(v2);
    Edge e3 = new Edge().setName("e3").setSourceVertex(v1).setTargetVertex(v3);
    Edge e4 = new Edge().setName("e4").setSourceVertex(v2).setTargetVertex(v4);
    Edge e5 = new Edge().setName("e5").setSourceVertex(v3).setTargetVertex(v4);
    Edge e6 = new Edge().setName("e6").setSourceVertex(v4).setTargetVertex(v1);
    Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5).addEdge(e6);
    Context context = new TestExecutionContext(model, new ShortestAllPaths(new VertexCoverage(100)));
    context.setNextElement(start);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.<Element>asList(
      start.build(),
      e1.build(),
      v1.build(),
      e2.build(),
      v2.build(),
      e4.build(),
      v4.build(),
      e6.build(),
      v1.build(),
      e3.build(),
      v3.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
  }

  @Test
  public void simpleAStar() {
    Vertex start = new Vertex().setName("Start");
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3a = new Vertex().setName("v3");
    Vertex v3b = new Vertex().setName("v4");
    Edge e1 = new Edge().setName("e1").setSourceVertex(start).setTargetVertex(v2).addAction(new Action("x = -1"));
    Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v1).addAction(new Action("x = x + 1"));
    Edge e3 = new Edge().setName("e3").setSourceVertex(v1).setTargetVertex(v2);
    Edge e4 = new Edge().setName("e4").setSourceVertex(v2).setTargetVertex(v3a).setGuard(new Guard("x > 1"));
    Edge e5 = new Edge().setName("e5").setSourceVertex(v3a).setTargetVertex(v2);
    Edge e6 = new Edge().setName("e6").setSourceVertex(v2).setTargetVertex(v3b);
    Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5).addEdge(e6);
    Context context = new TestExecutionContext(model, new AStarPath(new ReachedVertex("v3")));
    context.setNextElement(start);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.<Element>asList(
      start.build(),
      e1.build(),
      v2.build(),
      e2.build(),
      v1.build(),
      e3.build(),
      v2.build(),
      e2.build(),
      v1.build(),
      e3.build(),
      v2.build(),
      e2.build(),
      v1.build(),
      e3.build(),
      v2.build(),
      e4.build(),
      v3a.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
  }

  @Test
  public void multipleStartVerticesA() {
    // It's not true that we can have several start elements, core only care about the actual start point
    Vertex firstStartVertex = new Vertex().setName("Start");
    Vertex secondStartVertex = new Vertex().setName("Second Start");
    Vertex endVertex = new Vertex().setName("End");
    Edge e1 = new Edge().setSourceVertex(firstStartVertex).setTargetVertex(secondStartVertex);
    Edge e2 = new Edge().setSourceVertex(secondStartVertex).setTargetVertex(endVertex);
    Model model = new Model().addEdge(e1).addEdge(e2);
    Context context = new TestExecutionContext(model, new AStarPath(new ReachedVertex("End")));
    context.setNextElement(secondStartVertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    // We should start at "Second Start" and then walk the path to the end
    List<Element> expectedPath = Arrays.<Element>asList(
      secondStartVertex.build(),
      e2.build(),
      endVertex.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
    assertFalse(context.getProfiler().isVisited(firstStartVertex.build()));
    assertThat(context.getProfiler().getTotalVisitCount(), is(3L));
  }

  @Test
  public void simpleAllVerticesTest() {
    Vertex v1 = new Vertex().setName("v1");
    Edge e1 = new Edge().setName("e1").setTargetVertex(v1);
    Model model = new Model().addEdge(e1);
    Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100))).setNextElement(e1);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.<Element>asList(e1.build(), v1.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
  }

}
