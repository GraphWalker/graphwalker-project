package org.graphwalker.core.event;

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

import static org.graphwalker.core.event.EventType.AFTER_ELEMENT;
import static org.graphwalker.core.event.EventType.BEFORE_ELEMENT;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineBase;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ObserverTest implements Observer {

  private int counter = 0;

  @Override
  public void update(Machine observable, Element object, EventType type) {
    if (EventType.BEFORE_ELEMENT.equals(type)) {
      counter++;
    }
  }

  private MachineBase createMachine() {
    Vertex vertex = new Vertex();
    Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(new Vertex()));
    Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
    context.setNextElement(vertex);
    MachineBase machine = new SimpleMachine(context);
    assertTrue(Observer.class.isAssignableFrom(this.getClass()));
    assertTrue(Observable.class.isAssignableFrom(machine.getClass()));
    return machine;
  }

  @Test
  public void verifyEvents() throws Exception {
    Machine machine = createMachine();
    final List<EventType> types = new ArrayList<>();
    machine.addObserver((observable, object, type) -> types.add(type));
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertArrayEquals(new Object[]{
        BEFORE_ELEMENT, AFTER_ELEMENT
        , BEFORE_ELEMENT, AFTER_ELEMENT
        , BEFORE_ELEMENT, AFTER_ELEMENT}, types.toArray());
  }

  @Test
  public void updateCounter() throws Exception {
    Machine machine = createMachine();
    machine.addObserver(this);
    Context context = machine.getContexts().get(0);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
    assertThat(counter, is(3));
  }

  @Test
  public void removeObserver() throws Exception {
    Machine machine = createMachine();
    machine.addObserver(this);
    Context context = machine.getContexts().get(0);
    while (machine.hasNextStep()) {
      machine.getNextStep();
      machine.deleteObserver(this);
    }
    assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
    assertThat(counter, is(1));
  }

  @Test
  public void removeObservers() throws Exception {
    Machine machine = createMachine();
    machine.addObserver(this);
    assertThat(machine.getObservers().size(), is(1));
    machine.deleteObservers();
    assertThat(machine.getObservers().size(), is(0));
  }
}
