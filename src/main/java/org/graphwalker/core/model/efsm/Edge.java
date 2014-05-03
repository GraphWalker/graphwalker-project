package org.graphwalker.core.model.efsm;

import org.graphwalker.core.model.ModelBuilder;

/**
 * @author Nils Olsson
 */
public final class Edge {

    private final Vertex source;
    private final Vertex target;

    private Edge(Builder builder) {
        this.source = builder.source.build();
        this.target = builder.target.build();
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    public static class Builder implements ModelBuilder<Edge> {

        public Vertex.Builder source;
        public Vertex.Builder target;

        public Builder source(Vertex.Builder vertex) {
            this.source = vertex;
            return this;
        }

        public Builder target(Vertex.Builder vertex) {
            this.target = vertex;
            return this;
        }

        @Override
        public Edge build() {
            return new Edge(this);
        }
    }
}
