package org.graphwalker.core.generator;

import org.graphwalker.core.condition.Never;
import org.graphwalker.core.condition.PredefinedPathStopCondition;
import org.graphwalker.core.condition.StopConditionException;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.Arrays;

import static org.graphwalker.core.Models.*;
import static org.junit.Assert.*;

public class PredefinedPathTest {

  @Test
  public void simpleTest() {
    // Model
    Model model = fourEdgesModel();
    Edge edgeAB = model.getEdges().stream().filter(edge -> "ab".equals(edge.getId())).findFirst().get();
    Edge edgeAB_2 = model.getEdges().stream().filter(edge -> "ab_2".equals(edge.getId())).findFirst().get();
    Edge edgeAB_3 = model.getEdges().stream().filter(edge -> "ab_3".equals(edge.getId())).findFirst().get();
    Edge edgeBA = model.getEdges().stream().filter(edge -> "ba".equals(edge.getId())).findFirst().get();
    model.setPredefinedPath(Arrays.asList(edgeAB, edgeBA, edgeAB_2, edgeBA, edgeAB_3));

    // Runtime model
    Model.RuntimeModel runtimeModel = model.build();
    Vertex.RuntimeVertex source = findVertex(runtimeModel, "A");
    Vertex.RuntimeVertex target = findVertex(runtimeModel, "B");
    Edge.RuntimeEdge runtimeEdgeAB = findEdge(runtimeModel, "ab");
    Edge.RuntimeEdge runtimeEdgeAB_2 = findEdge(runtimeModel, "ab_2");
    Edge.RuntimeEdge runtimeEdgeAB_3 = findEdge(runtimeModel, "ab_3");
    Edge.RuntimeEdge runtimeEdgeBA = findEdge(runtimeModel, "ba");

    // Context, generator and machine
    Context context = new TestExecutionContext().setModel(runtimeModel).setNextElement(source);
    PathGenerator generator = new PredefinedPath(new PredefinedPathStopCondition());
    context.setPathGenerator(generator);
    Machine machine = new SimpleMachine(context);

    // Tests
    assertTrue(machine.hasNextStep());
    assertEquals(machine.getNextStep().getCurrentElement(), source);
    assertEquals(machine.getNextStep().getCurrentElement(), runtimeEdgeAB);
    assertEquals(machine.getNextStep().getCurrentElement(), target);
    assertEquals(machine.getNextStep().getCurrentElement(), runtimeEdgeBA);
    assertEquals(machine.getNextStep().getCurrentElement(), source);
    assertEquals(machine.getNextStep().getCurrentElement(), runtimeEdgeAB_2);
    assertEquals(machine.getNextStep().getCurrentElement(), target);
    assertEquals(machine.getNextStep().getCurrentElement(), runtimeEdgeBA);
    assertEquals(machine.getNextStep().getCurrentElement(), source);
    assertEquals(machine.getNextStep().getCurrentElement(), runtimeEdgeAB_3);
    assertEquals(machine.getNextStep().getCurrentElement(), target);
    assertFalse(machine.hasNextStep());
  }

  @Test(expected = MachineException.class)
  public void testUnreachableEdge() throws Exception {
    // Model
    Model model = simpleModel();
    Edge edgeAB = model.getEdges().get(0);
    model.setPredefinedPath(Arrays.asList(edgeAB, edgeAB));

    // Runtime model
    Model.RuntimeModel runtimeModel = model.build();
    Vertex.RuntimeVertex source = findVertex(runtimeModel, "A");
    Vertex.RuntimeVertex target = findVertex(runtimeModel, "B");
    Edge.RuntimeEdge edge = findEdge(runtimeModel, "ab");

    // Context, generator and machine
    Context context = new TestExecutionContext().setModel(runtimeModel).setNextElement(source);
    PathGenerator generator = new PredefinedPath(new PredefinedPathStopCondition());
    context.setPathGenerator(generator);
    Machine machine = new SimpleMachine(context);

    // Tests
    assertTrue(machine.hasNextStep());
    assertEquals(machine.getNextStep().getCurrentElement(), source);
    assertEquals(machine.getNextStep().getCurrentElement(), edge);
    assertEquals(machine.getNextStep().getCurrentElement(), target);
    assertTrue(machine.hasNextStep());
    machine.getNextStep(); // should fail
  }

  @Test(expected = StopConditionException.class)
  public void testIncompatibleStopConditionInstance() {
    new PredefinedPath(new Never()); // should faild
  }

}
