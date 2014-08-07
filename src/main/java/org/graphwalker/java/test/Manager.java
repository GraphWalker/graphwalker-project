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

import org.codehaus.plexus.util.SelectorUtils;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.java.annotation.GraphWalker;

import java.util.*;

/**
 * @author Nils Olsson
 */
public final class Manager {

    private final Configuration configuration;
    private final Collection<Group> executionGroups;

    public Manager(Configuration configuration) {
        this.configuration = configuration;
        Collection<Class<?>> testClasses = new Scanner().scan(configuration.getTestClassesDirectory(), configuration.getClassesDirectory());
        this.executionGroups = createExecutionGroups(filterTestClasses(testClasses));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Collection<Group> getExecutionGroups() {
        return executionGroups;
    }

    private Collection<Class<?>> filterTestClasses(Collection<Class<?>> testClasses) {
        Set<Class<?>> filteredClasses = new HashSet<>(testClasses.size());
        for (Class<?> testClass: testClasses) {
            if (isIncluded(testClass)) {
                filteredClasses.add(testClass);
            }
        }
        return filteredClasses;
    }

    private boolean isIncluded(Class<?> testClass) {
        String name = testClass.getName();
        for (String excluded: configuration.getExcludes()) {
            if (SelectorUtils.match(excluded, name, true)) {
                return false;
            }
        }
        for (String included: configuration.getIncludes()) {
            if (SelectorUtils.match(included, name, true)) {
                return true;
            }
        }
        return false;
    }

    private Collection<Group> createExecutionGroups(Collection<Class<?>> testClasses) {
        Map<String, Group> groups = new HashMap<>();
        for (Class<?> testClass: testClasses) {
            GraphWalker configuration = testClass.getAnnotation(GraphWalker.class);
            for (String name: configuration.groups()) {
                if (!groups.containsKey(name)) {
                    groups.put(name, new Group(name));
                }
                // TODO: Implement a way to configure the test, like the cli module does it
                Execution execution = new Execution(testClass, configuration.pathGenerator()
                        , configuration.stopCondition(), configuration.stopConditionValue(), configuration.start());
                groups.get(name).addExecution(execution);
            }
        }
        return groups.values();
    }

    public int getGroupCount() {
        return getExecutionGroups().size();
    }

    public int getTestCount() {
        int count = 0;
        for (Group group: getExecutionGroups()) {
            count += group.getExecutions().size();
        }
        return count;
    }
}
