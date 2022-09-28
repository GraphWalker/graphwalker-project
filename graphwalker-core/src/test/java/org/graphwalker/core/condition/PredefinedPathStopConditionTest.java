package org.graphwalker.core.condition;

import org.graphwalker.core.generator.PredefinedPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PredefinedPathStopConditionTest {

  private Model createSimpleModel() {
    Vertex v0 = new Vertex().setId("v0");
    Vertex v1 = new Vertex().setId("v1");
    Edge e0 = new Edge()
      .setId("e0")
      .setSourceVertex(v0)
      .setTargetVertex(v1)
      .setActions(Arrays.asList(
        new Action("i = 1"),
        new Action("b = false")
      ));
    Edge e1 = new Edge()
      .setId("e1")
      .setSourceVertex(v1)
      .setTargetVertex(v0)
      .setGuard(new Guard("i > (1 + 1) * 1.5 && b"));
    Edge e2 = new Edge()
      .setId("e2")
      .setSourceVertex(v1)
      .setTargetVertex(v1)
      .setActions(Arrays.asList(
        new Action("i = i + 1"),
        new Action("b = i > 4")
      ));
    List<Edge> predefinedPath = Arrays.asList(e0, e2, e2, e2, e2, e1);
    return new Model()
      .addVertex(v0)
      .addVertex(v1)
      .addEdge(e0)
      .addEdge(e1)
      .addEdge(e2)
      .setPredefinedPath(predefinedPath);
  }

  @Test()
  public void testFulfilment() {
    Model model = createSimpleModel();
    Vertex v0 = model.getVertices().stream().filter(vertex -> "v0".equals(vertex.getId())).findFirst().orElseThrow(() -> new RuntimeException("Vertex v0 not found"));
    Vertex v1 = model.getVertices().stream().filter(vertex -> "v1".equals(vertex.getId())).findFirst().orElseThrow(() -> new RuntimeException("Vertex v1 not found"));
    Edge e0 = model.getEdges().stream().filter(edge -> "e0".equals(edge.getId())).findFirst().orElseThrow(() -> new RuntimeException("Edge e0 not found"));
    Edge e1 = model.getEdges().stream().filter(edge -> "e1".equals(edge.getId())).findFirst().orElseThrow(() -> new RuntimeException("Edge e1 not found"));
    Edge e2 = model.getEdges().stream().filter(edge -> "e2".equals(edge.getId())).findFirst().orElseThrow(() -> new RuntimeException("Edge e2 not found"));

    StopCondition condition = new PredefinedPathStopCondition();
    Context context = new TestExecutionContext(model, new PredefinedPath(condition));

    context.setPredefinedPathCurrentElementIndex(0);

    context.setCurrentElement(v0.build());
    assertThat(condition.getFulfilment(), is((double) 1/13));

    context.setCurrentElement(e0.build());
    context.setPredefinedPathCurrentElementIndex(1);
    assertThat(condition.getFulfilment(), is((double) 2/13));

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is((double) 3/13));

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(2);
    assertThat(condition.getFulfilment(), is((double) 4/13));

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is((double) 5/13));

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(3);
    assertThat(condition.getFulfilment(), is((double) 6/13));

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is((double) 7/13));

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(4);
    assertThat(condition.getFulfilment(), is((double) 8/13));

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is((double) 9/13));

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(5);
    assertThat(condition.getFulfilment(), is((double) 10/13));

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is((double) 11/13));

    context.setCurrentElement(e1.build());
    context.setPredefinedPathCurrentElementIndex(6);
    assertThat(condition.getFulfilment(), is((double) 12/13));

    context.setCurrentElement(v0.build());
    assertThat(condition.getFulfilment(), is((double) 13/13));
  }

  @Test()
  public void testIsFulfilled() {
    Model model = createSimpleModel();
    Vertex v0 = model.getVertices().stream().filter(vertex -> "v0".equals(vertex.getId())).findFirst().orElseThrow(() -> new RuntimeException("Vertex v0 not found"));
    Vertex v1 = model.getVertices().stream().filter(vertex -> "v1".equals(vertex.getId())).findFirst().orElseThrow(() -> new RuntimeException("Vertex v1 not found"));
    Edge e0 = model.getEdges().stream().filter(edge -> "e0".equals(edge.getId())).findFirst().orElseThrow(() -> new RuntimeException("Edge e0 not found"));
    Edge e1 = model.getEdges().stream().filter(edge -> "e1".equals(edge.getId())).findFirst().orElseThrow(() -> new RuntimeException("Edge e1 not found"));
    Edge e2 = model.getEdges().stream().filter(edge -> "e2".equals(edge.getId())).findFirst().orElseThrow(() -> new RuntimeException("Edge e2 not found"));

    StopCondition condition = new PredefinedPathStopCondition();
    Context context = new TestExecutionContext(model, new PredefinedPath(condition));

    context.setPredefinedPathCurrentElementIndex(0);

    context.setCurrentElement(v0.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e0.build());
    context.setPredefinedPathCurrentElementIndex(1);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v1.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(2);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v1.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(3);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v1.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(4);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v1.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(5);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v1.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e1.build());
    context.setPredefinedPathCurrentElementIndex(6);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v0.build());
    assertTrue(condition.isFulfilled());
  }

}
