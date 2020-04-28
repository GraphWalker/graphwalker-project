package org.graphwalker.core.model;

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

import org.graphwalker.core.common.Objects;

import java.util.*;

import static org.graphwalker.core.common.Objects.*;
import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * <h1>Model</h1>
 * The Model,or graph, is a collection of edges and vertices,
 * </p>
 * <img src="doc-files/Model.png">
 * </p>
 * The model is a description of the expected behavior of
 * a system under test. It contains lists of edges and vertices, which creates
 * a directed graph.
 * </p>
 * In a model, the edges represents the actions during a test, and the vertices are where
 * the verifications are performed.
 *
 * @author Nils Olsson
 */
public class Model extends BuilderBase<Model, Model.RuntimeModel> {

  private List<Vertex> vertices = new ArrayList<>();
  private List<Edge> edges = new ArrayList<>();
  private List<Action> actions = new ArrayList<>();

  /**
   * Create a new Model
   */
  public Model() {
  }

  /**
   * Create a new Model, based on a existing {@link org.graphwalker.core.model.Model.RuntimeModel RuntimeModel}
   *
   * @param model A {@link org.graphwalker.core.model.Model.RuntimeModel RuntimeModel} that the new Model will be based on.
   */
  public Model(RuntimeModel model) {
    setId(model.getId());
    setName(model.getName());
    setProperties(model.getProperties());
    setRequirements(model.getRequirements());
    setActions(model.getActions());
    Map<RuntimeVertex, Vertex> cache = new HashMap<>();
    for (RuntimeVertex runtimeVertex : model.getVertices()) {
      Vertex vertex = new Vertex();
      vertex.setId(runtimeVertex.getId());
      vertex.setName(runtimeVertex.getName());
      vertex.setSharedState(runtimeVertex.getSharedState());
      vertex.setRequirements(runtimeVertex.getRequirements());
      vertex.setActions(runtimeVertex.getActions());
      vertex.setProperties(runtimeVertex.getProperties());
      this.vertices.add(vertex);
      cache.put(runtimeVertex, vertex);
    }
    for (RuntimeEdge runtimeEdge : model.getEdges()) {
      Edge edge = new Edge();
      edge.setId(runtimeEdge.getId());
      edge.setName(runtimeEdge.getName());
      edge.setSourceVertex(cache.get(runtimeEdge.getSourceVertex()));
      edge.setTargetVertex(cache.get(runtimeEdge.getTargetVertex()));
      edge.setGuard(runtimeEdge.getGuard());
      edge.setActions(runtimeEdge.getActions());
      edge.setRequirements(runtimeEdge.getRequirements());
      edge.setWeight(runtimeEdge.getWeight());
      edge.setProperties(runtimeEdge.getProperties());
      this.edges.add(edge);
    }
  }

  /**
   * Adds a vertex to the model.
   *
   * @param vertex The vertex to be added.
   * @return The model
   */
  public Model addVertex(Vertex vertex) {
    vertices.add(vertex);
    return this;
  }

  /**
   * Adds an edge to the model.
   * </p>
   * If either the source or target vertex of the edge is not in the model,
   * they will be automatically added as well.
   * </p>
   *
   * @param edge The edge to be added.
   * @return The model.
   */
  public Model addEdge(Edge edge) {
    edges.add(edge);
    if (isNotNull(edge.getSourceVertex()) && !vertices.contains(edge.getSourceVertex())) {
      vertices.add(edge.getSourceVertex());
    }
    if (isNotNull(edge.getTargetVertex()) && !vertices.contains(edge.getTargetVertex())) {
      vertices.add(edge.getTargetVertex());
    }
    return this;
  }

  /**
   * Delete an edge from the model.
   * </p>
   * Will remove the edge from the model.
   * </p>
   *
   * @param edge The edge to be deleted.
   * @return The model.
   */
  public Model deleteEdge(Edge edge) {
    edges.remove(edge);
    return this;
  }

  /**
   * Delete a vertex from the model.
   * </p>
   * Will remove a vertex from the model. Any edges that has the vertex
   * as either source or target, will also be deleted from the model.
   * </p>
   *
   * @param vertex The vertex to be deleted.
   * @return The model.
   */
  public Model deleteVertex(Vertex vertex) {
    edges.removeIf(edge -> vertex.equals(edge.getSourceVertex()) || vertex.equals(edge.getTargetVertex()));
    vertices.remove(vertex);
    return this;
  }

  /**
   * Adds an action to the model.
   * </p>
   * Before a model is being traversed, it will execute all its actions. This is typically
   * needed to initiate variables and data.
   *
   * @param action The action to be added.
   * @return The model
   */
  public Model addAction(Action action) {
    actions.add(action);
    return this;
  }

