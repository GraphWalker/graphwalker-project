package org.graphwalker.java.annotation;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import java.util.Set;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class AnnotationUtilsTest {

    @Test
    public void findTest() {
        Reflections reflections = new Reflections("org.graphwalker");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(GraphWalker.class);
        Assert.assertThat(classes.size(), is(1));
    }
}
