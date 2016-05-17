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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge.RuntimeEdge;
import org.graphwalker.core.model.Model.RuntimeModel;
import org.graphwalker.core.model.Vertex.RuntimeVertex;
import org.junit.Test;

import static org.graphwalker.core.Models.findEdge;
import static org.graphwalker.core.Models.findVertex;
import static org.graphwalker.core.Models.simpleModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nils Olsson
 */
public class RandomPathTest {

  @Test
  public void simpleTest() {
    RuntimeModel model = simpleModel().build();
    RuntimeVertex source = findVertex(model, "A");
    RuntimeVertex target = findVertex(model, "B");
    RuntimeEdge edge = findEdge(model, "ab");
    PathGenerator generator = new RandomPath(new VertexCoverage(100));
    Context context = new TestExecutionContext().setModel(model).setNextElement(source);
    context.setPathGenerator(generator);
    context.setCurrentElement(source);
    assertEquals(context.getCurrentElement(), source);
    assertEquals(generator.getNextStep().getCurrentElement(), edge);
    assertEquals(generator.getNextStep().getCurrentElement(), target);
    assertNotNull(generator.getContext());
    assertNotNull(generator.getStopCondition());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void newStopConditionTest() {
    RuntimeModel model = simpleModel().build();
    RuntimeVertex source = findVertex(model, "A");
    RuntimeVertex target = findVertex(model, "B");
    RuntimeEdge edge = findEdge(model, "ab");
    Context context = new TestExecutionContext().setModel(model).setNextElement(source);
    context.setPathGenerator(new RandomPath(new VertexCoverage(100)));
    context.setCurrentElement(source);
    assertEquals(context.getCurrentElement(), source);
    assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), edge);
    assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), target);
    context.getPathGenerator().setStopCondition(new EdgeCoverage(100));
    assertTrue(EdgeCoverage.class.isAssignableFrom(context.getPathGenerator().getStopCondition().getClass()));
  }

  @Test(expected = NoPathFoundException.class)
  public void failTest() {
    RuntimeModel model = simpleModel().build();
    RuntimeVertex source = findVertex(model, "A");
    RuntimeVertex target = findVertex(model, "B");
    RuntimeEdge edge = findEdge(model, "ab");
    Context context = new TestExecutionContext().setModel(model).setNextElement(source);
    context.setPathGenerator(new RandomPath(new VertexCoverage(100)));
    assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), source);
    assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), edge);
    assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), target);
    context.getPathGenerator().getNextStep(); // should fail
  }
}