  public Model addActions(Action... actions) {
    return addActions(Arrays.asList(actions));
  }

  public Model addActions(List<Action> actions) {
    this.actions.addAll(actions);
    return this;
  }

  /**
   * Assign a list of actions to the model.
   *
   * @param actions A list of actions
   * @return The model
   * @see Model#addAction
   */
  public Model setActions(List<Action> actions) {
    this.actions = new ArrayList<>(actions);
    return this;
  }

  /**
   * Gets the list of actions associated with the model.
   *
   * @return List of actions
   * @see Model#addAction
   */
  public List<Action> getActions() {
    return actions;
  }

  /**
   * Gets the list of vertices of the model.
   *
   * @return The list of vertices
   */
  public List<Vertex> getVertices() {
    return vertices;
  }

  /**
   * Gets the list of edges of the model.
   *
   * @return List of edges
   */
  public List<Edge> getEdges() {
    return edges;
  }

  /**
   * Creates an immutable model from this model.
   *
   * @return An immutable vertex as a RuntimeModel
   */
  @Override
  public RuntimeModel build() {
    return new RuntimeModel(this);
  }

  /**
   * <h1>RuntimeModel</h1>
   * Immutable class for Model
   * </p>
   * An immutable model guarantees that that the internal states of
   * the instance will not change after it's construction.
   */
  public static class RuntimeModel extends RuntimeBase {

    private final List<RuntimeVertex> vertices;
    private final List<RuntimeEdge> edges;
    private final List<Element> elementsCache;
    private final Map<Element, List<Element>> elementsByElementCache;
    private final Map<String, List<Element>> elementsByNameCache;
    private final Map<String, List<RuntimeEdge>> edgesByNameCache;
    private final Map<String, List<RuntimeVertex>> verticesByNameCache;
    private final Map<RuntimeVertex, List<RuntimeEdge>> inEdgesByVertexCache;
    private final Map<RuntimeVertex, List<RuntimeEdge>> outEdgesByVertexCache;
    private final Map<String, List<RuntimeVertex>> sharedStateCache;

    private RuntimeModel(Model model) {
      super(model.getId(), model.getName(), model.getActions(), model.getRequirements(), model.getProperties());
      this.vertices = BuilderFactory.build(model.getVertices());
      this.edges = BuilderFactory.build(model.getEdges());
      this.edgesByNameCache = createEdgesByNameCache();
      this.verticesByNameCache = createVerticesByNameCache();
      this.inEdgesByVertexCache = createInEdgesByVertexCache();
      this.outEdgesByVertexCache = createOutEdgesByVertexCache();
      this.elementsCache = createElementCache();
      this.elementsByNameCache = createElementsByNameCache();
      this.elementsByElementCache = createElementsByElementCache(elementsCache, outEdgesByVertexCache);
      this.sharedStateCache = createSharedStateCache();
    }

    /**
     * Gets the list of vertices of the model.
     *
     * @return The list of vertices
     * @see Model#getVertices
     */
    public List<RuntimeVertex> getVertices() {
      return vertices;
    }

    /**
     * Gets the unique list of shared state names.
     *
     * @return The list of shared states.
     * @see Vertex#setSharedState
     */
    public Set<String> getSharedStates() {
      return sharedStateCache.keySet();
    }

    /**
     * Gets the list of vertices that matches a shared state name.
     *
     * @param sharedState The shared state name too be matched.
     * @return The list of matching shared states.
     * @see Vertex#setSharedState
     */
    public List<RuntimeVertex> getSharedStates(String sharedState) {
      return sharedStateCache.get(sharedState);
    }

    /**
     * Will search in the model if there is a vertex that has a shared state name that matches
     * the given name.
     *
     * @param sharedState The shared state name too be matched.
     * @return True if the model has a matching shared state.
     * @see Vertex#setSharedState
     */
    public boolean hasSharedState(String sharedState) {
      return sharedStateCache.containsKey(sharedState);
    }

    /**
     * Will search the model for vertices that has shared states. If any is found, then true will
     * be returned.
     *
     * @return True if the models has any vertex with a shared state.
     * @see Vertex#setSharedState
     */
    public boolean hasSharedStates() {
      return !sharedStateCache.isEmpty();
    }

    /**
     * Searches the model vertices that matches search string.
     *
     * @param name The name of the vertex as a string.
     * @return The list of matching vertices.
     * @see Vertex#setName
     */
    public List<RuntimeVertex> findVertices(String name) {
      return verticesByNameCache.get(name);
    }

