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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

/**
 * @author Nils Olsson
 */
@Mojo(name = "generate-test-sources", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public final class GenerateTestMojo extends GenerateMojoBase {

  @Parameter(property = "graphwalker.generate.test.directory", defaultValue = "${project.build.directory}/generated-test-sources/graphwalker")
  private File generatedSourcesDirectory;

  @Override
  protected File getGeneratedSourcesDirectory() {
    return generatedSourcesDirectory;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    generate(getMavenProject().getTestResources());
    if (getGeneratedSourcesDirectory().exists()) {
      getMavenProject().addTestCompileSourceRoot(getGeneratedSourcesDirectory().getPath());
    }
  }
}
