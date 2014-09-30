package org.graphwalker.java.test;

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
