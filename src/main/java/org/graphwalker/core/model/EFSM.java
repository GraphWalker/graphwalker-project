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

import org.graphwalker.core.model.efsm.Edge;
import org.graphwalker.core.model.efsm.Vertex;

import java.util.Collections;
import java.util.List;

import static org.graphwalker.core.model.efsm.Edge.EdgeBuilder;
import static org.graphwalker.core.model.efsm.Vertex.VertexBuilder;

/**
 * @author Nils Olsson
 */
public final class EFSM implements Model {

    private final List<Vertex> vertices;
    private final List<Edge> edges;

    private EFSM(EFSMBuilder builder) {
        this.vertices = Collections.unmodifiableList(builder.getVertices().build());
        this.edges = Collections.unmodifiableList(builder.getEdges().build());
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public static class EFSMBuilder implements org.graphwalker.core.model.Builder<EFSM> {

        private final BuilderSet<VertexBuilder, Vertex> vertices = new BuilderSet<>();
        private final BuilderSet<EdgeBuilder, Edge> edges = new BuilderSet<>();

        public EFSMBuilder add(VertexBuilder vertex) {
            vertices.add(vertex);
            return this;
        }

        public EFSMBuilder add(EdgeBuilder edge) {
            edges.add(edge);
            vertices.add(edge.getSourceVertex());
            vertices.add(edge.getTargetVertex());
            return this;
        }

        public BuilderSet<VertexBuilder, Vertex> getVertices() {
            return vertices;
        }

        public BuilderSet<EdgeBuilder, Edge> getEdges() {
            return edges;
        }

        @Override
        public EFSM build() {
            return new EFSM(this);
        }
    }
}
