package org.graphwalker.gradle.plugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Nils Olsson
 */
public class Clean extends DefaultTask {

    @TaskAction
    public void cleanUp() {
        Path directory = Paths.get(getProject().getProjectDir().getAbsolutePath(), "generated-sources");
        if (Files.exists(directory)) {
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        System.out.println(file.toFile().getAbsolutePath());
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                        System.out.println(dir.toFile().getAbsolutePath());
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
