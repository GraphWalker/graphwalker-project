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

import org.graphwalker.core.algorithm.Algorithm;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.model.*;
import org.graphwalker.core.statistics.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public class ExecutionContext extends SimpleScriptContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionContext.class);

    private final static String DEFAULT_SCRIPT_LANGUAGE = "JavaScript";
    private final Profiler profiler = new Profiler(this);
    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(DEFAULT_SCRIPT_LANGUAGE);

    private RuntimeModel model;
    private PathGenerator pathGenerator;

    private ExecutionStatus executionStatus = ExecutionStatus.NOT_EXECUTED;
    private Element currentElement;
    private Element nextElement;

    private Map<Class<? extends Algorithm>, Object> algorithms = new HashMap<>();

    public ExecutionContext() {
    }

    public ExecutionContext(Model model, PathGenerator pathGenerator) {
        this.model = model.build();
        this.pathGenerator = pathGenerator;
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
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
        setNextElement(nextElement.build());
        return this;
    }

  public ExecutionContext setNextElement(Element nextElement) {
    this.nextElement = nextElement;
    this.currentElement = null;
    return this;
  }

    public List<Requirement> getRequirements() {
        throw new RuntimeException("Not implemented");
    }

    public List<Requirement> getRequirements(RequirementStatus status) {
        throw new RuntimeException("Not implemented");
    }

    @SuppressWarnings("unchecked")
    public <A extends Algorithm> A getAlgorithm(Class<A> clazz) {
        if (!algorithms.containsKey(clazz)) {
            try {
                Constructor<? extends Algorithm> constructor = clazz.getConstructor(ExecutionContext.class);
                algorithms.put(clazz, constructor.newInstance(this));
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new MachineException(e);
            }
        }
        return (A)algorithms.get(clazz);
    }

    public <E> List<E> filter(List<E> elements) {
        List<E> filteredElements = new ArrayList<>();
        if (null != elements) {
            for (E element : elements) {
                if (element instanceof RuntimeEdge) {
                    RuntimeEdge edge = (RuntimeEdge) element;
                    if (!edge.isBlocked() && isAvailable(edge)) {
                        filteredElements.add(element);
                    }
                } else {
                    filteredElements.add(element);
                }
            }
        }
        return filteredElements;
    }

    private boolean isMethodCall(String script) {
        return script.matches("\\w+\\(\\);?");
    }

    public boolean isAvailable(RuntimeEdge edge) {
        if (null != edge.getGuard()) {
            logger.debug("Execute {} {}", edge.getGuard(), edge.getGuard().getScript());
            // TODO: Refactor how script engine is used and created
            getScriptEngine().setContext(this);
            Bindings bindings = getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("impl", this);
            try {
                //ScriptEngineFactory sef = scriptEngine.getFactory();
                //String s = sef.getMethodCallSyntax("impl", edge.getGuard().getScript(), new String[0]);
                //return (Boolean)getScriptEngine().eval("(function(){ return " + edge.getGuard().getScript() + ";}.bind(impl))()");
                if (isMethodCall(edge.getGuard().getScript())) {
                    return (Boolean)getScriptEngine().eval("impl." + edge.getGuard().getScript());
                } else {
                    return (Boolean)getScriptEngine().eval(edge.getGuard().getScript());
                }
            } catch (ScriptException e) {
                /* TODO: Handle errors or ignore them? when using A* guards will be evaluated before actions is performed that
                   can make the guard fail due to a ReferenceError: "variable" is not defined */
                e.printStackTrace();
            }
        }
        return true;
    }

    public void execute(Action action) {
        // TODO: Refactor
        getScriptEngine().setContext(this);
        Bindings bindings = getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("impl", this);
        try {
            if (isMethodCall(action.getScript())) {
                getScriptEngine().eval("impl." + action.getScript());
            } else {
                getScriptEngine().eval(action.getScript());
            }
        } catch (ScriptException e) {
            throw new MachineException(e);
        }
    }

    public void execute(String name) {
        // TODO: Refactor
        try {
            getClass().getMethod(name);
            getScriptEngine().setContext(this);
            Bindings bindings = getScriptEngine().getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("impl", this);
            getScriptEngine().eval("impl." + name + "()");
        } catch (ScriptException e) {
            throw new MachineException(e);
        } catch (NoSuchMethodException e) {
            // warn
        }

    }
}
