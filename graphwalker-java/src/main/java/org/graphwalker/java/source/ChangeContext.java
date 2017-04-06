package org.graphwalker.java.source;

/*
 * #%L
 * GraphWalker Java
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

import static org.graphwalker.core.model.Model.RuntimeModel;

import japa.parser.ast.body.MethodDeclaration;
import java.util.HashSet;
import java.util.Set;
import org.graphwalker.core.model.Element;

/**
 * @author Nils Olsson
 */
public final class ChangeContext {

  private final RuntimeModel model;
  private final Set<String> methodNames;
  private final Set<MethodDeclaration> methodDeclarations = new HashSet<>();

  public ChangeContext(RuntimeModel model) {
    this.model = model;
    methodNames = extractMethodNames(model);
  }

  public Set<String> getMethodNames() {
    return methodNames;
  }

  public void addMethodDeclaration(MethodDeclaration methodDeclaration) {
    methodDeclarations.add(methodDeclaration);
  }

  public Set<MethodDeclaration> getMethodDeclarations() {
    return methodDeclarations;
  }

  private Set<String> extractMethodNames(RuntimeModel model) {
    Set<String> methodNames = new HashSet<>();
    for (Element element : model.getElements()) {
      // TODO: do we need to ignore Start!?
      if (element.hasName() && !"Start".equalsIgnoreCase(element.getName())) {
        methodNames.add(element.getName());
      }
    }
    return methodNames;
  }

  public boolean isVertex(String name) {
    return null != model.findVertices(name);
  }
}
