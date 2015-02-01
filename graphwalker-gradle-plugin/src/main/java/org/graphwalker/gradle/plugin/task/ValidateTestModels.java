package org.graphwalker.gradle.plugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * @author Nils Olsson
 */
public class ValidateTestModels extends DefaultTask {

    @TaskAction
    public void validateTestModels() {
        System.out.println("ValidateModels called");
    }
}
