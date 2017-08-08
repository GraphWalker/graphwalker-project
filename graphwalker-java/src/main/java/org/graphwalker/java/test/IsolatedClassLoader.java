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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public final class IsolatedClassLoader extends URLClassLoader {

  private static final Logger logger = LoggerFactory.getLogger(IsolatedClassLoader.class);

  public IsolatedClassLoader(List<String> urls) {
    this(convert(urls));
  }

  public IsolatedClassLoader(URL[] urls) {
    super(urls, getParentClassLoader());
  }

  private static ClassLoader getParentClassLoader() {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    if (systemClassLoader != null) {
      return systemClassLoader.getParent();
    }
    return null;
  }

  private static URL[] convert(List<String> urls) {
    List<URL> urlList = new ArrayList<>();
    for (String url : urls) {
      try {
        urlList.add(new File(url).toURI().toURL());
      } catch (MalformedURLException e) {
        logger.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }
    return urlList.toArray(new URL[urlList.size()]);
  }
}
