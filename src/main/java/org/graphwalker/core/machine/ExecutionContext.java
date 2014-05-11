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

import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.model.Builder;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.statistics.Profiler;

import javax.script.SimpleScriptContext;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public class ExecutionContext extends SimpleScriptContext implements Context {

    private final RuntimeModel model;
    private final PathGenerator pathGenerator;
    private final Profiler profiler = new Profiler(this);

    private ExecutionStatus executionStatus = ExecutionStatus.NOT_EXECUTED;
    private Element currentElement;
    private Element nextElement;

    public ExecutionContext(Model model, PathGenerator pathGenerator) {
        this.model = model.build();
        this.pathGenerator = pathGenerator;
    }

    public RuntimeModel getModel() {
        return model;
    }

    public Profiler getProfiler() {
        return profiler;
    }

    public PathGenerator getPathGenerator() {
        return pathGenerator;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public void setCurrentElement(Element element) {
        this.currentElement = element;
    }

    public Element getNextElement() {
        return nextElement;
    }

    public void setNextElement(Builder<? extends Element> nextElement) {
        this.nextElement = nextElement.build();
        this.currentElement = null;
    }
}
