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

import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.io.factory.json.JsonContext;
import org.graphwalker.java.annotation.*;
import org.graphwalker.java.factory.PathGeneratorFactory;
import org.graphwalker.java.report.XMLReportGenerator;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

import static org.graphwalker.core.common.Objects.*;
import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public final class TestExecutor implements Executor, Observer {

  private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

  private static final Reflections reflections = new Reflections(new ConfigurationBuilder()
    .addUrls(filter(ClasspathHelper.forJavaClassPath(), ClasspathHelper.forClassLoader()))
    .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));

  private static Collection<URL> filter(Collection<URL> classPath, Collection<URL> classLoader) {
    Reflections.log = null;
    List<URL> urls = new ArrayList<>(), filteredUrls = new ArrayList<>();
    urls.addAll(classPath);
    urls.addAll(classLoader);
    for (URL url : urls) {
      if (!filteredUrls.contains(url) && exists(url)) {
        filteredUrls.add(url);
      }
    }
    return filteredUrls;
  }

  private static boolean exists(URL url) {
    try {
      return new File(URLDecoder.decode(url.getFile(), "UTF-8")).exists();
    } catch (UnsupportedEncodingException e) {
      return false;
    }
  }

  private final Configuration configuration;
  private final MachineConfiguration machineConfiguration;
  private final Map<Context, MachineException> failures = new HashMap<>();
  private final Machine machine;
  private Result result;

  public TestExecutor(Configuration configuration) throws IOException {
    this.configuration = configuration;
    this.machineConfiguration = createMachineConfiguration(AnnotationUtils.findTests(reflections));
    this.machine = createMachine(machineConfiguration);
    this.machine.addObserver(this);
  }

  public TestExecutor(Class<?>... tests) throws IOException {
    this.configuration = new Configuration();
    this.machineConfiguration = createMachineConfiguration(Arrays.asList(tests));
    this.machine = createMachine(machineConfiguration);
    this.machine.addObserver(this);
  }

  public TestExecutor(Context... contexts) {
    this.configuration = new Configuration();
    this.machineConfiguration = new MachineConfiguration();
    this.machine = new SimpleMachine(contexts);
    this.machine.addObserver(this);
  }

  public TestExecutor(Collection<Context> contexts) {
    this.configuration = new Configuration();
    this.machineConfiguration = new MachineConfiguration();
    this.machine = new SimpleMachine(contexts);
    this.machine.addObserver(this);
  }

  @Override
  public Machine getMachine() {
    return machine;
  }

  private MachineConfiguration createMachineConfiguration(Collection<Class<?>> testClasses) {
    MachineConfiguration machineConfiguration = new MachineConfiguration();
    for (Class<?> testClass : testClasses) {
      GraphWalker annotation = testClass.getAnnotation(GraphWalker.class);
      if (isTestIncluded(annotation, testClass.getName())) {
        ContextConfiguration contextConfiguration = new ContextConfiguration();
        contextConfiguration.setTestClass(testClass);
        machineConfiguration.addContextConfiguration(contextConfiguration);
      }
    }
    return machineConfiguration;
  }

  private Collection<Context> createContexts(MachineConfiguration machineConfiguration) throws IOException {
    Set<Context> contexts = new HashSet<>();
    for (ContextConfiguration contextConfiguration : machineConfiguration.getContextConfigurations()) {
      Context context = createContext(contextConfiguration.getTestClass());
      configureContext(context);
      contexts.add(context);
    }
    return contexts;
  }

  private Context createContext(Class<?> testClass) {
    try {
      return (Context) testClass.newInstance();
    } catch (Throwable t) {
      logger.error(t.getMessage());
      throw new TestExecutionException("Failed to create context");
    }
  }

  private void configureContext(Context context) throws IOException {
    Set<Model> models = AnnotationUtils.getAnnotations(context.getClass(), Model.class);
    if (!models.isEmpty()) {
      Path path = Paths.get(models.iterator().next().file());
      List<Context> contexts = ContextFactoryScanner.get(reflections, path).create(path);

      if (isNullOrEmpty(contexts)) {
        throw new TestExecutionException("Could not read the model: " + path.toString());
      } else if (contexts.size() == 1) {
        context.setModel(contexts.get(0).getModel());
        context.setNextElement(contexts.get(0).getNextElement());

        // If a json file was read, we have possible data for path generator
        if (contexts.get(0) instanceof JsonContext) {
          if (isNotNull(contexts.get(0).getPathGenerator())) {
            context.setPathGenerator(contexts.get(0).getPathGenerator());
          }
        }
      } else {
        for (Context examineContext : contexts) {
          try {
            if (Class.forName(path.getParent().toString().replace(File.separatorChar, '.') + "." + examineContext.getModel().getName()).isAssignableFrom(context.getClass())) {
              context.setModel(examineContext.getModel());
              context.setNextElement(examineContext.getNextElement());

              // If a json file was read, we have possible data for path generator
              if (contexts.get(0) instanceof JsonContext) {
                if (isNotNull(contexts.get(0).getPathGenerator())) {
                  context.setPathGenerator(examineContext.getPathGenerator());
                }
              }
            }
          } catch (ClassNotFoundException e) {
            logger.error("Problem examine: " + examineContext.getModel().getName());
          }
        }
      }
    }

    GraphWalker annotation = context.getClass().getAnnotation(GraphWalker.class);
    if (isNotNull(annotation)) {
      if (!"".equals(annotation.value())) {
        context.setPathGenerator(GeneratorFactory.parse(annotation.value()));
      } else {
        if (isNull(context.getPathGenerator())) {
          context.setPathGenerator(PathGeneratorFactory.createPathGenerator(annotation));
        }
      }
      if (isNotNullOrEmpty(annotation.start()) && isNotNull(context.getModel())) {
        context.setNextElement(getElement(context.getModel(), annotation.start()));
      }
    }
  }


  private Machine createMachine(MachineConfiguration machineConfiguration) throws IOException {
    Collection<Context> contexts = createContexts(machineConfiguration);
    Machine machine = new SimpleMachine(contexts);
    for (Context context : machine.getContexts()) {
      if (context instanceof Observer) {
        machine.addObserver((Observer) context);
      }
    }
    return machine;
  }

  @Override
  public MachineConfiguration getMachineConfiguration() {
    return machineConfiguration;
  }

  @Override
  public Result execute() {
    return execute(false);
  }

  @Override
  public Result execute(boolean ignoreErrors) {
    result = new Result();
    executeAnnotation(BeforeExecution.class, machine);
    try {
      while (machine.hasNextStep()) {
        machine.getNextStep();
      }
    } catch (MachineException e) {
      logger.error(e.getMessage());
      failures.put(e.getContext(), e);
    }
    executeAnnotation(AfterExecution.class, machine);
    result.updateResults(machine, failures);
    if (!ignoreErrors && !failures.isEmpty()) {
      throw new TestExecutionException(result);
    }
    return result;
  }

  @Override
  public Result getResult() {
    return result;
  }

  private boolean isTestIncluded(GraphWalker annotation, String name) {
    if (isNull(annotation)) {
      return true;
    }
    boolean belongsToGroup = false;
    for (String group : annotation.groups()) {
      for (String definedGroups : configuration.getGroups()) {
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
    if (null == elements || elements.isEmpty()) {
      throw new TestExecutionException("Start element not found");
    }
    if (1 < elements.size()) {
      throw new TestExecutionException("Ambiguous start element defined");
    }
    return elements.get(0);
  }

  private void executeAnnotation(Class<? extends Annotation> annotation, Machine machine) {
    for (Context context : machine.getContexts()) {
      executeAnnotation(annotation, context);
    }
  }

  private void executeAnnotation(Class<? extends Annotation> annotation, Context context) {
    AnnotationUtils.execute(annotation, context);
  }

  @Override
  public boolean isFailure(Context context) {
    return failures.containsKey(context);
  }

  @Override
  public MachineException getFailure(Context context) {
    return failures.get(context);
  }

  @Override
  public Collection<MachineException> getFailures() {
    return failures.values();
  }

  public void reportResults(File file, Date startTime, Properties properties) {
    new XMLReportGenerator(startTime, properties).writeReport(file, this);
    if (!getFailures().isEmpty()) {
      throw new TestExecutionException(
        MessageFormat.format("There are test failures.\n\n Please refer to {0} for the individual test results.", file.getAbsolutePath()));
    }
  }

  @Override
  public void update(Machine machine, Element element, EventType type) {
    switch (type) {
      case BEFORE_ELEMENT: {
        executeAnnotation(BeforeElement.class, machine.getCurrentContext());
      }
      break;
      case AFTER_ELEMENT: {
        executeAnnotation(AfterElement.class, machine.getCurrentContext());
      }
      break;
    }
  }
}
