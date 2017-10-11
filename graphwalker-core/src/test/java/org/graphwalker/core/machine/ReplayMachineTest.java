package org.graphwalker.core.machine;

/*-
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.*;
import org.graphwalker.core.statistics.Execution;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nils Olsson
 */
public class ReplayMachineTest {

  @Test
  public void replayMachine() throws Exception {
    Machine machine = createMachineExecution();
    Machine replayMachine = new ReplayMachine(machine.getProfiler());
    while (replayMachine.hasNextStep()) {
      replayMachine.getNextStep();
    }
    List<Element> expectedPath = machine.getProfiler().getExecutionPath().stream()
      .map(Execution::getElement).collect(Collectors.toList());
    List<Element> replayedPath = replayMachine.getProfiler().getExecutionPath().stream()
      .map(Execution::getElement).collect(Collectors.toList());
    assertThat(replayedPath, is(expectedPath));
  }

  private Machine createMachineExecution() {
    Vertex vertex = new Vertex();
    Edge edge1 = new Edge().setSourceVertex(vertex).setTargetVertex(vertex).addAction(new Action("flag = true;")).setName("edge1");
    Edge edge2 = new Edge().setSourceVertex(vertex).setTargetVertex(vertex).setGuard(new Guard("flag === true")).setName("edge2");
    Model model = new Model().addEdge(edge1).addEdge(edge2).addAction(new Action("var flag = false;"));
    Context context = new TestExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
    context.setNextElement(vertex);
    Machine machine = new SimpleMachine(context);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    return machine;
  }
}
