package org.graphwalker.gradle.plugin.task;

/*
 * #%L
 * GraphWalker Gradle Plugin
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
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

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.graphwalker.java.test.Configuration;
import org.graphwalker.java.test.IsolatedClassLoader;
import org.graphwalker.java.test.Reflector;
import org.graphwalker.java.test.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils Olsson
 */
public class ExecuteTests extends TaskBase {

    @Override
    public Task configure() {
        getOutputs().upToDateWhen(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task element) {
                return false;
            }
        });
        return this;
    }

    @TaskAction
    public void executeTests() {

        ClassLoader classLoader = new IsolatedClassLoader(getClasspathElements());
        Configuration configuration = createConfiguration();
        Reflector reflector = new Reflector(configuration, classLoader);
        Result result = reflector.execute();
        for (String error : result.getErrors()) {
            System.out.println(error);
        }
        System.out.println("Result: [" + result.getCompletedCount() + ", " + result.getFailedCount() + "]");

    }

    private Configuration createConfiguration() {
        return new Configuration();
    }

    private List<String> getClasspathElements() {
        final List<String> elements = new ArrayList<>();
        getProject().getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            @Override
            public void execute(JavaPlugin plugin) {
                SourceSetContainer sourceSets = (SourceSetContainer) getProject().getProperties().get("sourceSets");
                SourceSet sourceSet = sourceSets.getByName("test");
                for (File file : sourceSet.getRuntimeClasspath().getFiles()) {
                    elements.add(file.getAbsolutePath());
                }
            }
        });
        return elements;
    }
}