    /**
     * For the given vertex, all in-edges will be returned.
     * </p>
     * Any edge that has a target vertex that is identical to vertex, will be returned.
     *
     * @param vertex The vertex to match
     * @return List of matching in-edges.
     */
    public List<RuntimeEdge> getInEdges(RuntimeVertex vertex) {
      return inEdgesByVertexCache.get(vertex);
    }

    /**
     * Gets the all edges in the model.
     *
     * @return A list of edges.
     */
    public List<RuntimeEdge> getEdges() {
      return edges;
    }

    /**
     * For the given vertex, all out-edges will be returned.
     * </p>
     * Any edge that has a source vertex that is identical to vertex, will be returned.
     *
     * @param vertex The vertex to match
     * @return List of matching out-edges.
     */
    public List<RuntimeEdge> getOutEdges(RuntimeVertex vertex) {
      return outEdgesByVertexCache.get(vertex);
    }

    /**
     * Searches the model for edges matching the search string.
     * </p>
     * Any edge that has a matching name, will be returned.
     *
     * @param name The name of edges to be matched.
     * @return The list of matching edges.
     * @see Edge#setName
     */
    public List<RuntimeEdge> findEdges(String name) {
      return edgesByNameCache.get(name);
    }

    /**
     * Searches the model for any element matching the search string.
     * </p>
     * Any element that has a matching name, will be returned. An element can be either an edge
     * or a vertex.
     *
     * @param name The name of elements to be matched.
     * @return The list of matching elements.
     * @see Element#getName
     */
    public List<Element> findElements(String name) {
      return elementsByNameCache.get(name);
    }

    /**
     * Will return a list of all elements in a model.
     * </p>
     * The list will contain all edges and vertices in the model.
     *
     * @return The list of all elements in the model.
     */
    public List<Element> getElements() {
      return elementsCache;
    }

    /**
     * Will return the element with the given id.
     * </p>
     *
     * @param id The id of the element.
     * @return The element with the given id
     */
    public Element getElementById(String id) {
      for (Element element : elementsCache) {
        if (element.getId().equals(id)) {
          return element;
        }
      }
      return null;
    }

    /**
     * TODO: Add doc
     */
    public List<Element> getElements(Element element) {
      return elementsByElementCache.get(element);
    }

    private List<Element> createElementCache() {
      List<Element> elements = new ArrayList<>();
      elements.addAll(vertices);
      elements.addAll(edges);
      return unmodifiableList(elements);
    }

    private Map<Element, List<Element>> createElementsByElementCache(List<Element> elements, Map<RuntimeVertex, List<RuntimeEdge>> outEdges) {
      Map<Element, List<Element>> elementsByElementCache = new HashMap<>();
      for (Element element : elements) {
        if (element instanceof RuntimeEdge) {
          RuntimeEdge edge = (RuntimeEdge) element;
          elementsByElementCache.put(element, Collections.<Element>singletonList(edge.getTargetVertex()));
        } else if (element instanceof RuntimeVertex) {
          RuntimeVertex vertex = (RuntimeVertex) element;
          elementsByElementCache.put(element, cast(outEdges.get(vertex)));
        }
      }
      return makeImmutable(elementsByElementCache);
    }

    @SuppressWarnings("unchecked")
    private List<Element> cast(List<? extends Element> list) {
      return (List<Element>) list;
    }

    private Map<String, List<Element>> createElementsByNameCache() {
      Map<String, List<Element>> elementsByElementCache = new HashMap<>();
      for (Element element : createElementCache()) {
        if (element.hasName()) {
          if (!elementsByElementCache.containsKey(element.getName())) {
            elementsByElementCache.put(element.getName(), new ArrayList<>());
          }
          elementsByElementCache.get(element.getName()).add(element);
        }
      }
      return makeImmutable(elementsByElementCache);
    }

    private Map<String, List<RuntimeEdge>> createEdgesByNameCache() {
      Map<String, List<RuntimeEdge>> edgesByNameCache = new HashMap<>();
      for (RuntimeEdge edge : edges) {
        if (edge.hasName()) {
          if (!edgesByNameCache.containsKey(edge.getName())) {
            edgesByNameCache.put(edge.getName(), new ArrayList<>());
          }
          edgesByNameCache.get(edge.getName()).add(edge);
        }
      }
      return makeImmutable(edgesByNameCache);
    }

