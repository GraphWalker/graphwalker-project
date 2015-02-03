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
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.graphwalker.java.test.Reflections;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Nils Olsson
 */
public class GenerateSources extends GenerateBase {

    @OutputDirectory
    private File outputDirectory = new File(getProject().getProjectDir(), "graphwalker/generated-sources");

    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public Task configure() {
        getProject().getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            @Override
            public void execute(JavaPlugin plugin) {
                SourceSetContainer sourceSets = (SourceSetContainer)getProject().getProperties().get("sourceSets");
                SourceSet sourceSet = sourceSets.getByName("main");
                sourceSet.getJava().srcDir(getOutputDirectory());
            }
        });
        getProject().getPlugins().withId("android", new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                try {
                    Class<?> pluginClass = Class.forName("com.android.build.gradle.AppPlugin");
                    Method getDefaultConfigData = Reflections.getMethod(pluginClass, "getDefaultConfigData");
                    Object configData = Reflections.invoke(plugin, getDefaultConfigData);
                    Method getSourceSet = Reflections.getMethod(configData.getClass(), "getSourceSet");
                    Object sourceSet = Reflections.invoke(configData, getSourceSet);
                    Method getJava = Reflections.getMethod(sourceSet.getClass(), "getJava");
                    Object java = Reflections.invoke(sourceSet, getJava);
                    Method srcDir = Reflections.getMethod(java.getClass(), "srcDir", Object.class);
                    Reflections.invoke(java, srcDir, getOutputDirectory());
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        });
        return this;
    }

    @Override
    public List<File> getResources() {
        final List<File> resources = new ArrayList<>();
        getProject().getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            @Override
            public void execute(JavaPlugin plugin) {
                SourceSetContainer sourceSets = (SourceSetContainer)getProject().getProperties().get("sourceSets");
                SourceSet sourceSet = sourceSets.getByName("main");
                resources.addAll(sourceSet.getResources().getSrcDirs());
            }
        });
        getProject().getPlugins().withId("android", new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                try {
                    Class<?> pluginClass = Class.forName("com.android.build.gradle.AppPlugin");
                    Method getDefaultConfigData = Reflections.getMethod(pluginClass, "getDefaultConfigData");
                    Object configData = Reflections.invoke(plugin, getDefaultConfigData);
                    Method getSourceSet = Reflections.getMethod(configData.getClass(), "getSourceSet");
                    Object sourceSet = Reflections.invoke(configData, getSourceSet);
                    Method getResources = Reflections.getMethod(sourceSet.getClass(), "getResources");
                    Object resource = Reflections.invoke(sourceSet, getResources);
                    Method getSrcDirs = Reflections.getMethod(resource.getClass(), "getSrcDirs");
                    Object srcDirs = Reflections.invoke(resource, getSrcDirs);
                    Method addAll = Reflections.getMethod(List.class, "addAll", Collection.class);
                    Reflections.invoke(resources, addAll, srcDirs);
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        });
        return resources;
    }
}
