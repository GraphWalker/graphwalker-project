package org.graphwalker.core.statistics;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge.RuntimeEdge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Vertex.RuntimeVertex;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author Nils Olsson
 */
public class SimpleProfiler implements Profiler {

  private long startTime = 0;

  private final Map<Context, Map<Element, List<Execution>>> executions = new HashMap<>();
  private final List<Execution> executionPath = new ArrayList<>();

  @Override
  public void addContext(Context context) {
    if (!executions.containsKey(context)) {
      executions.put(context, new HashMap<>());
    }
  }

  @Override
  public Set<Context> getContexts() {
    return executions.keySet();
  }

  @Override
  public void start(Context context) {
    if (!executions.containsKey(context)) {
      executions.put(context, new HashMap<>());
    }
    if (!executions.get(context).containsKey(context.getCurrentElement())) {
      executions.get(context).put(context.getCurrentElement(), new ArrayList<>());
    }
    startTime = System.nanoTime();
  }

  @Override
  public void stop(Context context) {
    long stopTime = System.nanoTime();
    Execution execution = new Execution(context, context.getCurrentElement(), startTime, stopTime - startTime);
    executionPath.add(execution);
    executions.get(context).get(context.getCurrentElement()).add(execution);
  }

  @Override
  public boolean isVisited(Context context, Element element) {
    return executions.containsKey(context) && executions.get(context).containsKey(element);
  }

  @Override
  public long getTotalVisitCount() {
    return executionPath.size();
  }

  @Override
  public long getVisitCount(Context context, Element element) {
    if (executions.containsKey(context) && executions.get(context).containsKey(element)) {
      return executions.get(context).get(element).size();
    }
    return 0L;
  }

  @Override
  public List<Element> getUnvisitedElements() {
    return executions.keySet().stream()
      .map(this::getUnvisitedElements)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getUnvisitedElements(Context context) {
    return context.getModel().getElements().stream()
      .filter(element -> !executions.get(context).containsKey(element))
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getVisitedEdges() {
    return executions.keySet().stream()
      .map(this::getVisitedEdges)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getVisitedEdges(Context context) {
    return context.getModel().getElements().stream()
      .filter(element -> element instanceof RuntimeEdge)
      .filter(element -> executions.get(context).containsKey(element))
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getUnvisitedEdges() {
    return executions.keySet().stream()
      .map(this::getUnvisitedEdges)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getUnvisitedEdges(Context context) {
    return context.getModel().getElements().stream()
      .filter(element -> element instanceof RuntimeEdge)
      .filter(element -> !executions.get(context).containsKey(element))
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getUnvisitedVertices() {
    return executions.keySet().stream()
      .map(this::getUnvisitedVertices)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getUnvisitedVertices(Context context) {
    return context.getModel().getElements().stream()
      .filter(element -> element instanceof RuntimeVertex)
      .filter(element -> !executions.get(context).containsKey(element))
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getVisitedVertices() {
    return executions.keySet().stream()
      .map(this::getVisitedVertices)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  @Override
  public List<Element> getVisitedVertices(Context context) {
    return context.getModel().getElements().stream()
      .filter(element -> element instanceof RuntimeVertex)
      .filter(element -> executions.get(context).containsKey(element))
      .collect(Collectors.toList());
  }

  @Override
  public List<Execution> getExecutionPath() {
    return executionPath;
  }

  @Override
  public long getTotalExecutionTime() {
    return getTotalExecutionTime(TimeUnit.MILLISECONDS);
  }

  @Override
  public long getTotalExecutionTime(TimeUnit unit) {
    return executionPath.stream().mapToLong(e -> e.getDuration(unit)).sum();
  }

  @Override
  public List<Profile> getProfiles() {
    return executions.entrySet().stream()
      .flatMap(entry -> entry.getValue().keySet().stream().map(element -> getProfile(entry.getKey(), element)))
      .collect(Collectors.toList());
  }

  @Override
  public Profile getProfile(Context context, Element element) {
    return new Profile(context, element, executions.get(context).get(element));
  }
}
