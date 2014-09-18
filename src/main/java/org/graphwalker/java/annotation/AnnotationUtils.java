package org.graphwalker.java.annotation;

/*
 * #%L
 * GraphWalker Java
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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

import org.graphwalker.core.machine.Context;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nils Olsson
 */
public final class AnnotationUtils {

    private AnnotationUtils() {}

    private static String getExtension(String path) {
        int position = path.lastIndexOf('.');
        return path.lastIndexOf(File.separator)>position?"":path.substring(position+1);
    }

    private static boolean valid(URL url) {
        String extension = getExtension(url.getPath());
        if ("".equals(extension)) {
            try {
                return Paths.get(url.toURI()).toFile().exists();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return "jar".equals(extension);
    }

    private static Collection<URL> getUrls() {
        Set<URL> filteredUrls = new HashSet<>();
        Set<URL> urls = new HashSet<>();
        urls.addAll(ClasspathHelper.forClassLoader());
        urls.addAll(ClasspathHelper.forJavaClassPath());
        for (URL url: urls) {
            if (valid(url)) {
                filteredUrls.add(url);
            }
        }
        return filteredUrls;
    }

    private static Reflections reflections = new Reflections(new ConfigurationBuilder()
            .addUrls(getUrls())
            .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

    public static Set<Class<? extends Context>> findTests() {
        Set<Class<? extends Context>> testClasses = new HashSet<>();
        for (Class<? extends Context> testClass: reflections.getSubTypesOf(Context.class)) {
            if (testClass.isAnnotationPresent(GraphWalker.class)) {
                testClasses.add(testClass);
            }
        }
        return testClasses;
    }

    public static <T extends Annotation> Set<T> getAnnotations(final Class<?> clazz, final Class<T> annotation) {
        Set<T> annotations = new HashSet<>();
        Class<?> queryClass = clazz;
        while (null != queryClass) {
            addAnnotation(queryClass, annotations, annotation);
            for (Class<?> interfaceClass: queryClass.getInterfaces()) {
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
        for (Method method: implementation.getClass().getMethods()) {
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
