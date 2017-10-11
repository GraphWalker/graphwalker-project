package org.graphwalker.core.statistics;

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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Nils Olsson
 */
public final class ProfilerTest {

  private static final Logger LOG = LoggerFactory.getLogger(ProfilerTest.class);

  private static final Vertex start = new Vertex();
  private static final Context context = new TestExecutionContext()
    .setModel(new Model()
      .addEdge(new Edge()
        .setSourceVertex(start)
        .setTargetVertex(new Vertex())).build())
    .setCurrentElement(start.build());

  @Test
  public void create() throws Exception {
    Profiler profiler = new SimpleProfiler();
    profiler.addContext(context);
    assertNotNull(profiler);
    assertThat(profiler.getContexts(), is(new HashSet<>(Arrays.asList(context))));
    assertFalse(profiler.isVisited(context, start.build()));
    assertThat(profiler.getTotalVisitCount(), is(0L));
    assertThat(profiler.getVisitCount(context, start.build()), is(0L));
    assertThat(profiler.getUnvisitedEdges().size(), is(1));
    profiler.start(context);
    profiler.stop(context);
    assertTrue(profiler.isVisited(context, start.build()));
    assertThat(profiler.getTotalVisitCount(), is(1L));
    assertThat(profiler.getVisitCount(context, start.build()), is(1L));
    assertThat(profiler.getUnvisitedElements(context).size(), is(2));
    assertThat(profiler.getUnvisitedEdges(context).size(), is(1));
    assertThat(profiler.getUnvisitedVertices(context).size(), is(1));
    assertThat(profiler.getExecutionPath().size(), is(1));
    assertThat(profiler.getTotalExecutionTime(), is(not(0)));
    Context newContext = new TestExecutionContext();
    profiler.start(newContext);
    profiler.stop(newContext);
    assertThat(profiler.getContexts(), is(new HashSet<>(Arrays.asList(context, newContext))));
    assertNotNull(profiler.getProfile(context, start.build()));
    assertThat(profiler.getProfiles().size(), is(2));
  }


  /**
   * This test verifies that in a multi model scenario, where 2 models have elements with
   * the same id's works.
   * The test generated failure commit 6b5da82638a7f60c751ffcc3f52f275c9a8242a7
   * The failure was that not all elements actually was visited.
   * The profiler did not handle elements with the same id in 2 different contexts. This,
   * in turn, made the profiler method isVisited return a false true.
   * The failure was intermittent because of the random path generator.
   */
  @Test
  public void multiModel() throws Exception {
    Vertex A = new Vertex().setName("A").setId("n1");
    Vertex B = new Vertex().setName("B").setId("n2").setSharedState("shared_state");

    Model model1 = new Model();
    model1.addEdge(new Edge().setTargetVertex(A).setName("a").setId("e0").addAction(new Action("a++;")));
    model1.addEdge(new Edge().setSourceVertex(A).setTargetVertex(B).setName("b1").setId("e1").addAction(new Action("b1++;")));
    model1.addEdge(new Edge().setSourceVertex(B).setTargetVertex(A).setName("b2").setId("e2").addAction(new Action("b2++;")));
    model1.addEdge(new Edge().setSourceVertex(B).setTargetVertex(A).setName("b3").setId("e3").addAction(new Action("b3++;")));

    model1.addAction(new Action("a=0;b1=0;b2=0;b3=0;"));

    ExecutionContext context1 = new TestExecutionContext();
    context1.setModel(model1.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
    context1.setNextElement(context1.getModel().findElements("a").get(0));

    Vertex C = new Vertex().setName("C").setId("n1").setSharedState("shared_state");
    Vertex D = new Vertex().setName("D").setId("n2");

    Model model2 = new Model();
    model2.addEdge(new Edge().setSourceVertex(C).setTargetVertex(D).setName("d").setId("e1").addAction(new Action("d++;")));
    model2.addEdge(new Edge().setSourceVertex(D).setTargetVertex(C).setName("c1").setId("e2").addAction(new Action("c1++;")));
    model2.addEdge(new Edge().setSourceVertex(D).setTargetVertex(C).setName("c2").setId("e3").addAction(new Action("c2++;")));

    model2.addAction(new Action("d=0;c1=0;c2=0;"));

    ExecutionContext context2 = new TestExecutionContext();
    context2.setModel(model2.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));

    Machine machine = new SimpleMachine(context1, context2);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      LOG.debug(machine.getCurrentContext().getCurrentElement().getName());
    }

