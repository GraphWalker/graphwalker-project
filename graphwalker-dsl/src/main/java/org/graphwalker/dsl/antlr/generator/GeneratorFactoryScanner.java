package org.graphwalker.dsl.antlr.generator;

/*
 * #%L
 * GraphWalker Input/Output
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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.graphwalker.core.condition.Never;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.generator.PathGeneratorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class GeneratorFactoryScanner {

  private GeneratorFactoryScanner() {
  }

  private static final Logger logger = LoggerFactory.getLogger(GeneratorFactoryScanner.class);

  public static PathGenerator get(String generator) {
    List<Class<PathGeneratorBase>> pathGenerators;
    try (ScanResult scanResult = new ClassGraph()
      .enableClassInfo().scan()) {
      pathGenerators = scanResult
        .getSubclasses(PathGeneratorBase.class.getName())
        .loadClasses(PathGeneratorBase.class);
    }
    logger.debug("Available path generators:  " + pathGenerators.toString());
    for (Class<? extends PathGeneratorBase> generatorClass : pathGenerators) {
      if (generatorClass.getSimpleName().equalsIgnoreCase(generator)) {
        PathGenerator pathGenerator = null;
        try {
          pathGenerator = generatorClass.getConstructor(StopCondition.class).newInstance(new Never());
        } catch (InstantiationException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(e);
        }
        logger.debug("Found suitable path generator: " + pathGenerator.getClass().getName());
        return pathGenerator;
      }
    }
    throw new GeneratorFactoryException("No suitable generator found with name: " + generator + " in classpath");
  }
}
