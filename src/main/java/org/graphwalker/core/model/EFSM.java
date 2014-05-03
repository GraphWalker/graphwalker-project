package org.graphwalker.core.model;

import org.graphwalker.core.model.efsm.Edge;
import org.graphwalker.core.model.efsm.Vertex;

import java.util.*;

/**
 * @author Nils Olsson
 */
public final class EFSM {

    private final List<Vertex> vertices;
    private final List<Edge> edges;

    private EFSM(Builder builder) {
        this.vertices = Collections.unmodifiableList(builder.vertices.build());
        this.edges = Collections.unmodifiableList(builder.edges.build());
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public static class Builder implements ModelBuilder<EFSM> {

        protected final ModelBuilderSet<Vertex.Builder, Vertex> vertices = new ModelBuilderSet<>();
        protected final ModelBuilderSet<Edge.Builder, Edge> edges = new ModelBuilderSet<>();

        public Builder add(Edge.Builder edge) {
            edges.add(edge);
            vertices.add(edge.source);
            vertices.add(edge.target);
            return this;
        }

        @Override
        public EFSM build() {
            return new EFSM(this);
        }
    }
}