    LOG.debug("\na: " + context1.getAttribute("a").toString() +
              "\nb1: " + context1.getAttribute("b1").toString() +
              "\nb2: " + context1.getAttribute("b2").toString() +
              "\nd: " + context2.getAttribute("d").toString() +
              "\nc1: " + context2.getAttribute("c1").toString() +
              "\nc2: " + context2.getAttribute("c2").toString());
    assertTrue(Float.parseFloat(context1.getAttribute("a").toString()) >= 1);
    assertTrue(Float.parseFloat(context1.getAttribute("b1").toString()) >= 1);
    assertTrue(Float.parseFloat(context1.getAttribute("b2").toString()) >= 1);
    assertTrue(Float.parseFloat(context2.getAttribute("d").toString()) >= 1);
    assertTrue(Float.parseFloat(context2.getAttribute("c1").toString()) >= 1);
    assertTrue(Float.parseFloat(context2.getAttribute("c2").toString()) >= 1);
  }

  @Test
  public void visited() throws Exception {
    Vertex vertex = new Vertex().setName("a_vertex");
    Edge edge = new Edge().setSourceVertex(vertex).setTargetVertex(vertex);
    Model model = new Model().addEdge(edge);
    Profiler profiler = new SimpleProfiler();
    Context contextA = new TestExecutionContext()
      .setModel(model.build())
      .setProfiler(profiler)
      .setCurrentElement(vertex.build());
    Context contextB = new TestExecutionContext()
      .setModel(model.build())
      .setProfiler(profiler)
      .setCurrentElement(vertex.build());
    assertFalse(profiler.isVisited(contextA, vertex.build()));
    assertFalse(profiler.isVisited(contextB, vertex.build()));
    profiler.start(contextA);
    profiler.stop(contextA);
    assertTrue(profiler.isVisited(contextA, contextA.getCurrentElement()));
    assertFalse(profiler.isVisited(contextB, contextB.getCurrentElement()));
    assertThat(profiler.getTotalVisitCount(), is(1L));
    assertThat(profiler.getVisitedEdges().size(), is(0));
    assertThat(profiler.getVisitedVertices().size(), is(1));
    assertThat(profiler.getUnvisitedElements().size(), is(3));
    assertThat(profiler.getUnvisitedEdges().size(), is(2));
    assertThat(profiler.getUnvisitedVertices().size(), is(1));
    assertThat(profiler.getUnvisitedVertices(contextA).size(), is(0));
    assertThat(profiler.getUnvisitedVertices(contextB).size(), is(1));
    assertThat(profiler.getUnvisitedEdges(contextA).size(), is(1));
    assertThat(profiler.getUnvisitedEdges(contextB).size(), is(1));
    contextA.setCurrentElement(edge.build());
    profiler.start(contextA);
    profiler.stop(contextA);
    assertThat(profiler.getVisitedEdges().size(), is(1));
  }

  @Test
  public void unvisited() throws Exception {
    Vertex vertex = new Vertex().setName("a_vertex");
    Model model = new Model().addVertex(vertex);
    Profiler profiler = new SimpleProfiler();
    Context contextA = new TestExecutionContext()
      .setModel(model.build())
      .setProfiler(profiler)
      .setCurrentElement(vertex.build());
    Context contextB = new TestExecutionContext()
      .setModel(model.build())
      .setProfiler(profiler)
      .setCurrentElement(vertex.build());
    assertThat(profiler.getTotalVisitCount(), is(0L));
    assertThat(profiler.getVisitedEdges().size(), is(0));
    assertThat(profiler.getVisitedVertices().size(), is(0));
    assertThat(profiler.getUnvisitedElements().size(), is(2));
    assertThat(profiler.getUnvisitedEdges().size(), is(0));
    assertThat(profiler.getUnvisitedVertices().size(), is(2));
    assertThat(profiler.getUnvisitedVertices(contextA).size(), is(1));
    assertThat(profiler.getUnvisitedVertices(contextB).size(), is(1));
    assertThat(profiler.getUnvisitedEdges(contextA).size(), is(0));
    assertThat(profiler.getUnvisitedEdges(contextB).size(), is(0));
  }
}
