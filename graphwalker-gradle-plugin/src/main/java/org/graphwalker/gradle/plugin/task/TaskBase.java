package org.graphwalker.gradle.plugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;

/**
 * @author Nils Olsson
 */
public abstract class TaskBase extends DefaultTask {

    public abstract Task configure();
}
