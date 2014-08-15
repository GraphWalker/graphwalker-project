package org.graphwalker.java.test;

/*
 * #%L
 * GraphWalker Java
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

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.model.Model;
import org.graphwalker.io.factory.ModelFactory;
import org.graphwalker.io.factory.YEdModelFactory;
import org.graphwalker.java.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Nils Olsson
 */
public final class Execution {

    private final Class<?> testClass;
    private final Class<? extends PathGenerator> pathGenerator;
    private final Class<? extends StopCondition> stopCondition;
    private final String stopConditionValue;
    private final Model model;
    private final String start;

    public Execution(final Class<?> testClass, final Class<? extends PathGenerator> pathGenerator, final Class<? extends StopCondition> stopCondition, String stopConditionValue, String start) {
        this.testClass = testClass;
        this.stopCondition = stopCondition;
        this.pathGenerator = pathGenerator;
        this.stopConditionValue = stopConditionValue;
        this.model = createModel(testClass);
        this.start = start;
    }

    private Model createModel(final Class<?> testClass) {
        ModelFactory factory = new YEdModelFactory();
        for (Annotation annotation: AnnotationUtils.getAnnotations(testClass, org.graphwalker.java.annotation.Model.class)) {
            Path file = Paths.get(((org.graphwalker.java.annotation.Model) annotation).file());
            if (factory.accept(file)) {
                return factory.create(file.toString());
            }
        }
        throw new RuntimeException(); // TODO:
    }

    public String getName() {
        return testClass.getName();
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Class<? extends PathGenerator> getPathGenerator() {
        return pathGenerator;
    }

    public Class<? extends StopCondition> getStopCondition() {
        return stopCondition;
    }

    public String getStopConditionValue() {
        return stopConditionValue;
    }

    public Model getModel() {
        return model;
    }

    public String getStart() {
        return start;
    }
}
