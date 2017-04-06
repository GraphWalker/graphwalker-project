package org.graphwalker.java.factory;

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

import java.lang.reflect.Constructor;
import org.graphwalker.core.condition.ReachedStopCondition;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.java.annotation.GraphWalker;
import org.graphwalker.java.test.TestExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public abstract class PathGeneratorFactory {

  private static final Logger logger = LoggerFactory.getLogger(PathGeneratorFactory.class);

  public static PathGenerator createPathGenerator(GraphWalker annotation) {
    try {
      Constructor constructor;
      try {
        constructor = annotation.pathGenerator().getConstructor(StopCondition.class);
      } catch (Throwable t) {
        logger.error(t.getMessage());
        constructor = annotation.pathGenerator().getConstructor(ReachedStopCondition.class);
      }
      if (null == constructor) {
        throw new TestExecutionException("Couldn't find a valid constructor");
      }
      return (PathGenerator) constructor.newInstance(StopConditionFactory.createStopCondition(annotation));
    } catch (Throwable e) {
      logger.error(e.getMessage());
      throw new TestExecutionException(e);
    }
  }
}
