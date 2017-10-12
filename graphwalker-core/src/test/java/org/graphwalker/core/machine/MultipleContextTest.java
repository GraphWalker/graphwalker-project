package org.graphwalker.core.machine;

import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

public class MultipleContextTest {

  Vertex start = new Vertex();
  Vertex stop = new Vertex();
  Edge edge = new Edge().setSourceVertex(start).setTargetVertex(stop);
  Model model = new Model().addEdge(edge);

  @Test
  public void multipleContexts() throws Exception {
    Context contextA = new TestExecutionContext(model).setNextElement(start);
    Context contextB = new TestExecutionContext(model).setNextElement(start);
    Machine machine = new SimpleMachine(contextA, contextB);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }
}
