package org.graphwalker.io.factory;

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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Nils Olsson
 */
public final class ContextFactoryScanner {

  public static final String JAVA = "JAVA";
  public static final String GRAPHML = "GRAPHML";
  public static final String DOT = "DOT";
  public static final String JSON = "JSON";

  private ContextFactoryScanner() {
  }

  private static final Logger logger = LoggerFactory.getLogger(ContextFactoryScanner.class);

  private static Map<Class<? extends ContextFactory>, ContextFactory> factories = new HashMap<>();

  public static ContextFactory get(Path path) {
    LoadingCache<Class<? extends ContextFactory>, ContextFactory> memo = CacheBuilder.newBuilder()
      .build(CacheLoader.from(ContextFactoryScanner::create));

    try (ScanResult scanResult = new ClassGraph().enableClassInfo().scan()) {
      for ( ClassInfo classInfo : scanResult.getClassesImplementing(ContextFactory.class)) {
        ContextFactory factory = null;
        try {
          factory = memo.get((Class<? extends ContextFactory>) classInfo.loadClass());
        } catch (ExecutionException e) {
          factory = create((Class<? extends ContextFactory>) classInfo.loadClass());
        }
        if (null != factory && factory.accept(path)) {
          return factory;
        }
      }
    }
    throw new ContextFactoryException("No suitable context factory found for file: " + path.toString());
  }

  private static ContextFactory create(Class<? extends ContextFactory> factoryClass) {
    if (!factories.containsKey(factoryClass)) {
      try {
        factories.put(factoryClass, factoryClass.newInstance());
      } catch (InstantiationException | IllegalAccessException e) {
        logger.error(e.getMessage());
        return null;
      }
    }
    return factories.get(factoryClass);
  }
}
