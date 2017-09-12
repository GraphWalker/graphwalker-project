package org.graphwalker.core.model;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.graphwalker.core.condition.AlternativeCondition;
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.RequirementStatus;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class RequirementTest {

  @Test
  public void create() throws Exception {
    Requirement requirement = new Requirement("REQ");
    assertNotNull(requirement);
    assertEquals("REQ", requirement.getKey());
  }

  @Test
  public void executeWithSingleRequirement() throws Exception {
    Model model = new Model().addVertex(new Vertex().addRequirement(new Requirement("REQ1")).setName("CHECK_REQ"));
    Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
    context.setNextElement(model.getVertices().get(0));
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertNotNull(context.getRequirements());
    assertThat(context.getRequirements().size(), is(1));
    assertThat(context.getRequirements(RequirementStatus.FAILED).size(), is(0));
    assertThat(context.getRequirements(RequirementStatus.NOT_COVERED).size(), is(0));
    assertThat(context.getRequirements(RequirementStatus.PASSED).size(), is(1));
  }

  @Test
  public void executeWithMultipleRequirements() throws Exception {
    Vertex start = new Vertex().addRequirement(new Requirement("FIRST_STEP"));
    Vertex alt1 = new Vertex().setName("Alt1").addRequirement(new Requirement("ALT1"));
    Vertex alt2 = new Vertex().setName("Alt2").addRequirement(new Requirement("ALT2"));
    Model model = new Model()
        .addEdge(new Edge().setSourceVertex(start).setTargetVertex(alt1).addRequirement(new Requirement("road1")))
        .addEdge(new Edge().setSourceVertex(start).setTargetVertex(alt2).addRequirement(new Requirement("road2")));
    AlternativeCondition condition = new AlternativeCondition();
    condition.addStopCondition(new ReachedVertex("Alt1"));
    condition.addStopCondition(new ReachedVertex("Alt2"));
    Context context = new TestExecutionContext(model, new RandomPath(condition));
    context.setNextElement(model.getVertices().get(0));
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertNotNull(context.getRequirements());
    assertThat(context.getRequirements().size(), is(5));
    assertThat(context.getRequirements(RequirementStatus.FAILED).size(), is(0));
    assertThat(context.getRequirements(RequirementStatus.NOT_COVERED).size(), is(2));
    assertThat(context.getRequirements(RequirementStatus.PASSED).size(), is(3));
  }
}
