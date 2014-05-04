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

import org.graphwalker.core.model.ModelBuilder;

/**
 * @author Nils Olsson
 */
public final class Edge {

    private final Vertex source;
    private final Vertex target;

    private Edge(Builder builder) {
        this.source = builder.getSource().build();
        this.target = builder.getTarget().build();
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    public static class Builder implements ModelBuilder<Edge> {

        private Vertex.Builder source;
        private Vertex.Builder target;

        public Builder setSource(Vertex.Builder vertex) {
            this.source = vertex;
            return this;
        }

        public Builder setTarget(Vertex.Builder vertex) {
            this.target = vertex;
            return this;
        }

        public Vertex.Builder getSource() {
            return source;
        }

        public Vertex.Builder getTarget() {
            return target;
        }

        @Override
        public Edge build() {
            return new Edge(this);
        }
    }
}
