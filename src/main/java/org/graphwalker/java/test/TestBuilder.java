package org.graphwalker.java.test;

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

import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.graphwalker.io.factory.ContextFactoryScanner;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Nils Olsson
 */
public final class TestBuilder {

    private Path model;
    private Context context;
    private PathGenerator generator;
    private String start;

    public TestBuilder setModel(String model) {
        return setModel(Paths.get(model));
    }

    public TestBuilder setModel(Path model) {
        this.model = model;
        return this;
    }

    public TestBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public TestBuilder setPathGenerator(PathGenerator generator) {
        this.generator = generator;
        return this;
    }

    public TestBuilder setStart(String start) {
        this.start = start;
        return this;
    }

    public Context build() {
        ContextFactory factory = ContextFactoryScanner.get(model);
        if (null != factory) {
            return factory.create(model, context)
                .setPathGenerator(generator)
                .setNextElement(context.getModel().findElements(start).get(0));
        }
        throw new ContextFactoryException("Failed to create setContext");
    }

    public Executor execute() {
        return new TestExecutor(build()).execute();
    }

}
