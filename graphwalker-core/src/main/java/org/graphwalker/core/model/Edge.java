package org.graphwalker.core.model;

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

import org.graphwalker.core.common.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.graphwalker.core.common.Objects.*;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * <h1>Edge</h1>
 * The  Edge holds the information for a transition in a model.
 * </p>
 * The edge represents an action performed by a test, which takes the system
 * under test, from a state to another.
 * The edge has a source and target vertex. If the vertices are identical, the
 * edge is a self loop. The source vertex is not mandatory, but in a model,
 * there should be only one such instance. Also, the target vertex is not
 * mandatory, but again, in a model, there should be only one such instance.
 *
 * @author Nils Olsson
 */
public class Edge extends CachedBuilder<Edge, Edge.RuntimeEdge> {

  private Vertex sourceVertex;
  private Vertex targetVertex;
  private Guard guard;
  private List<Action> actions = new ArrayList<>();
  private Double weight = 0.0;
  private Integer dependency = 0;

  /**
   * Sets the source vertex of the edge.
   *
   * @param vertex The source vertex.
   * @return The edge.
   */
  public Edge setSourceVertex(Vertex vertex) {
    this.sourceVertex = vertex;
    invalidateCache();
    return this;
  }

  /**
   * Gets the source vertex.
   *
   * @return The source vertex.
   * @see Edge#setSourceVertex
   */
  public Vertex getSourceVertex() {
    return sourceVertex;
  }

  /**
   * Sets the target vertex of the edge.
   *
   * @param vertex The target vertex.
   * @return The edge.
   */
  public Edge setTargetVertex(Vertex vertex) {
    this.targetVertex = vertex;
    invalidateCache();
    return this;
  }

  /**
   * Gets the target vertex of the edge.
   *
   * @return The vertex.
   * @see Edge#setTargetVertex
   */
  public Vertex getTargetVertex() {
    return targetVertex;
  }

  /**
   * Sets the guard of the edge. The code in the guard is by default interpreted as javascript.
   * The guard works like an 'if-statement'. It controls the accessibility of the edge.
   * During execution, the guard evaluates to a boolean expression.
   * If true, the edge is accessible, else it's not.
   *
   * @param guard The guard.
   * @return The edge.
   */
  public Edge setGuard(Guard guard) {
    this.guard = guard;
    invalidateCache();
    return this;
  }

  /**
   * Gets the guard of the edge.
   *
   * @return The guard.
   * @see Edge#setGuard
   */
  public Guard getGuard() {
    return guard;
  }

  /**
   * Adds an action to the edge, which represents a piece of code that will be executed
   * each time the edge is being traversed. The code is by default interpreted as javascript.
   *
   * @param action The action.
   * @return The edge.
   */
  public Edge addAction(Action action) {
    this.actions.add(action);
    invalidateCache();
    return this;
  }

  public Edge addActions(Action... actions) {
    return addActions(Arrays.asList(actions));
  }

  public Edge addActions(List<Action> actions) {
    this.actions.addAll(actions);
    return this;
  }

  /**
   * Adds a list of actions to the edge, which represents a pieces of code that will be executed
   * each time the edge is being traversed. The code snippets is by default interpreted as javascript.
   *
   * @param actions The actions.
   * @return The edge.
   * @see Edge#addAction
   */
  public Edge setActions(List<Action> actions) {
    this.actions = new ArrayList<>(actions);
    invalidateCache();
    return this;
  }

  /**
   * Gets the lists of actions of the edge.
   *
   * @return The actions
   * @see Edge#setActions
   */
  public List<Action> getActions() {
    return unmodifiableList(actions);
  }

  /**
   * Gets the weight of the edge.
   *
   * @return The weight as double.
   * @see Edge#setWeight
   */
  public Double getWeight() {
    return weight;
  }

  /**
   * The weight is used as probability when using the {@link org.graphwalker.core.generator.WeightedRandomPath}.
   * Weight means the probability for the edge to be selected.
   *
   * @param weight a double between 0 and 1
   * @return The edge
   */
  public Edge setWeight(Double weight) {
    this.weight = weight;
    invalidateCache();
    return this;
  }

