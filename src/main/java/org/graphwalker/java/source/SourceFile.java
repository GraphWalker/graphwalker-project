package org.graphwalker.java.source;

/*
 * #%L
 * GraphWalker Java
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

import java.io.File;
import java.nio.file.Path;

/**
 * @author Nils Olsson
 */
public final class SourceFile {

    private final Path inputPath;
    private final Path relativePath;
    private final Path outputPath;
    private final String packageName;

    public SourceFile(File file, File baseDirectory, File outputDirectory) {
        this(file.toPath(), baseDirectory.toPath(), outputDirectory.toPath());
    }

    public SourceFile(Path inputPath, Path basePath, Path outputPath) {
        this.inputPath = inputPath;
        this.relativePath = basePath.relativize(inputPath);
        this.packageName = this.relativePath.getParent().toString().replace(File.separator, ".");
        this.outputPath = outputPath.resolve(this.relativePath).resolveSibling(getFileName() + ".java");
    }

    public Path getInputPath() {
        return inputPath;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getFileName() {
        return removeExtension(inputPath.getFileName().toString());
    }


    private String removeExtension(final String filename) {
        String ext = extension(filename);
        if ("".equals(ext)) {
            return filename;
        }
        final int index = filename.lastIndexOf(ext) - 1;
        return filename.substring(0, index);
    }

    private String extension(String filename) {
        int lastSep = filename.lastIndexOf(File.separatorChar);
        int lastDot;
        if (lastSep < 0) {
            lastDot = filename.lastIndexOf('.');
        } else {
            lastDot = filename.substring(lastSep + 1).lastIndexOf('.');
            if (lastDot >= 0) {
                lastDot += lastSep + 1;
            }
        }
        if (lastDot >= 0 && lastDot > lastSep) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
