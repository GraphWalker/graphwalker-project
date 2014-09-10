package org.graphwalker.core.event;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observable;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.graphwalker.core.event.EventType.AFTER_ELEMENT;
import static org.graphwalker.core.event.EventType.BEFORE_ELEMENT;
import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class ObserverTest implements Observer<Element> {

    private int counter = 0;

    @Override
    public void update(Observable<Element> observable, Element object, EventType type) {
        if (EventType.BEFORE_ELEMENT.equals(type)) {
            counter++;
        }
    }

    private ObservableMachine createMachine() {
        Vertex vertex = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(new Vertex()));
        Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
        context.setNextElement(vertex);
        ObservableMachine machine = new SimpleMachine(context);
        Assert.assertTrue(Observer.class.isAssignableFrom(this.getClass()));
        Assert.assertTrue(Observable.class.isAssignableFrom(machine.getClass()));
        return machine;
    }

    @Test
    public void verifyEvents() {
        Machine machine = createMachine();
        final List<EventType> types = new ArrayList<>();
        machine.addObserver(new Observer<Element>() {
            @Override
            public void update(Observable<Element> observable, Element object, EventType type) {
                types.add(type);
            }
        });
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertArrayEquals(new Object[]{
                  BEFORE_ELEMENT, AFTER_ELEMENT
                , BEFORE_ELEMENT, AFTER_ELEMENT
                , BEFORE_ELEMENT, AFTER_ELEMENT}, types.toArray());
    }


    @Test(expected = NullPointerException.class)
    public void nullObserver() {
        ObservableMachine observable = createMachine();
        observable.addObserver(null);
    }

    @Test
    public void updateCounter() {
        Machine machine = createMachine();
        machine.addObserver(this);
        Context context = machine.getContexts().get(0);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
        Assert.assertThat(counter, is(3));
    }

    @Test
    public void removeObserver() {
        Machine machine = createMachine();
        machine.addObserver(this);
        Context context = machine.getContexts().get(0);
        while (machine.hasNextStep()) {
            machine.getNextStep();
            machine.deleteObserver(this);
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
        Assert.assertThat(counter, is(1));
    }
}
