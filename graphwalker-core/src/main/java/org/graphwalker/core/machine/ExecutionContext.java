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

import org.graphwalker.core.algorithm.Algorithm;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Builder;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.statistics.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * <h1>ExecutionContext</h1>
 * The ExecutionContext ties a model and a path generator together.
 * </p>
 * The context not only connects a model with a path generator, it also keeps track of
 * the execution of the model when traversing it, and it's history. Also, the model has an
 * internal code and data scoop, which the context also is responsible for running,
 * </p>
 *
 * @author Nils Olsson
 */
public abstract class ExecutionContext extends SimpleScriptContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionContext.class);

    private final static String DEFAULT_SCRIPT_LANGUAGE = "JavaScript";
    private ScriptEngine scriptEngine;

    private RuntimeModel model;
    private PathGenerator pathGenerator;
    private Profiler profiler;
    private ExecutionStatus executionStatus = ExecutionStatus.NOT_EXECUTED;
    private Element currentElement;
    private Element nextElement;
    private Element lastElement;

    private final Map<Class<? extends Algorithm>, Object> algorithms = new HashMap<>();

    private final Map<Requirement, RequirementStatus> requirements = new HashMap<>();

    public ExecutionContext() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(DEFAULT_SCRIPT_LANGUAGE);
        engine.setContext(this);
        String script = "";
        Compilable compiler = (Compilable) engine;
        for (Method method : getClass().getMethods()) {
            String arguments = "";
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                if (i > 0) {
                    arguments += ",";
                }
                arguments += Character.toChars(65 + i)[0];
            }
            script += "function " + method.getName() + "(" + arguments;
            script += ") { return impl." + method.getName() + "(" + arguments + ");};";
        }
        try {
            CompiledScript compiledScript = compiler.compile(script);
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("impl", this);
            compiledScript.eval(bindings);
            scriptEngine = compiledScript.getEngine();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public ExecutionContext(Model model, PathGenerator pathGenerator) {
        this(model.build(), pathGenerator);
    }

    public ExecutionContext(RuntimeModel model, PathGenerator pathGenerator) {
        this();
        setModel(model);
        setPathGenerator(pathGenerator);
    }

    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public RuntimeModel getModel() {
        return model;
    }

    public Context setModel(RuntimeModel model) {
        this.model = model;
        addRequirements(model);
        return this;
    }

    private void addRequirements(RuntimeModel model) {
        requirements.clear();
        for (Requirement requirement : model.getRequirements()) {
            requirements.put(requirement, RequirementStatus.NOT_COVERED);
        }
        for (Element element : model.getElements()) {
            for (Requirement requirement : element.getRequirements()) {
                requirements.put(requirement, RequirementStatus.NOT_COVERED);
            }
        }
    }

    public Profiler getProfiler() {
        return profiler;
    }

    public Context setProfiler(Profiler profiler) {
        this.profiler = profiler;
        return this;
    }

    public PathGenerator getPathGenerator() {
        return pathGenerator;
    }

    public Context setPathGenerator(PathGenerator pathGenerator) {
        this.pathGenerator = pathGenerator;
        if (isNotNull(pathGenerator)) {
            this.pathGenerator.setContext(this);
        }
        return this;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public Context setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
        return this;
    }

    public Element getLastElement() {
        return lastElement;
    }

    public Element getCurrentElement() {
        return currentElement;
    }

    public Context setCurrentElement(Element element) {
        this.lastElement = this.currentElement;
        this.currentElement = element;
        this.nextElement = null;
        return this;
    }

    public Element getNextElement() {
        return nextElement;
    }

    public Context setNextElement(Builder<? extends Element> nextElement) {
        setNextElement(nextElement.build());
        return this;
    }

    public Context setNextElement(Element nextElement) {
        this.nextElement = nextElement;
        this.currentElement = null;
        return this;
    }

    public Context setRequirementStatus(Requirement requirement, RequirementStatus requirementStatus) {
        requirements.put(requirement, requirementStatus);
        return this;
    }

    public List<Requirement> getRequirements() {
        return new ArrayList<>(requirements.keySet());
    }

    public List<Requirement> getRequirements(RequirementStatus status) {
        List<Requirement> filteredRequirements = new ArrayList<>();
        for (Requirement requirement : requirements.keySet()) {
            if (status.equals(requirements.get(requirement))) {
                filteredRequirements.add(requirement);
            }
        }
        return filteredRequirements;
    }

    @SuppressWarnings("unchecked")
    public <A extends Algorithm> A getAlgorithm(Class<A> clazz) {
        if (!algorithms.containsKey(clazz)) {
            try {
                Constructor<? extends Algorithm> constructor = clazz.getConstructor(Context.class);
                algorithms.put(clazz, constructor.newInstance(this));
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new MachineException(this, e);
            }
        }
        return (A) algorithms.get(clazz);
    }

    public <E> List<E> filter(Collection<E> elements) {
        List<E> filteredElements = new ArrayList<>();
        if (isNotNull(elements)) {
            for (E element : elements) {
                if (element instanceof RuntimeEdge) {
                    RuntimeEdge edge = (RuntimeEdge) element;
                    if (isAvailable(edge)) {
                        filteredElements.add(element);
                    }
                } else {
                    filteredElements.add(element);
                }
            }
        }
        return filteredElements;
    }

    public boolean isAvailable(RuntimeEdge edge) {
        if (edge.hasGuard()) {
            logger.debug("Execute {} {}", edge.getGuard(), edge.getGuard().getScript());
            try {
                return (Boolean) getScriptEngine().eval(edge.getGuard().getScript());
            } catch (ScriptException e) {
                throw new MachineException(this, e);
            }
        }
        return true;
    }

    public void execute(Action action) {
        logger.debug("Execute {}", action.getScript());
        try {
            getScriptEngine().eval(action.getScript());
        } catch (ScriptException e) {
            throw new MachineException(this, e);
        }
    }

    public void execute(String name) {
        logger.debug("Execute {}", name);
        try {
            getClass().getMethod(name); // provoke a NoSuchMethodException exception if the method doesn't exist
            getScriptEngine().eval(name + "()");
        } catch (NoSuchMethodException e) {
            // ignore, method is not defined in the execution context
        } catch (Throwable t) {
            throw new MachineException(this, t);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getKeys() {
        Map<String, String> keys = new HashMap<>();
        List<String> methods = new ArrayList<>();
        for (Method method : getClass().getMethods()) {
            methods.add(method.getName());
        }
        if (getBindings(ENGINE_SCOPE).containsKey("nashorn.global")) {
            Map<String, Object> global = (Map<String, Object>) getBindings(ENGINE_SCOPE).get("nashorn.global");
            for (String key : global.keySet()) {
                if (isVariable(key, methods)) {
                    if (global.get(key) instanceof Double) {
                        keys.put(key, "" + Math.round((double) global.get(key)));
                    } else {
                        keys.put(key, global.get(key).toString());
                    }
                }
            }
        } else {
            for (String key : getBindings(ENGINE_SCOPE).keySet()) {
                if (isVariable(key, methods)) {
                    Object value = getBindings(ENGINE_SCOPE).get(key);
                    if (value instanceof Double) {
                        keys.put(key, "" + Math.round((double) value));
                    } else {
                        keys.put(key, value.toString());
                    }
                }
            }
        }
        return keys;
    }

    @SuppressWarnings("unchecked")
    public Object getAttribute(String name) {
        if (getBindings(ENGINE_SCOPE).containsKey("nashorn.global")) {
            Map<String, Object> attributes = (Map<String, Object>) getBindings(ENGINE_SCOPE).get("nashorn.global");
            return attributes.get(name);
        } else {
            return super.getAttribute(name);
        }
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(String name, Object value) {
        if (getBindings(ENGINE_SCOPE).containsKey("nashorn.global")) {
            Map<String, Object> attributes = (Map<String, Object>) getBindings(ENGINE_SCOPE).get("nashorn.global");
            attributes.put(name, value);
        } else {
            super.setAttribute(name, value, ENGINE_SCOPE);
        }
    }

    private boolean isVariable(String key, List<String> methods) {
        return !"impl".equals(key) && !methods.contains(key) && !"print".equals(key) && !"println".equals(key) && !"context".equals(key);
    }
}
