package org.graphwalker.java.test;

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

import static org.graphwalker.core.common.Objects.isNullOrEmpty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public final class TestBuilder {

  private static final Logger logger = LoggerFactory.getLogger(TestBuilder.class);

  private List<Context> contexts = new ArrayList<>();

  public TestBuilder addContext(Context context) {
    contexts.add(context);
    return this;
  }

  @Deprecated
  public TestBuilder addContext(Context context, Path path) throws IOException {
    List<Context> pathContexts = ContextFactoryScanner.get(path).create(path);
    if (isNullOrEmpty(pathContexts)) {
      throw new TestExecutionException("Could not read the model: " + path.toString());
    } else if (pathContexts.size() > 1) {
      throw new TestExecutionException("The model path: " + path.toString() + ", has more models than 1. Can only handle 1 model.");
    }
    context.setModel(pathContexts.get(0).getModel());
    context.setNextElement(pathContexts.get(0).getNextElement());
    contexts.add(context);
    return this;
  }

  public TestBuilder addClass(Class<? extends Context> testClass) {
    contexts.add(createContext(testClass));
    return this;
  }

  public TestBuilder addClass(Class<? extends Context> testClass, String pathGenerator) {
    return addClass(testClass, GeneratorFactory.parse(pathGenerator));
  }

  public TestBuilder addClass(Class<? extends Context> testClass, PathGenerator pathGenerator) {
    Context context = createContext(testClass);
    context.setPathGenerator(pathGenerator);
    contexts.add(context);
    return this;
  }

  public TestBuilder addContext(Context context, Path model, String pathGenerator) {
    return addContext(context, model, GeneratorFactory.parse(pathGenerator));
  }

  public TestBuilder addContext(Context context, Path model, PathGenerator pathGenerator) {
    addModel(context, model);
    context.setPathGenerator(pathGenerator);
    contexts.add(context);
    return this;
  }

  private Context addModel(Context context, Path model) {
    try {
      List<Context> pathContexts = ContextFactoryScanner.get(model).create(model);
      if (isNullOrEmpty(pathContexts)) {
        throw new TestExecutionException("Could not read the model: " + model.toString());
      } else if (pathContexts.size() > 1) {
        throw new TestExecutionException("The model path: " + model.toString() + ", has more models than 1. Can only handle 1 model.");
      }
      context.setModel(pathContexts.get(0).getModel());
      context.setNextElement(pathContexts.get(0).getNextElement());
      return context;
    } catch (IOException e) {
      throw new TestExecutionException(e);
    }
  }

  private Context createContext(Class<? extends Context> testClass) {
    try {
      return testClass.newInstance();
    } catch (Throwable t) {
      logger.error(t.getMessage());
      throw new ContextFactoryException("Failed to create context", t);
    }
  }

  public Result execute(boolean ignoreError) {
    return new TestExecutor(contexts).execute(ignoreError);
  }

  public Result execute() {
    return execute(false);
  }
}
