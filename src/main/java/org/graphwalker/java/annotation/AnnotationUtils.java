package org.graphwalker.java.annotation;

import org.reflections.Reflections;

import java.util.Set;

/**
 * @author Nils Olsson
 */
public final class AnnotationUtils {

    private AnnotationUtils() {}

    public static Set<Class<?>> findTests() {
        return findTests("");
    }

    public static Set<Class<?>> findTests(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getTypesAnnotatedWith(GraphWalker.class);
    }

}
