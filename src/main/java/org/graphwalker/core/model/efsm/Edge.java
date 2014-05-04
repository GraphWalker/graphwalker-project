package org.graphwalker.core.model.efsm;

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
