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
import org.gradle.api.*;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.graphwalker.gradle.plugin.task.*;

/**
 * @author Nils Olsson
 */
public class GraphWalkerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(BasePlugin.class);
        project.getPlugins().apply(JavaBasePlugin.class);
        configure(project, project.getExtensions().create("graphwalker", GraphWalkerExtension.class));
    }

    private <T extends TaskBase> T create(Project project, Class<T> type, String name, String description) {
        T task = project.getTasks().create(name, type);
        task.setGroup("graphwalker");
        task.setDescription(description);
        return task;
    }

    private void configure(final Project project, final GraphWalkerExtension extension) {
        //create(project, ValidateModels.class, "validateModels", "");
        //create(project, ValidateTestModels.class, "validateTestModels", "");
        final Task generateSources = create(project, GenerateSources.class, "generateSources", "").configure();
        final Task generateTestSources = create(project, GenerateTestSources.class, "generateTestSources", "").configure();
        //create(project, ExecuteTests.class, "executeTests", "");
        //create(project, Watch.class, "watch", "");
        final Task clean = create(project, Clean.class, "gwClean", "").configure();
        project.getPlugins().withType(BasePlugin.class, new Action<BasePlugin>() {
            @Override
            public void execute(BasePlugin plugin) {
                project.getTasks().getByName(BasePlugin.CLEAN_TASK_NAME).dependsOn(clean);
            }
        });
        project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            @Override
            public void execute(JavaPlugin plugin) {
                project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(generateSources);
                project.getTasks().getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).dependsOn(generateTestSources);
            }
        });
    }
}
