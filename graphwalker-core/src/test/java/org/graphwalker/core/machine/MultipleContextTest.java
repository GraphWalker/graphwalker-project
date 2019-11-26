package org.graphwalker.core.machine;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultipleContextTest {

  Vertex A = new Vertex().setId("A").setSharedState("A");
  Vertex B = new Vertex().setId("B").setSharedState("B");
  Edge AB = new Edge().setSourceVertex(A).setTargetVertex(B).setId("AB");
  Model model = new Model().addEdge(AB);

  @Test
  public void multipleContexts() throws Exception {
    Context contextA = new TestExecutionContext(model).setNextElement(A);
    Context contextB = new TestExecutionContext(model).setNextElement(A);
    Machine machine = new SimpleMachine(contextA, contextB);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  @Test
  public void seededMultipleContexts() throws Exception {
    Vertex A1 = new Vertex().setId("A1").setSharedState("A");
    Vertex B1 = new Vertex().setId("B1").setSharedState("B");
    Vertex A2 = new Vertex().setId("A2").setSharedState("A");
    Vertex B2 = new Vertex().setId("B2").setSharedState("B");

    Model model1 = new Model().setId("1");
    model1.addEdge(new Edge().setSourceVertex(A1).setTargetVertex(B1).setId("A1B1_1"));
    model1.addEdge(new Edge().setSourceVertex(A1).setTargetVertex(B1).setId("A1B1_2"));
    model1.addEdge(new Edge().setSourceVertex(A1).setTargetVertex(B1).setId("A1B1_3"));
    model1.addEdge(new Edge().setSourceVertex(B1).setTargetVertex(A1).setId("B1A1_1"));
    model1.addEdge(new Edge().setSourceVertex(B1).setTargetVertex(A1).setId("B1A1_2"));

    Model model2 = new Model().setId("2");
    model2.addEdge(new Edge().setSourceVertex(A2).setTargetVertex(B2).setId("A2B2_1"));
    model2.addEdge(new Edge().setSourceVertex(A2).setTargetVertex(B2).setId("A2B2_2"));
    model2.addEdge(new Edge().setSourceVertex(A2).setTargetVertex(B2).setId("A2B2_3"));
    model2.addEdge(new Edge().setSourceVertex(B2).setTargetVertex(A2).setId("B2A2_1"));
    model2.addEdge(new Edge().setSourceVertex(B2).setTargetVertex(A2).setId("B2A2_2"));

    SingletonRandomGenerator.setSeed(123456789);
    Context context1 = new TestExecutionContext(model1, new RandomPath(new EdgeCoverage(100))).setNextElement(A1);
    Context context2 = new TestExecutionContext(model2, new RandomPath(new EdgeCoverage(100))).setNextElement(A2);
    Machine machine = new SimpleMachine(context1, context2);

    List<String> actualPath = new ArrayList<String>();
    while (machine.hasNextStep()) {
      machine.getNextStep();
      actualPath.add(machine.getCurrentContext().getCurrentElement().getId());
    }

    Assert.assertArrayEquals(new ArrayList<>(Arrays.asList(
      "A1",
      "A2",
      "A2B2_3",
      "B2",
      "B1",
      "B1A1_2",
      "A1",
      "A2",
      "A2B2_3",
      "B2",
      "B1",
      "B1A1_1",
      "A1",
      "A1B1_2",
      "B1",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_2",
      "B1",
      "B2",
      "B2A2_1",
      "A2",
      "A1",
      "A1B1_3",
      "B1",
      "B1A1_2",
      "A1",
      "A2",
      "A2B2_2",
      "B2",
      "B2A2_2",
      "A2",
      "A2B2_3",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_3",
      "B1",
      "B1A1_1",
      "A1",
      "A1B1_3",
      "B1",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_1",
      "B1",
      "B1A1_1",
      "A1",
      "A1B1_2",
      "B1",
      "B1A1_1",
      "A1",
      "A2",
      "A2B2_2",
      "B2",
      "B1",
      "B1A1_2",
      "A1",
      "A1B1_2",
      "B1",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_1",
      "B1",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_1",
      "B1",
      "B2",
      "B2A2_1",
      "A2",
      "A2B2_3",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_3",
      "B1",
      "B1A1_1",
      "A1",
      "A1B1_1",
      "B1",
      "B2",
      "B2A2_2",
      "A2",
      "A1",
      "A1B1_2",
      "B1",
      "B1A1_1",
      "A1",
      "A2",
      "A2B2_1",
      "B2"
    )).toArray(), actualPath.toArray());
  }
}
