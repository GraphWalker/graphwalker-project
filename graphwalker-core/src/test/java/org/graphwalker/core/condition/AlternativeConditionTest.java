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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class AlternativeConditionTest {

  @Test
  public void testConstructor() throws Exception {
    AlternativeCondition alternativeCondition = new AlternativeCondition();
    assertNotNull(alternativeCondition);
    assertNotNull(alternativeCondition.getStopConditions());
    assertThat(alternativeCondition.getStopConditions().size(), is(0));
  }

  @Test
  public void testFulfilment() throws Exception {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1");
    Model model = new Model().addEdge(e1);
    StopCondition condition = new AlternativeCondition()
        .addStopCondition(new VertexCoverage(100))
        .addStopCondition(new ReachedEdge("e1"));
    Context context = new TestExecutionContext(model, new RandomPath(condition));
    context.setCurrentElement(v1.build());
    Machine machine = new SimpleMachine(context);
    assertThat(condition.getFulfilment(), is(0.0));
    machine.getNextStep();
    assertThat(condition.getFulfilment(), is(1.0));
    assertThat(condition.toString(), is("VertexCoverage(100) OR ReachedEdge(e1)"));
  }

  @Test
  public void testIsFulfilled() throws Exception {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1");
    Model model = new Model().addEdge(e1);
    StopCondition condition = new AlternativeCondition()
        .addStopCondition(new VertexCoverage(100))
        .addStopCondition(new ReachedEdge("e1"));
    Context context = new TestExecutionContext(model, new RandomPath(condition));
    context.setCurrentElement(v1.build());
    Machine machine = new SimpleMachine(context);
    assertThat(condition.getFulfilment(), is(0.0));
    machine.getNextStep();
    assertTrue(condition.isFulfilled());
    assertThat(condition.toString(), is("VertexCoverage(100) OR ReachedEdge(e1)"));
  }

  @Test
  public void testMultipleCondition() throws Exception {
    AlternativeCondition condition = new AlternativeCondition();
    condition.addStopCondition(new Never());
    assertThat("Should be false", condition.isFulfilled(), is(false));
    condition.addStopCondition(new Never());
    assertThat("Should be false", condition.isFulfilled(), is(false));
    condition.addStopCondition(new Never());
    assertThat("Should be false", condition.isFulfilled(), is(false));
    assertThat(condition.toString(), is("Never() OR Never() OR Never()"));
    condition.addStopCondition(new Always());
    assertThat("Should be true", condition.isFulfilled(), is(true));
    assertThat(condition.toString(), is("Never() OR Never() OR Never() OR Always()"));
  }

  private class Always extends StopConditionBase {

    protected Always() {
      super("");
    }

    @Override
    public double getFulfilment() {
      return 1.0;
    }

    @Override
    public boolean isFulfilled() {
      return true;
    }
  }
}
