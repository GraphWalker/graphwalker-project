package org.graphwalker.gradle.plugin.task;

import org.gradle.api.tasks.TaskAction;
import org.graphwalker.java.source.CodeGenerator;

import java.io.File;
import java.util.List;

/**
 * @author Nils Olsson
 */
public abstract class GenerateBase extends TaskBase {

    public abstract File getOutputDirectory();

    public abstract List<File> getResources();

    @TaskAction
    public void generateSources() {
        for (File resource: getResources()) {
            CodeGenerator.generate(resource.toPath(), getOutputDirectory().toPath());
        }
    }
}
