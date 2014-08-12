package org.graphwalker.core.model;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Nils Olsson
 */
public final class Vertex extends CachedBuilder<Vertex.RuntimeVertex> {

    private String name;
    private List<Requirement> requirements = new ArrayList<>();
    private String sharedState;
    private String id;
    private boolean startVertex = false;

    public Vertex setName(String name) {
        this.name = name;
        invalidateCache();
        return this;
    }

    public String getName() {
        return name;
    }

    public Vertex addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
        invalidateCache();
        return this;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public String getSharedState() {
        return sharedState;
    }

    public Vertex setSharedState(String sharedState) {
        this.sharedState = sharedState;
        invalidateCache();
        return this;
    }

    public String getId() {
        return id;
    }

    public Vertex setId(String id) {
        this.id = id;
        invalidateCache();
        return this;
    }

    public boolean isStartVertex() {
        return startVertex;
    }

    public Vertex setStartVertex(boolean startVertex) {
        this.startVertex = startVertex;
        invalidateCache();
        return this;
    }

    @Override
    protected RuntimeVertex createCache() {
        return new RuntimeVertex(this);
    }

    public static final class RuntimeVertex extends NamedElement {

        private final List<Requirement> requirements;
        private final String sharedState;
        private final String id;
        private final boolean startVertex;

        private RuntimeVertex(Vertex vertex) {
            super(vertex.getName());
            this.requirements = Collections.unmodifiableList(vertex.getRequirements());
            this.sharedState = vertex.getSharedState();
            this.id = vertex.getId();
            this.startVertex = vertex.isStartVertex();
        }

        public List<Requirement> getRequirements() {
            return requirements;
        }

        public String getId() {
            return id;
        }

        public String getSharedState() {
            return sharedState;
        }

        public boolean hasSharedState() {
            return null != sharedState && !"".equals(sharedState);
        }

        public boolean isStartVertex() {
            return startVertex;
        }

        @Override
        public void accept(ElementVisitor visitor) {
            visitor.visit(this);
        }
    }
}
