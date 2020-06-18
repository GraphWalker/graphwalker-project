package org.graphwalker.core.example;

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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.ExecutionStatus;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ExampleTest extends ExecutionContext {

  public void vertex1() {
  }

  public void edge1() {
  }

  public void vertex2() {
  }

  public void vertex3() {
    throw new RuntimeException();
  }

  public boolean isFalse() {
    return false;
  }

  public boolean isTrue() {
    return true;
  }

  public void myAction() {
  }

  @Test
  public void success() throws Exception {
    Vertex start = new Vertex();
    Model model = new Model().addEdge(new Edge()
                                          .setName("edge1")
                                          .setGuard(new Guard("ExampleTest.isTrue()"))
                                          .setSourceVertex(start
                                                               .setName("vertex1"))
                                          .setTargetVertex(new Vertex()
                                                               .setName("vertex2"))
                                          .addAction(new Action("ExampleTest.myAction()")));
    this.setModel(model.build());
    this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
    setNextElement(start);
    Machine machine = new SimpleMachine(this);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  @Test(expected = MachineException.class)
  public void failure() throws Exception {
    Vertex start = new Vertex();
    Model model = new Model().addEdge(new Edge()
                                          .setName("edge1")
                                          .setGuard(new Guard("ExampleTest.isFalse()"))
                                          .setSourceVertex(start
                                                               .setName("vertex1"))
                                          .setTargetVertex(new Vertex()
                                                               .setName("vertex2")));
    this.setModel(model.build());
    this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
    setNextElement(start);
    Machine machine = new SimpleMachine(this);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  @Test
  public void exception() {
    Vertex start = new Vertex();
    Model model = new Model().addEdge(new Edge()
                                          .setName("edge1")
                                          .setGuard(new Guard("ExampleTest.isTrue()"))
                                          .setSourceVertex(start
                                                               .setName("vertex3"))
                                          .setTargetVertex(new Vertex()
                                                               .setName("vertex2")));
    this.setModel(model.build());
    this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
    setNextElement(start);
    Machine machine = new SimpleMachine(this);
    assertThat(getExecutionStatus(), is(ExecutionStatus.NOT_EXECUTED));
    try {
      while (machine.hasNextStep()) {
        machine.getNextStep();
        assertThat(getExecutionStatus(), is(ExecutionStatus.EXECUTING));
      }
    } catch (Throwable t) {
      assertTrue(MachineException.class.isAssignableFrom(t.getClass()));
      assertThat(getExecutionStatus(), is(ExecutionStatus.FAILED));
    }
  }
}
