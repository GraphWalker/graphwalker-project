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

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.statistics.Profiler;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Miroslav Janeski
 */
public class DependencyEdgeCoverageTest {

  @Test
  public void testConstructor() {
    DependencyEdgeCoverage edgeCoverage = new DependencyEdgeCoverage(200,100);
    assertThat(edgeCoverage.getPercent(), is(200));
    assertThat(edgeCoverage.getDependency(), is(100));
  }

  @Test(expected = StopConditionException.class)
  public void testNegativePercent() {
    new DependencyEdgeCoverage(-55,1);
  }
  
  @Test(expected = StopConditionException.class)
  public void testNegativeDependency() {
    new DependencyEdgeCoverage(1,-55);
  }

  @Test
  public void testFulfilment() {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setDependency(0.8);
    Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setDependency(0.9);
    Model model = new Model().addEdge(e1).addEdge(e2);
    StopCondition condition = new DependencyEdgeCoverage(100,85);
    Context context = new TestExecutionContext(model, new RandomPath(condition));
    context.setProfiler(new Profiler());
    assertThat(condition.getFulfilment(), is(0.0));
    context.setCurrentElement(e1.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertThat(condition.getFulfilment(), is(0.0));
    context.setCurrentElement(e2.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertThat(condition.getFulfilment(), is(1.0));
  }

  @Test
  public void testIsFulfilled() {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setDependency(0.9);
    Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setDependency(0.8);
    Model model = new Model().addEdge(e2).addEdge(e1);
    StopCondition condition = new DependencyEdgeCoverage(100,85);
    Context context = new TestExecutionContext(model, new RandomPath(condition));
    context.setProfiler(new Profiler());
    assertFalse(condition.isFulfilled());
    context.setCurrentElement(e1.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertFalse(condition.isFulfilled());
    context.setCurrentElement(e2.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertFalse(condition.isFulfilled());
    context.setCurrentElement(v1.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertTrue(condition.isFulfilled());
  }
  
  @Test
  public void testIsFulfilledHighDependencyTreshold() {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setDependency(0.8);
    Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setDependency(0.8);
    Model model = new Model().addEdge(e2).addEdge(e1);
    StopCondition condition = new DependencyEdgeCoverage(100,85);
    Context context = new TestExecutionContext(model, new RandomPath(condition));
    context.setProfiler(new Profiler());
    assertFalse(condition.isFulfilled());
    context.setCurrentElement(e1.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertFalse(condition.isFulfilled());
    context.setCurrentElement(e2.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertFalse(condition.isFulfilled());
    context.setCurrentElement(v1.build());
    context.getProfiler().start(context);
    context.getProfiler().stop(context);
    assertTrue(condition.isFulfilled());
  }
}
