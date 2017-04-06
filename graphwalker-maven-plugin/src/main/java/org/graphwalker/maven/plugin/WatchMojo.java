package org.graphwalker.maven.plugin;

/*
 * #%L
 * GraphWalker Maven Plugin
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

import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.graphwalker.core.model.Model.RuntimeModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.java.source.CodeGenerator;
import org.graphwalker.java.source.SourceFile;

/**
 * @author Nils Olsson
 */
@Mojo(name = "watch")
public final class WatchMojo extends AbstractMojo {

  private static final WatchEvent.Kind[] EVENT_TYPES = new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};

  @Component
  private MavenProject mavenProject;

  @Parameter(defaultValue = "${project.build.sourceEncoding}")
  private String encoding;

  @Parameter(defaultValue = "${project.build.directory}/generated-sources/graphwalker")
  private File sourcesDirectory;

  @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/graphwalker")
  private File testSourcesDirectory;

  @SuppressWarnings("unchecked")
  private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  private WatchService watchService;
  private final CodeGenerator codeGenerator = new CodeGenerator();
  private final Map<Path, File> resourceMap = new HashMap<>();
  private final Map<WatchKey, Path> watchKeyMap = new HashMap<>();

  private String getEncoding() {
    return StringUtils.isEmpty(encoding) ? ReaderFactory.FILE_ENCODING : encoding;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      addResources(mavenProject.getResources(), sourcesDirectory);
      addResources(mavenProject.getTestResources(), testSourcesDirectory);
      watch(resourceMap.keySet());
      //noinspection InfiniteLoopStatement
      for (; ; ) {
        WatchKey watchKey = getWatchService().take();
        Path path = watchKeyMap.get(watchKey);
        if (null == path) {
          continue;
        }
        for (WatchEvent<?> event : watchKey.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();
          if (kind == OVERFLOW) {
            continue;
          }
          WatchEvent<Path> file = cast(event);
          Path resolvedPath = path.resolve(file.context());
          if (ENTRY_CREATE == event.kind() || ENTRY_MODIFY == event.kind()) {
            update(getRootPath(resolvedPath), resolvedPath);
          } else if (ENTRY_DELETE == event.kind()) {
            delete(getRootPath(resolvedPath), resolvedPath);
          }
        }
        watchKey.reset();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("", e);
    }
  }

  private void addResources(List<Resource> resources, File outputDirectory) {
    for (Resource resource : resources) {
      resourceMap.put(FileSystems.getDefault().getPath(resource.getDirectory()), outputDirectory);
    }
  }

  private WatchService getWatchService() throws IOException {
    if (null == watchService) {
      watchService = FileSystems.getDefault().newWatchService();
    }
    return watchService;
  }

  private void watch(Set<Path> paths) throws IOException {
    for (Path path : paths) {
      watch(path);
    }
  }

  private void watch(Path path) throws IOException {
    if (Files.exists(path) && !watchKeyMap.values().contains(path)) {
      if (Files.isDirectory(path)) {
        WatchKey watchKey = path.register(getWatchService(), EVENT_TYPES, HIGH);
        watchKeyMap.put(watchKey, path);
        getLog().info("Watching: " + path.toString());
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) throws IOException {
            watch(path);
            return FileVisitResult.CONTINUE;
          }
        });
      }
    }
  }

  private Path getRootPath(Path path) throws MojoExecutionException {
    for (Path rootPath : resourceMap.keySet()) {
      if (path.startsWith(rootPath)) {
        return rootPath;
      }
    }
    throw new MojoExecutionException("Path not found");
  }

  private boolean isModified(Path outputPath, Path path) throws IOException {
    return Files.getLastModifiedTime(outputPath).to(TimeUnit.MILLISECONDS)
           < Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS);
  }

  private boolean isSupportedFileType(Path path) throws IOException {
    return null != ContextFactoryScanner.get(path);
  }

  private void generate(SourceFile sourceFile) throws IOException {
    File outputFile = sourceFile.getOutputPath().toFile();
    ContextFactory contextFactory = ContextFactoryScanner.get(sourceFile.getInputPath());
    List<Context> contexts = contextFactory.create(sourceFile.getInputPath());
    for (Context context : contexts) {
      try {
        RuntimeModel model = context.getModel();
        String source = codeGenerator.generate(sourceFile, model);
        if (Files.exists(sourceFile.getOutputPath())) {
          String existingSource = StringUtils.removeDuplicateWhitespace(FileUtils.fileRead(outputFile, getEncoding()));
          if (existingSource.equals(StringUtils.removeDuplicateWhitespace(new String(source.getBytes(), getEncoding())))) {
            return;
          }
        }
        if (getLog().isInfoEnabled()) {
          getLog().info("Generate: " + sourceFile.getOutputPath());
        }
        FileUtils.mkdir(sourceFile.getOutputPath().getParent().toFile().getAbsolutePath());
        FileUtils.fileDelete(outputFile.getAbsolutePath());
        FileUtils.fileWrite(outputFile.getAbsolutePath(), getEncoding(), source);
      } catch (Throwable t) {
        if (getLog().isInfoEnabled()) {
          getLog().info("Error: Generate: " + sourceFile.getOutputPath());
        }
        if (getLog().isDebugEnabled()) {
          getLog().debug("Error: Generate: " + sourceFile.getOutputPath(), t);
        }
      }
    }
  }

  private void update(Path root, Path path) throws IOException {
    if (Files.exists(path) && !Files.isHidden(path)) {
      if (Files.isDirectory(path)) {
        watch(path);
      } else if (isSupportedFileType(path)) {
        SourceFile sourceFile = new SourceFile(path, root, resourceMap.get(root).toPath());
        if (!Files.exists(sourceFile.getOutputPath()) || isModified(sourceFile.getOutputPath(), path)) {
          generate(sourceFile);
        }
      }
    }
  }

  private void delete(Path root, Path path) throws IOException {
    if (isSupportedFileType(path)) {
      SourceFile sourceFile = new SourceFile(path, root, resourceMap.get(root).toPath());
      if (Files.exists(sourceFile.getOutputPath())) {
        Files.delete(sourceFile.getOutputPath());
        if (!Files.exists(sourceFile.getOutputPath())) {
          getLog().info("Delete: " + sourceFile.getOutputPath());
        }
      }
    }
  }
}
