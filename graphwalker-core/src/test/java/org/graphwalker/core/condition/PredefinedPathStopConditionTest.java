package org.graphwalker.core.condition;

import org.graphwalker.core.generator.PredefinedPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PredefinedPathStopConditionTest {

  @Test()
  public void testFulfilment() {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setId("e1");
    Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setId("e2");
    List<Edge> predefinedPath = Arrays.asList(e1, e2);
    Model model = new Model().addEdge(e1).addEdge(e2).setPredefinedPath(predefinedPath);
    StopCondition condition = new PredefinedPathStopCondition();
    Context context = new TestExecutionContext(model, new PredefinedPath(condition));

    context.setPredefinedPathCurrentElementIndex(0);

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is((double) 1/5));

    context.setCurrentElement(e1.build());
    context.setPredefinedPathCurrentElementIndex(1);
    assertThat(condition.getFulfilment(), is((double) 2/5));

    context.setCurrentElement(v2.build());
    assertThat(condition.getFulfilment(), is((double) 3/5));

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(2);
    assertThat(condition.getFulfilment(), is((double) 4/5));

    context.setCurrentElement(v1.build());
    assertThat(condition.getFulfilment(), is(1.0));
  }

  @Test()
  public void testIsFulfilled() {
    Vertex v1 = new Vertex();
    Vertex v2 = new Vertex();
    Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setId("e1");
    Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setId("e2");
    List<Edge> predefinedPath = Arrays.asList(e1, e2);
    Model model = new Model().addEdge(e1).addEdge(e2).setPredefinedPath(predefinedPath);
    StopCondition condition = new PredefinedPathStopCondition();
    Context context = new TestExecutionContext(model, new PredefinedPath(condition));

    context.setPredefinedPathCurrentElementIndex(0);

    context.setCurrentElement(v1.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e1.build());
    context.setPredefinedPathCurrentElementIndex(1);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v2.build());
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(e2.build());
    context.setPredefinedPathCurrentElementIndex(2);
    assertFalse(condition.isFulfilled());

    context.setCurrentElement(v1.build());
    assertTrue(condition.isFulfilled());
  }

}
