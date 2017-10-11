package org.graphwalker.core.generator;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.ReachedEdge;
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.statistics.Execution;
import org.graphwalker.core.statistics.SimpleProfiler;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nils Olsson
 */
public class CombinedPathTest {

  Vertex start = new Vertex();
  Vertex v1 = new Vertex().setName("v1");
  Vertex v2 = new Vertex().setName("v2");
  Model model = new Model()
      .addEdge(new Edge().setSourceVertex(start).setTargetVertex(v1))
      .addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));

  @Test
  public void simpleTest() throws Exception {
    CombinedPath generator = new CombinedPath();
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
    Context context = new TestExecutionContext(model, generator);
    context.setProfiler(new SimpleProfiler());
    context.setCurrentElement(start.build());
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  @Test(expected = NoPathFoundException.class)
  public void failTest() throws Exception {
    CombinedPath generator = new CombinedPath();
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
    Context context = new TestExecutionContext(model, generator);
    context.setProfiler(new SimpleProfiler());
    context.setCurrentElement(start.build());
    while (context.getPathGenerator().hasNextStep()) {
      context.getPathGenerator().getNextStep();
    }
    context.getPathGenerator().getNextStep();
  }

  @Test
  public void toStringTest() throws Exception {
    CombinedPath generator = new CombinedPath();
    assertEquals(generator.getPathGenerators().size(), 0);
    assertEquals(generator.toString(), "");
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
    assertEquals(generator.getPathGenerators().size(), 1);
    assertEquals("RandomPath(ReachedVertex(v1))", generator.toString());
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
    assertEquals(generator.getPathGenerators().size(), 2);
    assertEquals("RandomPath(ReachedVertex(v1)) RandomPath(ReachedVertex(v2))", generator.toString());
  }

  @Test
  public void generatePath() throws Exception {
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Vertex v3 = new Vertex().setName("v3");
    Edge e1 = new Edge().setName("e1").setSourceVertex(v1).setTargetVertex(v2);
    Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v3);
    Edge e3 = new Edge().setName("e3").setSourceVertex(v3).setTargetVertex(v1);
    Model model = new Model()
      .addEdge(e1)
      .addEdge(e2)
      .addEdge(e3);

    CombinedPath combinedPath = new CombinedPath();
    combinedPath.addPathGenerator(new RandomPath(new EdgeCoverage(100)));
    combinedPath.addPathGenerator(new AStarPath(new ReachedEdge("e2")));

    Context context = new TestExecutionContext(model, combinedPath).setCurrentElement(v1.build());
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    List<Element> expectedPath = Arrays.asList(
      e1.build(),
      v2.build(),
      e2.build(),
      v3.build(),
      e3.build(),
      v1.build(),

      // This is where the A* generates a path from v1 to e2
      e1.build(),
      v2.build(),
      e2.build()
    );
    List<Element> path = machine.getProfiler().getExecutionPath().stream()
      .map(Execution::getElement).collect(Collectors.toList());
    assertThat(expectedPath, is(path));
  }

  @Test(expected = MachineException.class)
  public void negativeTest() throws Exception {
    CombinedPath generator = new CombinedPath();
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
    generator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
    Context context = new TestExecutionContext(model, generator);
    context.setProfiler(new SimpleProfiler());
    context.setCurrentElement(start.build());
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }
}
