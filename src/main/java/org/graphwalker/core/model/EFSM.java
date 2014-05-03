package org.graphwalker.core.model;

/**
 * @author Nils Olsson
 */
public final class EFSM {

    private EFSM(Builder builder) {

    }

    public static class Builder implements ModelBuilder<EFSM> {

        @Override
        public EFSM build() {
            return new EFSM(this);
        }
    }
}
