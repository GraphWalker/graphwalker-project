package org.graphwalker.io.factory;

/*
 * #%L
 * GraphWalker Input/Output
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

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nils Olsson
 */
public final class ContextFactoryScanner {

    private static Map<Class<? extends ContextFactory>, ContextFactory> factories = new HashMap<>();

    private static Reflections reflections = new Reflections(new ConfigurationBuilder()
            .addUrls(ClasspathHelper.forJavaClassPath())
            .addUrls(ClasspathHelper.forClassLoader())
            .addScanners(new SubTypesScanner()));

    public static ContextFactory get(Path path) {
        for (Class<? extends ContextFactory> factoryClass: reflections.getSubTypesOf(ContextFactory.class)) {
            ContextFactory factory = create(factoryClass);
            if (null != factory && factory.accept(path)) {
                return factory;
            }
        }
        throw new ContextFactoryException("No suitable context factory found");
    }

    private static ContextFactory create(Class<? extends ContextFactory> factoryClass) {
        if (!factories.containsKey(factoryClass)) {
            try {
                factories.put(factoryClass, factoryClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }
        return factories.get(factoryClass);
    }
}
