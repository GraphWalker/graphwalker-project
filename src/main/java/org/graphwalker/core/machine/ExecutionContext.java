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
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.statistics.Profiler;

import javax.script.SimpleScriptContext;
import java.util.List;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public class ExecutionContext extends SimpleScriptContext implements Context {

    private RuntimeModel model;
    private PathGenerator pathGenerator;
    private Profiler profiler = new Profiler(this);

    private ExecutionStatus executionStatus = ExecutionStatus.NOT_EXECUTED;
    private Element currentElement;
    private Element nextElement;

    public ExecutionContext() {
    }

    public ExecutionContext(Model model, PathGenerator pathGenerator) {
        this.model = model.build();
        this.pathGenerator = pathGenerator;
    }

    public RuntimeModel getModel() {
        return model;
    }

    public ExecutionContext setModel(Model model) {
        this.model = model.build();
        return this;
    }

    public Profiler getProfiler() {
        return profiler;
    }

    public PathGenerator getPathGenerator() {
        return pathGenerator;
    }

    public ExecutionContext setPathGenerator(PathGenerator pathGenerator) {
        this.pathGenerator = pathGenerator;
        return this;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public ExecutionContext setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
        return this;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public ExecutionContext setCurrentElement(Element element) {
        this.currentElement = element;
        return this;
    }

    public Element getNextElement() {
        return nextElement;
    }

    public ExecutionContext setNextElement(Builder<? extends Element> nextElement) {
        this.nextElement = nextElement.build();
        this.currentElement = null;
        return this;
    }

    public List<Requirement> getRequirements() {
        throw new RuntimeException("Not implemented");
    }

    public List<Requirement> getRequirements(RequirementStatus status) {
        throw new RuntimeException("Not implemented");
    }


}
