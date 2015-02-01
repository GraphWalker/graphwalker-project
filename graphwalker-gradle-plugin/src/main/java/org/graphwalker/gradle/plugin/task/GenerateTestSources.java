package org.graphwalker.gradle.plugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.graphwalker.java.source.CodeGenerator;

import java.io.File;

/**
 * @author Nils Olsson
 */
public class GenerateTestSources extends DefaultTask {

    @InputDirectory
    private File resources = new File("src/test/resources");

    @OutputDirectory
    private File generatedTestSources = new File(getProject().getProjectDir(), "generated-test-sources/graphwalker");

    @TaskAction
    public void generateTestSources() {
        CodeGenerator.generate(resources.toPath(), generatedTestSources.toPath());
    }
}