    private Map<String, List<RuntimeVertex>> createVerticesByNameCache() {
      Map<String, List<RuntimeVertex>> verticesByNameCache = new HashMap<>();
      for (RuntimeVertex vertex : vertices) {
        if (vertex.hasName()) {
          if (!verticesByNameCache.containsKey(vertex.getName())) {
            verticesByNameCache.put(vertex.getName(), new ArrayList<>());
          }
          verticesByNameCache.get(vertex.getName()).add(vertex);
        }
      }
      return makeImmutable(verticesByNameCache);
    }

    private Map<RuntimeVertex, List<RuntimeEdge>> createInEdgesByVertexCache() {
      Map<RuntimeVertex, List<RuntimeEdge>> inEdgesByVertexCache = new HashMap<>();
      for (RuntimeVertex vertex : vertices) {
        inEdgesByVertexCache.put(vertex, new ArrayList<>());
      }
      for (RuntimeEdge edge : edges) {
        RuntimeVertex vertex = edge.getTargetVertex();
        if (isNotNull(vertex)) {
          inEdgesByVertexCache.get(vertex).add(edge);
        }
      }
      return makeImmutable(inEdgesByVertexCache);
    }

    private Map<RuntimeVertex, List<RuntimeEdge>> createOutEdgesByVertexCache() {
      Map<RuntimeVertex, List<RuntimeEdge>> outEdgesByVertexCache = new HashMap<>();
      for (RuntimeVertex vertex : vertices) {
        outEdgesByVertexCache.put(vertex, new ArrayList<>());
      }
      for (RuntimeEdge edge : edges) {
        RuntimeVertex vertex = edge.getSourceVertex();
        if (isNotNull(vertex)) {
          outEdgesByVertexCache.get(vertex).add(edge);
        }
      }
      return makeImmutable(outEdgesByVertexCache);
    }

    private Map<String, List<RuntimeVertex>> createSharedStateCache() {
      Map<String, List<RuntimeVertex>> sharedStateCache = new HashMap<>();
      for (RuntimeVertex vertex : vertices) {
        if (vertex.hasSharedState()) {
          if (!sharedStateCache.containsKey(vertex.getSharedState())) {
            sharedStateCache.put(vertex.getSharedState(), new ArrayList<>());
          }
          sharedStateCache.get(vertex.getSharedState()).add(vertex);
        }
      }
      return makeImmutable(sharedStateCache);
    }

    private <K, E> Map<K, List<E>> makeImmutable(Map<K, List<E>> source) {
      Map<K, List<E>> map = new HashMap<>();
      for (K key : source.keySet()) {
        map.put(key, unmodifiableList(source.get(key)));
      }
      return unmodifiableMap(map);
    }

    /**
     * TODO: Doc...
     */
    @Override
    public void accept(ElementVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((edges == null) ? 0 : edges.hashCode());
      result = prime
               * result
               + ((edgesByNameCache == null) ? 0 : edgesByNameCache
          .hashCode());
      result = prime
               * result
               + ((elementsByElementCache == null) ? 0
                                                   : elementsByElementCache.hashCode());
      result = prime
               * result
               + ((elementsByNameCache == null) ? 0 : elementsByNameCache
          .hashCode());
      result = prime * result
               + ((elementsCache == null) ? 0 : elementsCache.hashCode());
      result = prime
               * result
               + ((inEdgesByVertexCache == null) ? 0
                                                 : inEdgesByVertexCache.hashCode());
      result = prime
               * result
               + ((outEdgesByVertexCache == null) ? 0
                                                  : outEdgesByVertexCache.hashCode());
      result = prime
               * result
               + ((sharedStateCache == null) ? 0 : sharedStateCache
          .hashCode());
      result = prime * result
               + ((vertices == null) ? 0 : vertices.hashCode());
      result = prime
               * result
               + ((verticesByNameCache == null) ? 0 : verticesByNameCache
          .hashCode());
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (!super.equals(o)) {
        return false;
      }
      RuntimeModel that = (RuntimeModel) o;
      return Objects.equals(vertices, that.vertices) &&
             Objects.equals(edges, that.edges) &&
             Objects.equals(elementsCache, that.elementsCache) &&
             Objects.equals(elementsByElementCache, that.elementsByElementCache) &&
             Objects.equals(elementsByNameCache, that.elementsByNameCache) &&
             Objects.equals(edgesByNameCache, that.edgesByNameCache) &&
             Objects.equals(verticesByNameCache, that.verticesByNameCache) &&
             Objects.equals(inEdgesByVertexCache, that.inEdgesByVertexCache) &&
             Objects.equals(outEdgesByVertexCache, that.outEdgesByVertexCache) &&
             Objects.equals(sharedStateCache, that.sharedStateCache);
    }

  }
}
