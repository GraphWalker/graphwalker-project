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

import org.graalvm.polyglot.Value;
import org.graphwalker.core.algorithm.Algorithm;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.model.*;
import org.graphwalker.core.statistics.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public abstract class ExecutionContext implements Context {

  private static final Logger LOG = LoggerFactory.getLogger(ExecutionContext.class);

  private org.graalvm.polyglot.Context executionEnvironment;
  private org.graalvm.polyglot.Context globalExecutionEnvironment;

  private RuntimeModel model;
  private PathGenerator pathGenerator;
  private Profiler profiler;
  private ExecutionStatus executionStatus = ExecutionStatus.NOT_EXECUTED;
  private Element currentElement;
  private Element nextElement;
  private Element lastElement;
  private Integer predefinedPathCurrentEdgeIndex;

  private String REGEXP_GLOBAL = "global\\.";

  private final Map<Class<? extends Algorithm>, Object> algorithms = new HashMap<>();

  private final Map<Requirement, RequirementStatus> requirements = new HashMap<>();

  public ExecutionContext() {
    executionEnvironment = org.graalvm.polyglot.Context.newBuilder().allowAllAccess(true).option("engine.WarnInterpreterOnly", "false").build();
    executionEnvironment.getBindings("js").putMember(getClass().getSimpleName(), this);
    predefinedPathCurrentEdgeIndex = 0;
  }

  public ExecutionContext(Model model, PathGenerator pathGenerator) {
    this(model.build(), pathGenerator);
  }

  public ExecutionContext(RuntimeModel model, PathGenerator pathGenerator) {
    this();
    setModel(model);
    setPathGenerator(pathGenerator);
  }

  public org.graalvm.polyglot.Context getExecutionEnvironment() {
    return executionEnvironment;
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
    this.profiler.addContext(this);
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

  public Integer getPredefinedPathCurrentEdgeIndex() {
    return predefinedPathCurrentEdgeIndex;
  }

  public Context setPredefinedPathCurrentElementIndex(Integer predefinedPathCurrentElementIndex) {
    this.predefinedPathCurrentEdgeIndex = predefinedPathCurrentElementIndex;
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
        LOG.error(e.getMessage());
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
      LOG.debug("Execute guard: '{}' in edge {}, in model: '{}'", edge.getGuard().getScript(), edge.getName(), getModel().getName());
      Pattern pattern = Pattern.compile(REGEXP_GLOBAL);
      Matcher matcher = pattern.matcher(edge.getGuard().getScript());
      if (matcher.find()) {
        return globalExecutionEnvironment.eval("js", edge.getGuard().getScript().replaceAll(REGEXP_GLOBAL, "")).asBoolean();
      } else {
        return executionEnvironment.eval("js", edge.getGuard().getScript()).asBoolean();
      }
    }
    return true;
  }

  public void execute(Action action) {
    LOG.debug("Execute action: '{}' in model: '{}'", action.getScript(), getModel().getName());
    Pattern pattern = Pattern.compile(REGEXP_GLOBAL);
    Matcher matcher = pattern.matcher(action.getScript());
    if (matcher.find()) {
      globalExecutionEnvironment.eval("js", action.getScript().replaceAll(REGEXP_GLOBAL, ""));
    } else {
      executionEnvironment.eval("js", action.getScript());
    }
    LOG.debug("Data: '{}'", data());
  }

  public void execute(Element element) {
    if (!element.hasName()) {
      return;
    }
    LOG.debug("Execute method: '{}' in model: '{}'", element.getName(), getModel().getName());
    try {
      Method method = getClass().getMethod(element.getName());
      method.invoke(this);
    } catch (NoSuchMethodException e) {
      // ignore, method is not defined in the execution context
    } catch (Throwable t) {
      executionStatus = ExecutionStatus.FAILED;
      LOG.error(t.getMessage());
      throw new MachineException(this, t);
    }
  }

  public Value getAttribute(String name) {
    Pattern pattern = Pattern.compile(REGEXP_GLOBAL);
    Matcher matcher = pattern.matcher(name);
    if (matcher.find()) {
      return globalExecutionEnvironment.getBindings("js").getMember(name.replaceAll(REGEXP_GLOBAL, ""));
    } else {
      return executionEnvironment.getBindings("js").getMember(name);
    }
  }

  public void setAttribute(String name, Value value) {
    executionEnvironment.getBindings("js").putMember(name, value);
  }

  public String data() {
    StringBuilder data = new StringBuilder();
    for (String member : executionEnvironment.getBindings("js").getMemberKeys()) {
      if (executionEnvironment.getBindings("js").getMember(member).toString().contains("org.graphwalker.core.machine.TestExecutionContext")) {
        continue;
      }
      data.append(member)
        .append(": ")
        .append(executionEnvironment.getBindings("js").getMember(member))
        .append(", ");
    }
    if (isNotNull(globalExecutionEnvironment)) {
      for (String member : globalExecutionEnvironment.getBindings("js").getMemberKeys()) {
        data.append("global." + member)
          .append(": ")
          .append(globalExecutionEnvironment.getBindings("js").getMember(member))
          .append(", ");
      }
    }
    return data.toString();
  }

  @Override
  public void setGlobalExecutionEnvironment(org.graalvm.polyglot.Context globalExecutionEnvironment) {
    this.globalExecutionEnvironment = globalExecutionEnvironment;
  }
}
