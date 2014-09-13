package org.graphwalker.maven.plugin;

/*
 * #%L
 * GraphWalker Maven Plugin
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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
import org.codehaus.plexus.util.StringUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.java.source.CodeGenerator;
import org.graphwalker.java.source.SourceFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public abstract class GenerateMojoBase extends DefaultMojoBase {

    @Parameter(defaultValue = "${project.build.sourceEncoding}", required = true, readonly = true)
    private String sourceEncoding;

    private CodeGenerator codeGenerator = new CodeGenerator();

    protected String getSourceEncoding() {
        return sourceEncoding;
    }

    protected abstract File getGeneratedSourcesDirectory();

    protected void generate(List<Resource> resources) {
        for (Resource resource: resources) {
            generate(resource);
        }
    }

    private void generate(Resource resource) {
        File baseDirectory = new File(resource.getDirectory());
        for (File file: findFiles("**/*.*", null, baseDirectory)) {
            generate(file, baseDirectory, getGeneratedSourcesDirectory());
        }
    }

    private void generate(File file, File baseDirectory, File outputDirectory) {
        generate(new SourceFile(file, baseDirectory, outputDirectory));
    }

    private void generate(SourceFile sourceFile) {
        if (null != ContextFactoryScanner.get(sourceFile.getInputPath())) {
            File outputFile = sourceFile.getOutputPath().toFile();
            try {
                ContextFactory contextFactory = ContextFactoryScanner.get(sourceFile.getInputPath());
                RuntimeModel model = contextFactory.create(sourceFile.getInputPath()).getModel();
                String source = codeGenerator.generate(sourceFile, model);
                if (Files.exists(sourceFile.getOutputPath())) {
                    String existingSource = StringUtils.removeDuplicateWhitespace(FileUtils.fileRead(outputFile, sourceEncoding));
                    if (existingSource.equals(StringUtils.removeDuplicateWhitespace(new String(source.getBytes(), sourceEncoding)))) {
                        return;
                    }
                }
                if (getLog().isInfoEnabled()) {
                    getLog().info("Generate " + sourceFile.getInputPath());
                }
                FileUtils.mkdir(sourceFile.getOutputPath().getParent().toFile().getAbsolutePath());
                FileUtils.fileDelete(outputFile.getAbsolutePath());
                FileUtils.fileWrite(outputFile.getAbsolutePath(), sourceEncoding, source);
            } catch (Throwable t) {
                if (getLog().isInfoEnabled()) {
                    getLog().info("Error: Generate " + sourceFile.getInputPath());
                }
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Error: Generate " + sourceFile.getInputPath(), t);
                }
            }
        }
    }
}
