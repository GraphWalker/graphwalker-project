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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.RequirementStatus;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class RequirementCoverageTest {

  @Test
  public void testConstructor() throws Exception {
    RequirementCoverage requirementCoverage = new RequirementCoverage(66);
    assertThat(requirementCoverage.getPercent(), is(66));
  }

  @Test(expected = StopConditionException.class)
  public void testNegativePercent() throws Exception {
    new RequirementCoverage(-55);
  }

  @Test
  public void testFulfilment() throws Exception {
    Vertex vertex = new Vertex().addRequirement(new Requirement("1"));
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex)
                                          .setTargetVertex(vertex).addRequirement(new Requirement("2")));//.addRequirement(new Requirement("3"));
    Context context = new TestExecutionContext(model, new RandomPath(new RequirementCoverage(100)));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertEquals(context.getRequirements(), context.getRequirements(RequirementStatus.PASSED));
    assertTrue(context.getRequirements(RequirementStatus.NOT_COVERED).isEmpty());
    assertTrue(context.getRequirements(RequirementStatus.FAILED).isEmpty());
  }

  @Test
  public void testIsFulfilled() throws Exception {
    Vertex vertex = new Vertex().addRequirement(new Requirement("1"));
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex)
                                          .setTargetVertex(vertex).addRequirement(new Requirement("2")));//.addRequirement(new Requirement("3"));
    Context context = new TestExecutionContext(model, new RandomPath(new RequirementCoverage(100)));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      assertFalse(context.getPathGenerator().getStopCondition().isFulfilled());
      machine.getNextStep();
    }
    assertTrue(context.getPathGenerator().getStopCondition().isFulfilled());
  }

  @Test
  public void testNoRequirements() throws Exception {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
    Context context = new TestExecutionContext(model, new RandomPath(new RequirementCoverage(100)));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      assertFalse(context.getPathGenerator().getStopCondition().isFulfilled());
      machine.getNextStep();
    }
    assertTrue(context.getPathGenerator().getStopCondition().isFulfilled());
  }

  @Test
  public void testEdgeRequirement() throws Exception {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex).addRequirement(new Requirement("REQ1")));
    StopCondition stopCondition = new RequirementCoverage(100);
    Context context = new TestExecutionContext(model, new RandomPath(stopCondition));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    assertFalse(stopCondition.isFulfilled());
    assertThat(context.getRequirements(RequirementStatus.PASSED).size(), is(0));
    machine.getNextStep();
    assertFalse(stopCondition.isFulfilled());
    assertThat(context.getRequirements(RequirementStatus.PASSED).size(), is(0));
    machine.getNextStep();
    assertFalse(stopCondition.isFulfilled());
    assertThat(context.getRequirements(RequirementStatus.PASSED).size(), is(0));
    machine.getNextStep();
    assertTrue(stopCondition.isFulfilled());
    assertNotNull(context.getRequirements());
    assertThat(context.getRequirements().size(), is(1));
    assertThat(context.getRequirements(RequirementStatus.FAILED).size(), is(0));
    assertThat(context.getRequirements(RequirementStatus.NOT_COVERED).size(), is(0));
    assertThat(context.getRequirements(RequirementStatus.PASSED).size(), is(1));
  }
}
