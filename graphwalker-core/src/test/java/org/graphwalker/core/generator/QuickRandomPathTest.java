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

import static org.graphwalker.core.Models.findEdge;
import static org.graphwalker.core.Models.findVertex;
import static org.graphwalker.core.Models.simpleModel;
import static org.graphwalker.core.Models.singleModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.graphwalker.core.algorithm.AlgorithmException;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge.RuntimeEdge;
import org.graphwalker.core.model.Model.RuntimeModel;
import org.graphwalker.core.model.Vertex.RuntimeVertex;
import org.graphwalker.core.statistics.Profiler;
import org.junit.Test;

/**
 * @author Kristian Karl
 */
public class QuickRandomPathTest {

  @Test
  public void simpleTest() throws Exception {
    RuntimeModel model = simpleModel().build();
    RuntimeVertex source = findVertex(model, "A");
    RuntimeVertex target = findVertex(model, "B");
    RuntimeEdge edge = findEdge(model, "ab");
    Context context = new TestExecutionContext().setModel(model).setNextElement(source);
    PathGenerator generator = new QuickRandomPath(new VertexCoverage(100));
    context.setPathGenerator(generator);
    Machine machine = new SimpleMachine(context);
    assertTrue(machine.hasNextStep());
    assertEquals(machine.getNextStep().getCurrentElement(), source);
    assertEquals(machine.getNextStep().getCurrentElement(), edge);
    assertEquals(machine.getNextStep().getCurrentElement(), target);
    assertFalse(machine.hasNextStep());
  }

  @Test(expected = AlgorithmException.class)
  public void failTest() throws Exception {
    RuntimeModel model = simpleModel().build();
    RuntimeVertex source = findVertex(model, "A");
    RuntimeVertex target = findVertex(model, "B");
    RuntimeEdge edge = findEdge(model, "ab");
    Context context = new TestExecutionContext().setModel(model).setNextElement(source);
    PathGenerator generator = new QuickRandomPath(new VertexCoverage(100));
    context.setProfiler(new Profiler());
    context.setPathGenerator(generator);
    context.setCurrentElement(source);
    assertEquals(context.getCurrentElement(), source);
    assertEquals(generator.getNextStep().getCurrentElement(), edge);
    assertEquals(generator.getNextStep().getCurrentElement(), target);
    context.getPathGenerator().getNextStep(); // should fail
  }

  @Test(expected = NoPathFoundException.class)
  public void singleTest() throws Exception {
    RuntimeModel model = singleModel().build();
    RuntimeVertex source = findVertex(model, "A");
    Context context = new TestExecutionContext().setModel(model).setNextElement(source);
    PathGenerator generator = new QuickRandomPath(new VertexCoverage(100));
    context.setProfiler(new Profiler());
    context.setPathGenerator(generator);
    context.setCurrentElement(source);
    assertTrue(generator.hasNextStep());
    generator.getNextStep(); // should fail
  }
}
