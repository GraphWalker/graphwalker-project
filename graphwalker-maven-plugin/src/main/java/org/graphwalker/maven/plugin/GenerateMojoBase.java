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

import java.io.File;
import java.util.List;
import org.apache.maven.model.Resource;
import org.graphwalker.java.source.CodeGenerator;


/**
 * @author Nils Olsson
 */
public abstract class GenerateMojoBase extends DefaultMojoBase {

  public GenerateMojoBase() {
  }

  protected abstract File getGeneratedSourcesDirectory();

  protected void generate(List<Resource> resources) {
    for (Resource resource : resources) {
      generate(resource);
    }
  }

  private void generate(Resource resource) {
    File baseDirectory = new File(resource.getDirectory());
    CodeGenerator.generate(baseDirectory.toPath(), getGeneratedSourcesDirectory().toPath());
  }

}
