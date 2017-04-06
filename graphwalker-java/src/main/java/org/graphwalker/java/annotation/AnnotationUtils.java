package org.graphwalker.java.annotation;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.graphwalker.core.machine.Context;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public abstract class AnnotationUtils {

  private static final Logger logger = LoggerFactory.getLogger(AnnotationUtils.class);

  public static Set<Class<?>> findTests(Reflections reflections) {
    return find(reflections, Context.class, GraphWalker.class);
  }

  public static Set<Class<?>> find(Reflections reflections, Class<?> type, Class<? extends Annotation> annotation) {
    Set<Class<?>> classes = new HashSet<>();
    for (Class<?> subType : reflections.getTypesAnnotatedWith(annotation)) {
      if (type.isAssignableFrom(subType)) {
        classes.add(subType);
      }
    }
    return classes;
  }

  private static boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotation) {
    return isAnnotationPresent(type, annotation, Thread.currentThread().getContextClassLoader());
  }

  @SuppressWarnings("unchecked")
  private static boolean isAnnotationPresent(Class<?> type, Class<? extends Annotation> annotation, ClassLoader classLoader) {
    try {
      Class<? extends Annotation> a = (Class<? extends Annotation>) classLoader.loadClass(annotation.getName());
      return type.isAnnotationPresent(a);
    } catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
      throw new RuntimeException();
    }
  }

  public static <T extends Annotation> Set<T> getAnnotations(final Class<?> clazz, final Class<T> annotation) {
    Set<T> annotations = new HashSet<>();
    Class<?> queryClass = clazz;
    while (null != queryClass) {
      addAnnotation(queryClass, annotations, annotation);
      for (Class<?> interfaceClass : queryClass.getInterfaces()) {
        addAnnotation(interfaceClass, annotations, annotation);
      }
      queryClass = queryClass.getSuperclass();
    }
    return annotations;
  }

  private static <T extends Annotation> void addAnnotation(final Class<?> clazz, final Set<T> annotations, final Class<T> annotation) {
    if (clazz.isAnnotationPresent(annotation)) {
      annotations.add(clazz.getAnnotation(annotation));
    }
  }

  public static void execute(Class<? extends Annotation> annotation, Object implementation) {
    for (Method method : implementation.getClass().getMethods()) {
      if (method.isAnnotationPresent(annotation)) {
        try {
          method.invoke(implementation);
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
