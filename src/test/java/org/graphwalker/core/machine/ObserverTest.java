package org.graphwalker.core.machine;

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
import org.graphwalker.core.event.Observable;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class ObserverTest implements Observer<Element> {

    private int counter = 0;

    @Override
    public void update(Observable<Element> observable, Element object) {
        counter++;
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

    @Test(expected = NullPointerException.class)
    public void nullObserver() {
        ObservableMachine observable = createMachine();
        observable.addObserver(null);
    }

    @Test
    public void changeObserver() {
        ObservableMachine observable = createMachine();
        Assert.assertThat(observable.hasChanged(), is(false));
        observable.notifyObservers();
        Assert.assertThat(observable.hasChanged(), is(false));
    }

    @Test
    public void addRemoveObserver() {
        ObservableMachine observable = createMachine();
        observable.addObserver(this);
        Assert.assertThat(observable.countObservers(), is(1));
        observable.deleteObserver(this);
        Assert.assertThat(observable.countObservers(), is(0));
        observable.addObserver(this);
        Assert.assertThat(observable.countObservers(), is(1));
        observable.deleteObservers();
        Assert.assertThat(observable.countObservers(), is(0));
        observable.addObserver(this);
        Assert.assertThat(observable.countObservers(), is(1));
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
}
