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

import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.graphwalker.io.factory.ContextFactoryScanner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils Olsson
 */
public final class TestBuilder {

	private List<Context> contexts = new ArrayList<>();

	public TestBuilder addModel(String model) {
		return addModel(Paths.get(model));
	}

	public TestBuilder addModel(Path model) {
		contexts.add(createContext(model, null, null, null));
		return this;
	}

	public TestBuilder addModel(String model, PathGenerator pathGenerator) {
		return addModel(Paths.get(model), pathGenerator);
	}

	public TestBuilder addModel(Path model, PathGenerator pathGenerator) {
		contexts.add(createContext(model, null, pathGenerator, null));
		return this;
	}

	public TestBuilder addModel(String model, PathGenerator pathGenerator, String start) {
		return addModel(Paths.get(model), pathGenerator, start);
	}

	public TestBuilder addModel(Path model, PathGenerator pathGenerator, String start) {
		contexts.add(createContext(model, null, pathGenerator, start));
		return this;
	}

	public TestBuilder addModel(String model, Context context) {
		return addModel(Paths.get(model), context);
	}

	public TestBuilder addModel(Path model, Context context) {
		contexts.add(createContext(model, context, null, null));
		return this;
	}

	private Context createContext(Path model, Context context, PathGenerator pathGenerator, String start) {
		ContextFactory factory = ContextFactoryScanner.get(model);
		Context newContext;
		try {
			if (null != context) {
				newContext = factory.create(model, context);
			} else {
				newContext = factory.create(model);
			}
			if (null != pathGenerator) {
				newContext.setPathGenerator(pathGenerator);
			}
			if (null != start) {
				newContext.setNextElement(newContext.getModel().findElements(start).get(0));
			}
		} catch (Throwable t) {
			throw new ContextFactoryException("Failed to create context", t);
		}
		return newContext;
	}

	public TestBuilder addContext(Context context) {
		contexts.add(context);
		return this;
	}

	public TestBuilder addClass(Class<? extends Context> testClass) {
		contexts.add(createContext(testClass));
		return this;
	}

	public TestBuilder addClass(Class<? extends Context> testClass, String pathGenerator) {
		return addClass(testClass, GeneratorFactory.parse(pathGenerator));
	}

	public TestBuilder addClass(Class<? extends Context> testClass, PathGenerator pathGenerator) {
		Context context = createContext(testClass);
		context.setPathGenerator(pathGenerator);
		contexts.add(context);
		return this;
	}

	private Context createContext(Class<? extends Context> testClass) {
		try {
			return testClass.newInstance();
		} catch (Throwable t) {
			throw new ContextFactoryException("Failed to create context", t);
		}
	}

	public Result execute() {
		if (contexts.isEmpty()) {
			return new TestExecutor(build()).execute();
		} else {
			return new TestExecutor(contexts).execute();
		}
	}

	@Deprecated private Path model;
	@Deprecated private Context context;
	@Deprecated private PathGenerator generator;
	@Deprecated private String start;

	@Deprecated
    public TestBuilder setModel(String model) {
        return setModel(Paths.get(model));
    }

	@Deprecated
    public TestBuilder setModel(Path model) {
        this.model = model;
        return this;
    }

	@Deprecated
    public TestBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

	@Deprecated
    public TestBuilder setPathGenerator(PathGenerator generator) {
        this.generator = generator;
        return this;
    }

	@Deprecated
    public TestBuilder setStart(String start) {
        this.start = start;
        return this;
    }

	@Deprecated
    public Context build() {
        ContextFactory factory = ContextFactoryScanner.get(model);
        try {
            return factory.create(model, context)
                    .setPathGenerator(generator)
                    .setNextElement(context.getModel().findElements(start).get(0));
        } catch (Throwable t) {
            throw new ContextFactoryException("Failed to create context", t);
        }
    }

}
