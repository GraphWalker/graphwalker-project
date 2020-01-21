package org.graphwalker.core.generator;

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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.Length;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kristian Karl
 */
public class WeightedRandomPathTest {

  private static final Logger LOG = LoggerFactory.getLogger(WeightedRandomPathTest.class);

  private final Vertex source = new Vertex().setName("source").setId("source");
  private final Vertex target = new Vertex().setName("target").setId("target");
  private final Edge edge1 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.5).setName("edge1").setId("edge1");
  private final Edge edge2 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.25).setName("edge2").setId("edge2");
  private final Edge edge3 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.15).setName("edge3").setId("edge3");
  private final Edge edge4 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge4").setId("edge4");
  private final Edge edge5 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge5").setId("edge5");
  private final Edge edge6 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.15).setName("edge6").setId("edge6");

  private final Edge back2SourceEdge = new Edge().setSourceVertex(target).setTargetVertex(source).setName("back2SourceEdge").setId("back2SourceEdge");
  private final Model model = new Model()
    .addEdge(edge1)
    .addEdge(edge2)
    .addEdge(edge3)
    .addEdge(edge4)
    .addEdge(edge5)
    .addEdge(back2SourceEdge);

  @Test
  public void doesNotThrowForValidModel() throws Exception {
    PathGenerator generator = new WeightedRandomPath(new EdgeCoverage(100));
    Context context = new TestExecutionContext(model, generator).setCurrentElement(source.build());
    SimpleMachine machine = new SimpleMachine(context);

    while (machine.hasNextStep()) {
      LOG.debug(machine.getCurrentContext().getCurrentElement().getName());
      machine.getNextStep();
    }
  }

  @Test(expected = MachineException.class)
  public void throwsWhenTotalWeightHigherThanOne() throws Exception {
    Model invalidModel = new Model()
      .addEdge(edge1)
      .addEdge(edge2)
      .addEdge(edge3)
      .addEdge(edge4)
      .addEdge(edge5)
      .addEdge(edge6)
      .addEdge(back2SourceEdge);
    PathGenerator generator = new WeightedRandomPath(new EdgeCoverage(100));
    Context context = new TestExecutionContext(invalidModel, generator).setCurrentElement(source.build());
    SimpleMachine machine = new SimpleMachine(context);

    while (machine.hasNextStep()) {
      LOG.debug(machine.getCurrentContext().getCurrentElement().getName());
      machine.getNextStep();
    }
  }

  @Test(expected = MachineException.class)
  public void throwsWhenModelEmpty() throws Exception {
    Model emptyModel = new Model();
    PathGenerator generator = new WeightedRandomPath(new EdgeCoverage(100));
    Context context = new TestExecutionContext(emptyModel, generator).setCurrentElement(source.build());
    SimpleMachine machine = new SimpleMachine(context);

    while (machine.hasNextStep()) {
      LOG.debug(machine.getCurrentContext().getCurrentElement().getName());
      machine.getNextStep();
    }
  }

  @Test
  public void doesNotThrowWhenNoWeightsSpecified() throws Exception {
    Edge edge1 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge1");
    Edge edge2 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge2");
    Edge edge3 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge3");
    Edge edge4 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge4");
    Edge edge5 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge5");

    Edge back2SourceEdge = new Edge().setSourceVertex(target).setTargetVertex(source).setName("back2SourceEdge");
    Model unweightedModel = new Model()
      .addEdge(edge1)
      .addEdge(edge2)
      .addEdge(edge3)
      .addEdge(edge4)
      .addEdge(edge5)
      .addEdge(back2SourceEdge);

    PathGenerator generator = new WeightedRandomPath(new EdgeCoverage(100));
    Context context = new TestExecutionContext(unweightedModel, generator).setCurrentElement(source.build());
    SimpleMachine machine = new SimpleMachine(context);

    while (machine.hasNextStep()) {
      LOG.debug(machine.getCurrentContext().getCurrentElement().getName());
      machine.getNextStep();
    }
  }

  @Test
  public void seededGenerator() {
    SingletonRandomGenerator.setSeed(1349327921);
    PathGenerator generator = new WeightedRandomPath(new Length(30));
    Context context = new TestExecutionContext(model, generator).setCurrentElement(source.build());
    SimpleMachine machine = new SimpleMachine(context);

    List<String> actualPath = new ArrayList<String>();
    while (machine.hasNextStep()) {
      machine.getNextStep();
      actualPath.add(machine.getCurrentContext().getCurrentElement().getId());
    }

    Assert.assertArrayEquals(new ArrayList<>(Arrays.asList(
      "edge1",
      "target",
      "back2SourceEdge",
      "source",
      "edge1",
      "target",
      "back2SourceEdge",
      "source",
      "edge1",
      "target",
      "back2SourceEdge",
      "source",
      "edge1",
      "target",
      "back2SourceEdge",
      "source",
      "edge1",
      "target",
      "back2SourceEdge",
      "source",
      "edge2",
      "target",
      "back2SourceEdge",
      "source",
      "edge2",
      "target",
      "back2SourceEdge",
      "source",
      "edge1",
      "target"
    )).toArray(), actualPath.toArray());
  }

  @Test
   public void singleEdge() {
    Model invalidModel = new Model().addEdge(edge1);
    PathGenerator generator = new WeightedRandomPath(new EdgeCoverage(100));
    Context context = new TestExecutionContext(invalidModel, generator).setCurrentElement(source.build());
    SimpleMachine machine = new SimpleMachine(context);

    while (machine.hasNextStep()) {
      LOG.debug(machine.getCurrentContext().getCurrentElement().getName());
      machine.getNextStep();
    }
  }
}
