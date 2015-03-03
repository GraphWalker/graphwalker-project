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

import java.util.*;

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
 *
 * @author Nils Olsson
 */
public final class Model implements Builder<Model.RuntimeModel> {

    private String id;
    private String name;
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final Set<Requirement> requirements = new HashSet<>();

    /**
     * Sets the unique identifier of the model. Even though several models in the
     * same test can share the same name, all identifiers must be unique.
     *
     * @param id A String that uniquely identifies this model.
     * @return The model
     */
    public Model setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the unique identifier of the model.
     *
     * @return The unique identifier as a string.
     * @see Model#setId
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the name of the model. The name of a model can be the same as others, it
     * does not have to be unique.
     *
     * @param name The name as a string.
     * @return The model.
     */
    public Model setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name of the model.
     *
     * @return The name as a string.
     * @see Model#setName
     */
    public String getName() {
        return name;
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
        if (null != edge.getSourceVertex() && !vertices.contains(edge.getSourceVertex())) {
            vertices.add(edge.getSourceVertex());
        }
        if (null != edge.getTargetVertex() && !vertices.contains(edge.getTargetVertex())) {
            vertices.add(edge.getTargetVertex());
        }
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
        return addActions(Arrays.asList(action));
    }

    /**
     * Adds a list of actions to the model.
     *
     * @param actions A list of actions
     * @return The model
     * @see Model#addAction
     */
    public Model addActions(List<Action> actions) {
        this.actions.addAll(actions);
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
     * Adds a requirement.
     * </p>
     * The requirement is associated with the whole model. Whenever a test fails any vertex in the model,
     * the requirement will be set to {@link org.graphwalker.core.machine.RequirementStatus FAILED}.
     * </p>
     * Please note that the requirements associated with the model, are not the same as the ones associated
     * with edges and vertices.
     *
     * @param requirement The requirement
     * @return The model
     */
    public Model addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
        return this;
    }

    /**
     * Adds a list of requirements.
     *
     * @param requirements The list of requirements
     * @return The model
     * @see Model#addRequirement
     */
    public Model addRequirements(Set<Requirement> requirements) {
        this.requirements.addAll(requirements);
        return this;
    }

    /**
     * Gets the list of requirement associated with the model.
     *
     * @return The list of requirement
     * @see Model#addRequirement
     */
    public Set<Requirement> getRequirements() {
        return requirements;
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
    public static class RuntimeModel extends ElementBase {

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
            super(model.getId(), model.getName(), model.getActions(), model.getRequirements());
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

        public List<RuntimeVertex> getSharedStates(String sharedState) {
            return sharedStateCache.get(sharedState);
        }

        public boolean hasSharedState(String sharedState) {
            return sharedStateCache.containsKey(sharedState);
        }

        public boolean hasSharedStates() {
            return !sharedStateCache.isEmpty();
        }

        public List<RuntimeVertex> findVertices(String name) {
            return verticesByNameCache.get(name);
        }

        public List<RuntimeEdge> getInEdges(RuntimeVertex vertex) {
            return inEdgesByVertexCache.get(vertex);
        }

        public List<RuntimeEdge> getEdges() {
            return edges;
        }

        public List<RuntimeEdge> getOutEdges(RuntimeVertex vertex) {
            return outEdgesByVertexCache.get(vertex);
        }

        public List<RuntimeEdge> findEdges(String name) {
            return edgesByNameCache.get(name);
        }

        public List<Element> findElements(String name) {
            return elementsByNameCache.get(name);
        }

        public List<Element> getElements() {
            return elementsCache;
        }

        public List<Element> getElements(Element element) {
            return elementsByElementCache.get(element);
        }

        private List<Element> createElementCache() {
            List<Element> elements = new ArrayList<>();
            elements.addAll(vertices);
            elements.addAll(edges);
            return Collections.unmodifiableList(elements);
        }

        private Map<Element, List<Element>> createElementsByElementCache(List<Element> elements, Map<RuntimeVertex, List<RuntimeEdge>> outEdges) {
            Map<Element, List<Element>> elementsByElementCache = new HashMap<>();
            for (Element element : elements) {
                if (element instanceof RuntimeEdge) {
                    RuntimeEdge edge = (RuntimeEdge) element;
                    elementsByElementCache.put(element, Arrays.<Element>asList(edge.getTargetVertex()));
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
                        elementsByElementCache.put(element.getName(), new ArrayList<Element>());
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
                        edgesByNameCache.put(edge.getName(), new ArrayList<RuntimeEdge>());
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
                        verticesByNameCache.put(vertex.getName(), new ArrayList<RuntimeVertex>());
                    }
                    verticesByNameCache.get(vertex.getName()).add(vertex);
                }
            }
            return makeImmutable(verticesByNameCache);
        }

        private Map<RuntimeVertex, List<RuntimeEdge>> createInEdgesByVertexCache() {
            Map<RuntimeVertex, List<RuntimeEdge>> inEdgesByVertexCache = new HashMap<>();
            for (RuntimeVertex vertex : vertices) {
                inEdgesByVertexCache.put(vertex, new ArrayList<RuntimeEdge>());
            }
            for (RuntimeEdge edge : edges) {
                RuntimeVertex vertex = edge.getTargetVertex();
                if (null != vertex) {
                    inEdgesByVertexCache.get(vertex).add(edge);
                }
            }
            return makeImmutable(inEdgesByVertexCache);
        }

        private Map<RuntimeVertex, List<RuntimeEdge>> createOutEdgesByVertexCache() {
            Map<RuntimeVertex, List<RuntimeEdge>> outEdgesByVertexCache = new HashMap<>();
            for (RuntimeVertex vertex : vertices) {
                outEdgesByVertexCache.put(vertex, new ArrayList<RuntimeEdge>());
            }
            for (RuntimeEdge edge : edges) {
                RuntimeVertex vertex = edge.getSourceVertex();
                if (null != vertex) {
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
                        sharedStateCache.put(vertex.getSharedState(), new ArrayList<RuntimeVertex>());
                    }
                    sharedStateCache.get(vertex.getSharedState()).add(vertex);
                }
            }
            return makeImmutable(sharedStateCache);
        }

        private <K, E> Map<K, List<E>> makeImmutable(Map<K, List<E>> source) {
            Map<K, List<E>> map = new HashMap<>();
            for (K key : source.keySet()) {
                map.put(key, Collections.unmodifiableList(source.get(key)));
            }
            return Collections.unmodifiableMap(map);
        }

        @Override
        public void accept(ElementVisitor visitor) {
            visitor.visit(this);
        }

    }
}
