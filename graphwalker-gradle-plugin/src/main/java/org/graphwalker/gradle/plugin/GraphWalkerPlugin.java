package org.graphwalker.gradle.plugin;

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
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.graphwalker.gradle.plugin.task.*;

import java.io.File;

/**
 * @author Nils Olsson
 */
public class GraphWalkerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        GraphWalkerExtension extension = project.getExtensions().create("graphwalker", GraphWalkerExtension.class);
        configure(project, extension);
    }

    private void configure(final Project project, final GraphWalkerExtension extension) {

        final ValidateModels validateModels = project.getTasks().create("validateModels", ValidateModels.class);
        validateModels.setGroup("graphwalker");
        validateModels.setDescription("");

        final ValidateTestModels validateTestModels = project.getTasks().create("validateTestModels", ValidateTestModels.class);
        validateTestModels.setGroup("graphwalker");
        validateTestModels.setDescription("");

        final GenerateSources generateSources = project.getTasks().create("generateSources", GenerateSources.class);
        generateSources.setGroup("graphwalker");
        generateSources.setDescription("");

        final GenerateTestSources generateTestSources = project.getTasks().create("generateTestSources", GenerateTestSources.class);
        generateTestSources.setGroup("graphwalker");
        generateTestSources.setDescription("");

        final ExecuteTests executeTests = project.getTasks().create("executeTests", ExecuteTests.class);
        executeTests.setGroup("graphwalker");
        executeTests.setDescription("");

        final Watch watch = project.getTasks().create("watch", Watch.class);
        watch.setGroup("graphwalker");
        watch.setDescription("");

        final Clean clean = project.getTasks().create("gwClean", Clean.class);
        clean.setGroup("graphwalker");
        clean.setDescription("");


        project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {

            @Override
            public void execute(JavaPlugin plugin) {
                final SourceSetContainer sourceSets = (SourceSetContainer)project.getProperties().get("sourceSets");
                sourceSets.getByName("main").getJava().srcDir(generateSources.getGeneratedSources());
                final Task processResources = project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME);
                processResources.dependsOn(generateSources);

            }
        });
    }
}
