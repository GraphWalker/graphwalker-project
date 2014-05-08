package org.graphwalker.core.model.efsm;

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

import org.graphwalker.core.model.CachedBuilder;
import org.graphwalker.core.model.NamedElement;

import static org.graphwalker.core.model.efsm.Vertex.ImmutableVertex;

/**
 * @author Nils Olsson
 */
public final class Edge extends CachedBuilder<Edge.ImmutableEdge> {

    private String name;
    private Vertex sourceVertex;
    private Vertex targetVertex;

    public Edge setName(String name) {
        this.name = name;
        invalidateCache();
        return this;
    }

    public String getName() {
        return name;
    }

    public Edge setSourceVertex(Vertex vertex) {
        this.sourceVertex = vertex;
        invalidateCache();
        return this;
    }

    public Vertex getSourceVertex() {
        return sourceVertex;
    }

    public Edge setTargetVertex(Vertex vertex) {
        this.targetVertex = vertex;
        invalidateCache();
        return this;
    }

    public Vertex getTargetVertex() {
        return targetVertex;
    }

    @Override
    protected ImmutableEdge createCache() {
        return new ImmutableEdge(this);
    }

    public static final class ImmutableEdge extends NamedElement {

        private final ImmutableVertex sourceVertex;
        private final ImmutableVertex targetVertex;

        private ImmutableEdge(Edge edge) {
            super(edge.getName());
            if (null != edge.getSourceVertex()) {
                this.sourceVertex = edge.getSourceVertex().build();
            } else {
                this.sourceVertex = null;
            }
            if (null != edge.getTargetVertex()) {
                this.targetVertex = edge.getTargetVertex().build();
            } else {
                this.targetVertex = null;
            }
        }

        public ImmutableVertex getSourceVertex() {
            return sourceVertex;
        }

        public ImmutableVertex getTargetVertex() {
            return targetVertex;
        }
    }
}
