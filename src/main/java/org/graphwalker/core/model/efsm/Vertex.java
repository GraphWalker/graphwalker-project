package org.graphwalker.core.model.efsm;

import org.graphwalker.core.model.ModelBuilder;

/**
 * @author Nils Olsson
 */
public final class Vertex {

    private Vertex(Builder builder) {
    }

    public static class Builder implements ModelBuilder<Vertex> {

        private Vertex vertex = null;

        @Override
        public Vertex build() {
            if (null == vertex) {
                vertex = new Vertex(this);
            }
            return vertex;
        }
    }
}
