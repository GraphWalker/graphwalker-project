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

import org.apache.maven.model.Resource;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.java.source.CodeGenerator;
import org.graphwalker.java.source.SourceFile;
import org.graphwalker.maven.plugin.generate.Cache;
import org.graphwalker.maven.plugin.generate.CacheEntry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public abstract class GenerateMojoBase extends DefaultMojoBase {

    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    private Cache cache = new Cache();

    private CodeGenerator codeGenerator = new CodeGenerator();

    public GenerateMojoBase() {

    }

    protected String getEncoding() {
        return StringUtils.isEmpty(encoding) ? ReaderFactory.FILE_ENCODING : encoding;
    }

    protected abstract File getGeneratedSourcesDirectory();

    protected void generate(List<Resource> resources) {
        for (Resource resource : resources) {
            generate(resource);
        }
        cache.save();
    }

    private void generate(Resource resource) {
        File baseDirectory = new File(resource.getDirectory());
        for (File file : findFiles("**/*.*", null, baseDirectory)) {
            generate(file, baseDirectory, getGeneratedSourcesDirectory());
        }
    }

    private void generate(File file, File baseDirectory, File outputDirectory) {
        generate(new SourceFile(file, baseDirectory, outputDirectory));
    }

    private boolean isCached(SourceFile sourceFile) {
        Path path = sourceFile.getInputPath();
        if (cache.containsKey(path)) {
            CacheEntry entry = cache.get(path);
            try {
                return Files.getLastModifiedTime(path).equals(entry.getLastModifiedTime());
            } catch (IOException e) {
                //
            }
        }
        return false;
    }

    private void generate(SourceFile sourceFile) {
        File outputFile = sourceFile.getOutputPath().toFile();
        if (!isCached(sourceFile) && (!outputFile.exists() || outputFile.lastModified() < sourceFile.getInputPath().toFile().lastModified())) {
            ContextFactory contextFactory = getContextFactory(sourceFile);
            if (null != contextFactory) {
                try {
                    RuntimeModel model = contextFactory.create(sourceFile.getInputPath()).getModel();
                    String source = codeGenerator.generate(sourceFile, model);
                    if (Files.exists(sourceFile.getOutputPath())) {
                        String existingSource = StringUtils.removeDuplicateWhitespace(FileUtils.fileRead(outputFile, getEncoding()));
                        if (existingSource.equals(StringUtils.removeDuplicateWhitespace(new String(source.getBytes(), getEncoding())))) {
                            return;
                        }
                    }
                    if (getLog().isInfoEnabled()) {
                        getLog().info("Generate " + sourceFile.getInputPath());
                    }
                    FileUtils.mkdir(sourceFile.getOutputPath().getParent().toFile().getAbsolutePath());
                    FileUtils.fileDelete(outputFile.getAbsolutePath());
                    FileUtils.fileWrite(outputFile.getAbsolutePath(), getEncoding(), source);
                } catch (Throwable t) {
                    CacheEntry entry = new CacheEntry(sourceFile.getInputPath().toFile().lastModified());
                    cache.put(sourceFile.getInputPath(), entry);
                    if (getSession().getRequest().isShowErrors() && getLog().isErrorEnabled()) {
                        getLog().error("Error: Generate " + sourceFile.getInputPath(), t);
                    } else if (getLog().isDebugEnabled()) {
                        getLog().debug("Error: Generate " + sourceFile.getInputPath(), t);
                    }
                }
            }
        }
    }

    private ContextFactory getContextFactory(SourceFile sourceFile) {
        try {
            return ContextFactoryScanner.get(sourceFile.getInputPath());
        } catch (Throwable t) {
            return null;
        }
    }
}
