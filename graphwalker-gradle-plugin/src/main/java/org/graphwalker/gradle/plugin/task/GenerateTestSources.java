package org.graphwalker.gradle.plugin.task;

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
public class GenerateTestSources extends GenerateBase {

    @OutputDirectory
    private File outputDirectory = new File(getProject().getProjectDir(), "graphwalker/generated-test-sources");

    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public Task configure() {
        getProject().getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            @Override
            public void execute(JavaPlugin plugin) {
                SourceSetContainer sourceSets = (SourceSetContainer)getProject().getProperties().get("sourceSets");
                SourceSet sourceSet = sourceSets.getByName("test");
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
                    Method getTestSourceSet = Reflections.getMethod(configData.getClass(), "getTestSourceSet");
                    Object sourceSet = Reflections.invoke(configData, getTestSourceSet);
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
                SourceSet sourceSet = sourceSets.getByName("test");
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
                    Method getTestSourceSet = Reflections.getMethod(configData.getClass(), "getTestSourceSet");
                    Object sourceSet = Reflections.invoke(configData, getTestSourceSet);
                    Method getResources = Reflections.getMethod(sourceSet.getClass(), "getResources");
                    Object resource = Reflections.invoke(sourceSet, getResources);
                    Method getSrcDirs = Reflections.getMethod(resource.getClass(), "getSrcDirs");
                    Object srcDirs = Reflections.invoke(resource, getSrcDirs);
                    Method addAll = Reflections.getMethod(List.class, "addAll", Collection.class);
                    Reflections.invoke(resources, addAll, srcDirs);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        return resources;
    }
}
