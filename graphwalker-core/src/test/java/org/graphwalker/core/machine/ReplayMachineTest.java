package org.graphwalker.core.machine;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ReplayMachineTest {

  @Test
  public void replayMachine() {
    Machine machine = createMachineExecution();
    Machine replayMachine = new ReplayMachine(machine.getProfiler());
    while (replayMachine.hasNextStep()) {
      replayMachine.getNextStep();
    }
    assertThat(replayMachine.getProfiler().getPath().toArray(), is(machine.getProfiler().getPath().toArray()));
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
