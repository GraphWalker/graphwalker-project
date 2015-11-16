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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.common.Objects.isNotNullOrEmpty;
import static org.graphwalker.core.common.Objects.isNull;
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
public final class Edge extends CachedBuilder<Edge.RuntimeEdge> {

    private String id;
    private String name;
    private Vertex sourceVertex;
    private Vertex targetVertex;
    private Guard guard;
    private final List<Action> actions = new ArrayList<>();
    private final Set<Requirement> requirements = new HashSet<>();
    private Double weight = 0.0;
    private final Map<String, Object> properties = new HashMap<>();

    /**
     * Sets the unique identifier of the edge. Even though several edges in the
     * same model can share the same name, all identifiers must be unique.
     *
     * @param id A String that uniquely identifies this edge.
     * @return The edge.
     */
    public Edge setId(String id) {
        this.id = id;
        invalidateCache();
        return this;
    }

    /**
     * Gets the unique identifier of the edge.
     *
     * @return The unique identifier as a string.
     * @see Edge#setId
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the name of the edge. The name of an edge can be shared by other edges, it
     * does not have to be unique.
     *
     * @param name The name as a string.
     * @return The edge.
     */
    public Edge setName(String name) {
        this.name = name;
        invalidateCache();
        return this;
    }

    /**
     * Gets the name of the edge.
     *
     * @return The name as a string.
     * @see Edge#setName
     */
    public String getName() {
        return name;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public Edge setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Edge setProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    /**
     * Adds a requirement.
     * The requirement is used by the target vertex. It will never be used by the edge itself.
     * In some cases, the requirement being verified by some vertex is dependent on which in-edge
     * has been traversed.
     * </p>
     * In the example below, the vertex <strong>v_MainView</strong> does not have any requirements associated
     * to it. The requirement that is going to be verified is decided by the in-edge.<br>
     * So, for example, if we walking over the <strong>v_LoginDialog</strong>, the requirement that is going to be
     * verified in <strong>v_MainView</strong>, will be <strong>UC 2.1.1</strong>.
     * </p>
     * <img src="doc-files/Edge.addRequirement.png">
     * </p>
     *
     * @param requirement The requirement.
     * @return The Edge
     */
    public Edge addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
        invalidateCache();
        return this;
    }

    /**
     * Adds a list of requirements.
     *
     * @param requirements The list of requirements.
     * @return The edge.
     * @see Edge#addRequirement
     */
    public Edge addRequirements(Set<Requirement> requirements) {
        this.requirements.addAll(requirements);
        invalidateCache();
        return this;
    }

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
     * Gets the list of requirements.
     *
     * @return The list of requirements.
     * @see Edge#addRequirement
     */
    public Set<Requirement> getRequirements() {
        return requirements;
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

    /**
     * Adds a list of actions to the edge, which represents a pieces of code that will be executed
     * each time the edge is being traversed. The code snippets is by default interpreted as javascript.
     *
     * @param actions The actions.
     * @return The edge.
     * @see Edge#addAction
     */
    public Edge addActions(List<Action> actions) {
        this.actions.addAll(actions);
        invalidateCache();
        return this;
    }

    /**
     * Gets the lists of actions of the edge.
     *
     * @return The actions
     * @see Edge#addActions
     */
    public List<Action> getActions() {
        return actions;
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
     * Creates a representation of the edge as a string.
     *
     * @return The edge as a string.
     */
    public String toString() {
        return "{ id: " + getId() + ", name: " + getName() + "}";
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
     * <h1>RuntimeEdge</h1>
     * Immutable class for Edge
     * </p>
     * This class is used in models. It guarantees that that the internal states of
     * the instance will not change after it's construction.
     * </p>
     */
    public static final class RuntimeEdge extends RuntimeBase {

        private final RuntimeVertex sourceVertex;
        private final RuntimeVertex targetVertex;
        private final Guard guard;
        private final Double weight;

        private RuntimeEdge(Edge edge) {
            super(edge.getId(), edge.getName(), edge.getActions(), edge.getRequirements(), edge.getProperties());
            this.sourceVertex = build(edge.getSourceVertex());
            this.targetVertex = build(edge.getTargetVertex());
            this.guard = edge.getGuard();
            this.weight = edge.getWeight();
        }

        private <T> T build(Builder<T> builder) {
            return (isNotNull(builder) ? builder.build() : null);
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
         *
         * @param visitor
         */
        @Override
        public void accept(ElementVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (isNull(o) || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            RuntimeEdge that = (RuntimeEdge) o;
            return Objects.equals(sourceVertex, that.sourceVertex) &&
                    Objects.equals(targetVertex, that.targetVertex) &&
                    Objects.equals(guard, that.guard) &&
                    Objects.equals(weight, that.weight);
        }
    }
}
