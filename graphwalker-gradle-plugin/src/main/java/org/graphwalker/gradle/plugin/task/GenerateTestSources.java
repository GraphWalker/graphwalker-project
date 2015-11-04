package org.graphwalker.gradle.plugin.task;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
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
                SourceSetContainer sourceSets = (SourceSetContainer) getProject().getProperties().get("sourceSets");
                SourceSet sourceSet = sourceSets.getByName("test");
                sourceSet.getJava().srcDir(getOutputDirectory());
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
                SourceSetContainer sourceSets = (SourceSetContainer) getProject().getProperties().get("sourceSets");
                SourceSet sourceSet = sourceSets.getByName("test");
                resources.addAll(sourceSet.getResources().getSrcDirs());
            }
        });
        return resources;
    }
}
