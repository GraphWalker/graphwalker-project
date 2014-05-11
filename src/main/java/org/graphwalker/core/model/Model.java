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

import java.util.*;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public final class Model implements Builder<Model.RuntimeModel> {

    private final BuilderSet<RuntimeVertex> vertices = new BuilderSet<>();
    private final BuilderSet<RuntimeEdge> edges = new BuilderSet<>();

    public Model addVertex(Vertex vertex) {
        vertices.add(vertex);
        return this;
    }

    public Model addEdge(Edge edge) {
        edges.add(edge);
        if (null != edge.getSourceVertex()) {
            vertices.add(edge.getSourceVertex());
        }
        if (null != edge.getTargetVertex()) {
            vertices.add(edge.getTargetVertex());
        }
        return this;
    }

    public BuilderSet<RuntimeVertex> getVertices() {
        return vertices;
    }

    public BuilderSet<RuntimeEdge> getEdges() {
        return edges;
    }

    @Override
    public RuntimeModel build() {
        return new RuntimeModel(this);
    }

    public static class RuntimeModel {

        private final List<RuntimeVertex> vertices;
        private final List<RuntimeEdge> edges;
        private final Map<RuntimeVertex, List<RuntimeEdge>> vertexEdgeCache;

        private RuntimeModel(Model model) {
            this.vertices = Collections.unmodifiableList(model.getVertices().build());
            this.edges = Collections.unmodifiableList(model.getEdges().build());
            this.vertexEdgeCache = createVertexEdgeCache();
        }

        public List<RuntimeVertex> getVertices() {
            return vertices;
        }

        public List<RuntimeEdge> getEdges() {
            return edges;
        }

        public List<RuntimeEdge> getEdges(RuntimeVertex vertex) {
            return vertexEdgeCache.get(vertex);
        }

        private Map<RuntimeVertex, List<RuntimeEdge>> createVertexEdgeCache() {
            Map<RuntimeVertex, List<RuntimeEdge>> vertexEdgeCache = new HashMap<>();
            for (RuntimeEdge edge: edges) {
                RuntimeVertex vertex = edge.getSourceVertex();
                if (null != vertex) {
                    if (!vertexEdgeCache.containsKey(vertex)) {
                        vertexEdgeCache.put(vertex, new ArrayList<RuntimeEdge>());
                    }
                    vertexEdgeCache.get(vertex).add(edge);
                }
            }
            Map<RuntimeVertex, List<RuntimeEdge>> unmodifiableVertexEdgeCache = new HashMap<>();
            for (RuntimeVertex vertex: vertexEdgeCache.keySet()) {
                unmodifiableVertexEdgeCache.put(vertex, Collections.unmodifiableList(vertexEdgeCache.get(vertex)));
            }
            return Collections.unmodifiableMap(unmodifiableVertexEdgeCache);
        }
    }
}
