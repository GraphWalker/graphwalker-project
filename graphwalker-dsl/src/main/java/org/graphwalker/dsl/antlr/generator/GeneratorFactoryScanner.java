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

import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.generator.PathGeneratorBase;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class GeneratorFactoryScanner {

  private GeneratorFactoryScanner() {
  }

  private static final Logger logger = LoggerFactory.getLogger(GeneratorFactoryScanner.class);

  static {
    Reflections.log = null;
  }

  private static boolean valid(URL url) {
    String extension = FilenameUtils.getExtension(url.getPath());
    return "".equals(extension) || "jar".equals(extension);
  }

  private static Collection<URL> getUrls() {
    Set<URL> filteredUrls = new HashSet<>();
    Set<URL> urls = new HashSet<>();
    urls.addAll(ClasspathHelper.forClassLoader());
    urls.addAll(ClasspathHelper.forJavaClassPath());
    for (URL url : urls) {
      if (valid(url)) {
        logger.debug(url.toString());
        filteredUrls.add(url);
      }
    }
    return filteredUrls;
  }

  public static Class get(String generator) {
    return get(new Reflections(new ConfigurationBuilder().addUrls(getUrls()).addScanners(new SubTypesScanner())), generator);
  }

  public static Class get(Reflections reflections, String generatorString) {
    for (Class<? extends PathGeneratorBase> generatorClass : reflections.getSubTypesOf(PathGeneratorBase.class)) {
      if (generatorClass.getSimpleName().equalsIgnoreCase(generatorString)) {
        return generatorClass;
      }
    }
    throw new GeneratorFactoryException("No suitable generator found with name: " + generatorString);
  }
}
