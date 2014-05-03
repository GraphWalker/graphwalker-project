package org.graphwalker.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Nils Olsson
 */
public final class ModelBuilderSet<T extends ModelBuilder<E>, E> extends HashSet<T> implements ModelBuilder<List<E>> {

    @Override
    public List<E> build() {
        List<E> elements = new ArrayList<>();
        for (T builder: this) {
            elements.add(builder.build());
        }
        return elements;
    }
}
