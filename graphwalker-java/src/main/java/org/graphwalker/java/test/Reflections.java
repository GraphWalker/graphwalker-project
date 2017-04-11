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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public abstract class Reflections {

  private static final Logger logger = LoggerFactory.getLogger(Reflections.class);

  private static final Class[] NO_ARGS = new Class[]{};

  public static Class<?> loadClass(ClassLoader classLoader, Class<?> type) {
    try {
      return classLoader.loadClass(type.getName());
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Object newInstance(ClassLoader classLoader, Class<?> type) {
    try {
      return loadClass(classLoader, type).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      logger.error(e.getCause().getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Object newInstance(Constructor<?> constructor, Object... arguments) {
    try {
      return constructor.newInstance(arguments);
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      logger.error(e.getCause().getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Object invoke(Object object, Method method, Object... arguments) {
    try {
      return method.invoke(object, arguments);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      logger.error(e.getCause().getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Method getMethod(Class<?> type, String name, Class... parameters) {
    try {
      return type.getMethod(name, parameters);
    } catch (NoSuchMethodException e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Constructor<?> getConstructor(ClassLoader classLoader, Class<?> type, Class<?> parameters) {
    try {
      return loadClass(classLoader, type).getConstructor(parameters);
    } catch (NoSuchMethodException e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Object get(Object object, Class<?> type, String name) {
    Method method = getMethod(type, name, NO_ARGS);
    return invoke(object, method);
  }
}
