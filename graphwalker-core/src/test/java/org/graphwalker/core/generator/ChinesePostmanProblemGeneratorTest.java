package org.graphwalker.core.generator;

import org.graphwalker.core.algorithm.AlgorithmException;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.*;
import org.graphwalker.core.statistics.SimpleProfiler;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.junit.Assert.*;

public class ChinesePostmanProblemGeneratorTest {
  private final Vertex v1 = new Vertex().setName("v1");
  private final Vertex v2 = new Vertex().setName("v2");
  private final Vertex v3 = new Vertex().setName("v3");
  private final Vertex v4 = new Vertex().setName("v4");

  private final Edge e1 = new Edge().setName("e1").setSourceVertex(v1).setTargetVertex(v2);
  private final Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v3);
  private final Edge e3 = new Edge().setName("e3").setSourceVertex(v3).setTargetVertex(v2);
  private final Edge e4 = new Edge().setName("e4").setSourceVertex(v1).setTargetVertex(v4);
  private final Edge e5 = new Edge().setName("e5").setSourceVertex(v4).setTargetVertex(v3);
  private final Edge e6 = new Edge().setName("e6").setSourceVertex(v3).setTargetVertex(v4);
  private final Edge e7 = new Edge().setName("e7").setSourceVertex(v3).setTargetVertex(v1);

  private final Model model = new Model()
    .addEdge(e1)
    .addEdge(e2)
    .addEdge(e3)
    .addEdge(e4)
    .addEdge(e5)
    .addEdge(e6)
    .addEdge(e7);

  @Test
  public void generatePath(){

    Context context = new TestExecutionContext(model, new ChinesePostmanProblemGenerator(new EdgeCoverage(100)));
    context.setCurrentElement(context.getModel().getElementById("v1")).setCurrentElement(v1.build());
    context.setProfiler(new SimpleProfiler());

    Deque<Builder<? extends Element>> expectedElements = new ArrayDeque<>(
      Arrays.asList(e1, v2, e2, v3, e6, v4, e5, v3, e7, v1, e4, v4, e5, v3, e3, v2, e2, v3, e7, v1)
    );

    execute(context, expectedElements);
    assertTrue(expectedElements.isEmpty());
  }

  @Test (expected = AlgorithmException.class)
  public void failTest(){
    Model notStronglyConnectedGraph = model.deleteEdge(e1);
    notStronglyConnectedGraph = notStronglyConnectedGraph.deleteEdge(e4);


    Context context = new TestExecutionContext(notStronglyConnectedGraph, new ChinesePostmanProblemGenerator(new EdgeCoverage(100)));
    context.setCurrentElement(context.getModel().getElementById("v1")).setCurrentElement(v1.build());
    context.setProfiler(new SimpleProfiler());

    while (context.getPathGenerator().hasNextStep()) {
      context.getPathGenerator().getNextStep();
      context.getProfiler().start(context);
      context.getProfiler().stop(context);
    }
  }

  private void execute(Context context, Deque<Builder<? extends Element>> expectedElements) {
    while (context.getPathGenerator().hasNextStep()) {
      context.getPathGenerator().getNextStep();
      context.getProfiler().start(context);
      context.getProfiler().stop(context);
      assertEquals(expectedElements.removeFirst().build().getName(), context.getCurrentElement().getName());
    }
  }
}
