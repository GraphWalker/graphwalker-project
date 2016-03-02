package org.graphwalker.core.machine;

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

import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.statistics.Profiler;

import java.util.ArrayList;
import java.util.List;

import static org.graphwalker.core.common.Objects.isNotNull;

/**
 * <h1>MachineBase</h1>
 * MachineBase represents the Finite State Machine.
 * </p>
 * The MachineBase keeps lists of Contexts and Observers, a Profiler and the exception strategy.
 * </p>
 *
 * @author Nils Olsson
 */
public abstract class MachineBase implements Machine {

    private final List<Context> contexts = new ArrayList<>();
    private final List<Observer> observers = new ArrayList<>();
    private final Profiler profiler = new Profiler();

    private ExceptionStrategy exceptionStrategy = new FailFastStrategy();
    private Context currentContext;

    @Override
    public List<Context> getContexts() {
        return contexts;
    }

    @Override
    public void addObserver(Observer observer) {
        if (isNotNull(observer) && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void notifyObservers(Element element, EventType type) {
        for (Observer observer : observers) {
            observer.update(this, element, type);
        }
    }

    @Override
    public void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void deleteObservers() {
        observers.clear();
    }

    @Override
    public Profiler getProfiler() {
        return profiler;
    }

    @Override
    public Context getCurrentContext() {
        return currentContext;
    }

    protected void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    @Override
    public ExceptionStrategy getExceptionStrategy() {
        return exceptionStrategy;
    }

    @Override
    public void setExceptionStrategy(ExceptionStrategy exceptionStrategy) {
        this.exceptionStrategy = exceptionStrategy;
    }

}
