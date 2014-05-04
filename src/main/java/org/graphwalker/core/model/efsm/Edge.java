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

import org.graphwalker.core.model.Builder;
import org.graphwalker.core.model.Element;
import static org.graphwalker.core.model.efsm.Vertex.VertexBuilder;
/**
 * @author Nils Olsson
 */
public final class Edge implements Element {

    private final Vertex sourceVertex;
    private final Vertex targetVertex;

    private Edge(EdgeBuilder builder) {
        this.sourceVertex = builder.getSourceVertex().build();
        this.targetVertex = builder.getTargetVertex().build();
    }

    public Vertex getSourceVertex() {
        return sourceVertex;
    }

    public Vertex getTargetVertex() {
        return targetVertex;
    }

    public static class EdgeBuilder implements Builder<Edge> {

        private VertexBuilder sourceVertex;
        private VertexBuilder targetVertex;

        public EdgeBuilder setSourceVertex(VertexBuilder vertex) {
            this.sourceVertex = vertex;
            return this;
        }

        public EdgeBuilder setTargetVertex(VertexBuilder vertex) {
            this.targetVertex = vertex;
            return this;
        }

        public VertexBuilder getSourceVertex() {
            return sourceVertex;
        }

        public VertexBuilder getTargetVertex() {
            return targetVertex;
        }

        @Override
        public Edge build() {
            return new Edge(this);
        }
    }
}
