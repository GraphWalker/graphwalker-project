package org.graphwalker.core.machine;

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
