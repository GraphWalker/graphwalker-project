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

import org.graphwalker.core.event.Observer;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.java.annotation.*;
import org.graphwalker.java.factory.PathGeneratorFactory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public final class TestExecutor implements Executor {

    private static final Reflections reflections = new Reflections(new ConfigurationBuilder()
          .addUrls(filter(ClasspathHelper.forJavaClassPath(), ClasspathHelper.forClassLoader()))
          .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

    private static Collection<URL> filter(Collection<URL> classPath, Collection<URL> classLoader) {
        Reflections.log = null;
        List<URL> urls = new ArrayList<>(), filteredUrls = new ArrayList<>();
        urls.addAll(classPath);
        urls.addAll(classLoader);
        for (URL url: urls) {
            if (!filteredUrls.contains(url) && new File(url.getFile()).exists()) {
                filteredUrls.add(url);
            }
        }
        return filteredUrls;
    }

    private final Configuration configuration;
    private final MachineConfiguration machineConfiguration;
    private final Map<Context, MachineException> failures = new HashMap<>();
    private Machine machine;

    public TestExecutor(Configuration configuration) {
        this.configuration = configuration;
        this.machineConfiguration = createMachineConfiguration(AnnotationUtils.findTests(reflections));
    }

    public TestExecutor(Class<?>... tests) {
        this.configuration = new Configuration();
        this.machineConfiguration = createMachineConfiguration(Arrays.asList(tests));
    }

    @Override
    public Machine getMachine() {
        return machine;
    }

    private MachineConfiguration createMachineConfiguration(Collection<Class<?>> testClasses) {
        MachineConfiguration machineConfiguration = new MachineConfiguration();
        for (Class<?> testClass: testClasses) {
            GraphWalker annotation = testClass.getAnnotation(GraphWalker.class);
            if (isTestIncluded(annotation, testClass.getName())) {
                ContextConfiguration contextConfiguration = new ContextConfiguration();
                contextConfiguration.setTestClass(testClass);
                machineConfiguration.addContextConfiguration(contextConfiguration);
            }
        }
        return machineConfiguration;
    }

    private Collection<Context> createContexts(MachineConfiguration machineConfiguration) {
        Set<Context> contexts = new HashSet<>();
        for (ContextConfiguration contextConfiguration: machineConfiguration.getContextConfigurations()) {
            Context context = createContext(contextConfiguration.getTestClass());
            configureContext(context);
            contexts.add(context);
        }
        return contexts;
    }

    private Context createContext(Class<?> testClass) {
        try {
            return (Context)testClass.newInstance();
        } catch (Throwable e) {
            throw new TestExecutionException("Failed to create context");
        }
    }

    private void configureContext(Context context) {
        Set<Model> models = AnnotationUtils.getAnnotations(context.getClass(), Model.class);
        GraphWalker annotation = context.getClass().getAnnotation(GraphWalker.class);
        if (!models.isEmpty()) {
            Path path = Paths.get(models.iterator().next().file());
            ContextFactoryScanner.get(reflections, path).create(path, context);
        }
        if (!"".equals(annotation.value())) {
            context.setPathGenerator(GeneratorFactory.parse(annotation.value()));
        } else {
            context.setPathGenerator(PathGeneratorFactory.createPathGenerator(annotation));
        }
        if (!"".equals(annotation.start())) {
            context.setNextElement(getElement(context.getModel(), annotation.start()));
        }
    }

    private Machine createMachine(MachineConfiguration machineConfiguration) {
        Collection<Context> contexts = createContexts(machineConfiguration);
        machine = new SimpleMachine(contexts);
        for (Context context: machine.getContexts()) {
            if (context instanceof Observer) {
                machine.addObserver((Observer)context);
            }
        }
        return machine;
    }

    public MachineConfiguration getMachineConfiguration() {
        return machineConfiguration;
    }

    public Result execute() {
        Machine machine = createMachine(getMachineConfiguration());
        executeAnnotation(BeforeExecution.class, machine);
        try {
            Context context = null;
            while (machine.hasNextStep()) {
                if (null != context) {
                    executeAnnotation(BeforeElement.class, context);
                }
                context = machine.getNextStep();
                executeAnnotation(AfterElement.class, context);
            }
        } catch (MachineException e) {
            failures.put(e.getContext(), e);
        }
        executeAnnotation(AfterExecution.class, machine);
        return new Result();
    }

    private boolean isTestIncluded(GraphWalker annotation, String name) {
        boolean belongsToGroup = false;
        for (String group: annotation.groups()) {
            for (String definedGroups: configuration.getGroups()) {
                if (SelectorUtils.match(definedGroups, group)) {
                    belongsToGroup = true;
                    break;
                }
            }
        }
        if (belongsToGroup) {
            for (String exclude : configuration.getExcludes()) {
                if (SelectorUtils.match(exclude, name)) {
                    return false;
                }
            }
            for (String include : configuration.getIncludes()) {
                if (SelectorUtils.match(include, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Element getElement(RuntimeModel model, String name) {
        List<Element> elements = model.findElements(name);
        if (null == elements || 0 == elements.size()) {
            throw new TestExecutionException("Start element not found");
        }
        if (1 < elements.size()) {
            throw new TestExecutionException("Ambiguous start element defined");
        }
        return elements.get(0);
    }

    private void executeAnnotation(Class<? extends Annotation> annotation, Machine machine) {
        for (Context context: machine.getContexts()) {
            executeAnnotation(annotation, context);
        }
    }

    private void executeAnnotation(Class<? extends Annotation> annotation, Context context) {
        AnnotationUtils.execute(annotation, context);
    }

    public boolean isFailure(Context context) {
        return failures.containsKey(context);
    }

    public MachineException getFailure(Context context) {
        return failures.get(context);
    }

    public Collection<MachineException> getFailures() {
        return failures.values();
    }
}