  /**
   * Creates an immutable edge from this edge.
   *
   * @return An immutable edge as a RuntimeEdge
   */
  @Override
  protected RuntimeEdge createCache() {
    return new RuntimeEdge(this);
  }

  /**
   * Gets the dependency of the edge.
   *
   * @return The dependency as double.
   * @see Edge#setDependency
   */
  public Integer getDependency() {
    return dependency;
  }

  /**
   * The dependency shows how much targetVertex depends on sourceVertex.
   * One way to obtain the dependency is by using
   * process mining to generate a model out of log files.
   *
   * @param dependency a double between 0 and 1
   * @return The edge
   */
  public Edge setDependency(Integer dependency) {
    this.dependency = dependency;
    invalidateCache();
    return this;
  }

  /**
   * <h1>RuntimeEdge</h1>
   * Immutable class for Edge
   * </p>
   * This class is used in models. It guarantees that that the internal states of
   * the instance will not change after it's construction.
   * </p>
   */
  public static class RuntimeEdge extends RuntimeBase {

    private final RuntimeVertex sourceVertex;
    private final RuntimeVertex targetVertex;
    private final Guard guard;
    private final Double weight;
    private final Integer dependency;

    private RuntimeEdge(Edge edge) {
      super(edge.getId(), edge.getName(), edge.getActions(), edge.getRequirements(), edge.getProperties());
      this.sourceVertex = build(edge.getSourceVertex());
      this.targetVertex = build(edge.getTargetVertex());
      this.guard = edge.getGuard();
      this.weight = edge.getWeight();
      this.dependency = edge.getDependency();
    }

    private <T> T build(Builder<T> builder) {
      return isNotNull(builder) ? builder.build() : null;
    }

    /**
     * Gets the source vertex.
     *
     * @return The source vertex.
     * @see Edge#setSourceVertex
     */
    public RuntimeVertex getSourceVertex() {
      return sourceVertex;
    }

    /**
     * Gets the target vertex of the edge.
     *
     * @return The vertex.
     * @see Edge#setTargetVertex
     */
    public RuntimeVertex getTargetVertex() {
      return targetVertex;
    }

    /**
     * Gets the guard of the edge.
     *
     * @return The guard.
     * @see Edge#setGuard
     */
    public Guard getGuard() {
      return guard;
    }

    public boolean hasGuard() {
      return isNotNull(guard) && isNotNullOrEmpty(guard.getScript());
    }

    /**
     * Gets the weight of the edge.
     *
     * @return The weight as double.
     * @see Edge#setWeight
     */
    public Double getWeight() {
      return weight;
    }

    /**
     * TODO Needs documentation
     */
    @Override
    public void accept(ElementVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((guard == null) ? 0 : guard.hashCode());
      result = prime * result
               + ((sourceVertex == null) ? 0 : sourceVertex.hashCode());
      result = prime * result
               + ((targetVertex == null) ? 0 : targetVertex.hashCode());
      result = prime * result
               + ((weight == null) ? 0 : weight.hashCode());
      result = prime * result
               + ((dependency == null) ? 0 : dependency.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (!super.equals(o)) {
        return false;
      }
      RuntimeEdge that = (RuntimeEdge) o;
      return Objects.equals(sourceVertex, that.sourceVertex) &&
             Objects.equals(targetVertex, that.targetVertex) &&
             Objects.equals(guard, that.guard) &&
             Objects.equals(weight, that.weight) &&
             Objects.equals(dependency, that.dependency);
    }

    /**
     * Gets the dependency of the edge.
     *
     * @return The dependency as Integer.
     * @see Edge#setDependency
     */
    public Integer getDependency() {
      return dependency;
    }

    /**
     * Gets the dependency of the edge.
     *
     * @return The dependency as Double.
     * @see Edge#setDependency
     */
    public double getDependencyAsDouble() {
      return (double) getDependency() / 100;
    }


  }
}
