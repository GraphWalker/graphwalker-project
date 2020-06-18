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
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nils Olsson
 */
public class InternalStateTest {

  @Test
  public void testIsFulfilled() throws Exception {
    Vertex vertex = new Vertex();
    Model model = new Model()
      .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex).addAction(new Action("index++")))
      .addAction(new Action("var index = 0"));
    StopCondition condition = new InternalState("index == 99");
    Context context = new TestExecutionContext(model, new RandomPath(condition)).setCurrentElement(vertex.build());
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      assertThat(condition.getFulfilment(), is(0.0));
      assertFalse(condition.isFulfilled());
      machine.getNextStep();
    }
    assertThat(condition.getFulfilment(), is(1.0));
    assertTrue(condition.isFulfilled());
    assertThat(context.getAttribute("index").asInt(), is(99));
  }

  @Test(expected = StopConditionException.class)
  public void testWrongTypeOfExpression() throws Exception {
    Vertex start = new Vertex();
    Vertex vertex = new Vertex();
    Model model = new Model()
      .addEdge(new Edge().setSourceVertex(start).setTargetVertex(vertex).addAction(new Action("index = 0")))
      .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex).addAction(new Action("index++")));
    StopCondition condition = new InternalState("var test = 'test'");
    Context context = new TestExecutionContext(model, new RandomPath(condition)).setCurrentElement(start.build());
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      assertThat(condition.getFulfilment(), is(0.0));
      assertFalse(condition.isFulfilled());
      machine.getNextStep();
    }
    assertThat(condition.getFulfilment(), is(1.0));
    assertTrue(condition.isFulfilled());
    assertThat(context.getAttribute("index"), is("99"));
  }
}
