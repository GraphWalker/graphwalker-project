package org.graphwalker.core.machine;

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
import static org.junit.Assert.assertThat;

import org.graphwalker.core.condition.RequirementCoverage;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class FailedEdgeRequirementTest extends ExecutionContext {

  public void fail() {
    throw new RuntimeException("fail");
  }

  @Test
  public void failEdgeRequirement() throws Exception {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex).setName("fail").addRequirement(new Requirement("REQ1")));
    StopCondition stopCondition = new RequirementCoverage(100);
    Context context = new TestExecutionContext(model, new RandomPath(stopCondition));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    try {
      while (machine.hasNextStep()) {
        machine.getNextStep();
      }
    } catch (RuntimeException e) {
      assertThat(context.getRequirements(RequirementStatus.FAILED).size(), is(1));
    }
  }
}
