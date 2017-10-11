package org.graphwalker.core.condition;

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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.statistics.Execution;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ReachedSharedStateTest {

  @Test(expected = StopConditionException.class)
  public void testMissingSharedState() throws Exception {
    Vertex start = new Vertex().setName("Start");
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3 = new Vertex().setName("v3");
    Vertex v4 = new Vertex().setName("v4").setSharedState("MY_SHARED_STATE");
    Edge e1 = new Edge().setName("e1").setSourceVertex(start).setTargetVertex(v2).addAction(new Action("x = -1"));
    Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v1).addAction(new Action("x = x + 1"));
    Edge e3 = new Edge().setName("e3").setSourceVertex(v1).setTargetVertex(v2);
    Edge e4 = new Edge().setName("e4").setSourceVertex(v2).setTargetVertex(v3).setGuard(new Guard("x > 1"));
    Edge e5 = new Edge().setName("e5").setSourceVertex(v3).setTargetVertex(v2);
    Edge e6 = new Edge().setName("e6").setSourceVertex(v2).setTargetVertex(v4);
    Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5).addEdge(e6);
    new TestExecutionContext(model, new AStarPath(new ReachedSharedState("NOT_FOUND")));
  }

  @Test
  public void simpleAStarWithSharedState() throws Exception {
    Vertex start = new Vertex().setName("Start");
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3 = new Vertex().setName("v3");
    Vertex v4 = new Vertex().setName("v4").setSharedState("MY_SHARED_STATE");
    Edge e1 = new Edge().setName("e1").setSourceVertex(start).setTargetVertex(v2).addAction(new Action("x = -1"));
    Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v1).addAction(new Action("x = x + 1"));
    Edge e3 = new Edge().setName("e3").setSourceVertex(v1).setTargetVertex(v2);
    Edge e4 = new Edge().setName("e4").setSourceVertex(v2).setTargetVertex(v3).setGuard(new Guard("x > 1"));
    Edge e5 = new Edge().setName("e5").setSourceVertex(v3).setTargetVertex(v2);
    Edge e6 = new Edge().setName("e6").setSourceVertex(v2).setTargetVertex(v4);
    Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5).addEdge(e6);
    Context context = new TestExecutionContext(model, new AStarPath(new ReachedSharedState("MY_SHARED_STATE")));
    context.setNextElement(start);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.<Element>asList(
        start.build(),
        e1.build(),
        v2.build(),
        e6.build(),
        v4.build());
    List<Element> path = machine.getProfiler().getExecutionPath().stream()
      .map(Execution::getElement).collect(Collectors.toList());
    assertThat(expectedPath, is(path));
  }

  @Test
  public void simpleAStarWithSharedStateBehindGuard() throws Exception {
    Vertex start = new Vertex().setName("Start");
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3 = new Vertex().setName("v3").setSharedState("MY_SHARED_STATE");
    Vertex v4 = new Vertex().setName("v4");
    Edge e1 = new Edge().setName("e1").setSourceVertex(start).setTargetVertex(v2).addAction(new Action("x = -1"));
    Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v1).addAction(new Action("x = x + 1"));
    Edge e3 = new Edge().setName("e3").setSourceVertex(v1).setTargetVertex(v2);
    Edge e4 = new Edge().setName("e4").setSourceVertex(v2).setTargetVertex(v3).setGuard(new Guard("x > 1"));
    Edge e5 = new Edge().setName("e5").setSourceVertex(v3).setTargetVertex(v2);
    Edge e6 = new Edge().setName("e6").setSourceVertex(v2).setTargetVertex(v4);
    Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5).addEdge(e6);
    Context context = new TestExecutionContext(model, new AStarPath(new ReachedSharedState("MY_SHARED_STATE")));
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
        v3.build());
    List<Element> path = machine.getProfiler().getExecutionPath().stream()
      .map(Execution::getElement).collect(Collectors.toList());
    assertThat(expectedPath, is(path));
  }
}
