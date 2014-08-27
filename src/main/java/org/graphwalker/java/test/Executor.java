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

import org.graphwalker.core.condition.NamedStopCondition;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.*;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.java.annotation.AfterExecution;
import org.graphwalker.java.annotation.AnnotationUtils;
import org.graphwalker.java.annotation.BeforeExecution;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public final class Executor {

    private final Manager manager;
    private final Map<Context, Object> implementations = new HashMap<>();
    private final List<Machine> machines = new ArrayList<>();
    private final Map<Context, MachineException> failures = new HashMap<>();

    public Executor(Manager manager) {
        this.manager = manager;
    }

    public List<Machine> getMachines() {
        return Collections.unmodifiableList(machines);
    }

    public Map<Context, Object> getImplementations() {
        return Collections.unmodifiableMap(implementations);
    }

    private StopCondition createStopCondition(Class<? extends StopCondition> stopCondition, String value) throws IllegalAccessException, InstantiationException {
        if (value.isEmpty()) {
            return stopCondition.newInstance();
        }
        try {
            return stopCondition.getConstructor(new Class[]{String.class}).newInstance(value);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Long.TYPE}).newInstance(Long.parseLong(value));
        } catch (InvocationTargetException | NoSuchMethodException e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Integer.TYPE}).newInstance(Integer.parseInt(value));
        } catch (InvocationTargetException | NoSuchMethodException e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Double.TYPE}).newInstance(Double.parseDouble(value));
        } catch (InvocationTargetException | NoSuchMethodException e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Float.TYPE}).newInstance(Float.parseFloat(value));
        } catch (InvocationTargetException | NoSuchMethodException e) {
            // ignore
        }
        throw new RuntimeException(""); //TODO: change exception
    }

    public void awaitTermination() {
        if (!manager.getExecutionGroups().isEmpty()) {
            machines.clear();
            for (Group group: manager.getExecutionGroups()) {
                List<Context> contexts = new ArrayList<>();
                for (Execution execution: group.getExecutions()) {
                    if (Context.class.isAssignableFrom(execution.getTestClass())) {
                        try {
                            StopCondition stopCondition = createStopCondition(execution.getStopCondition(), execution.getStopConditionValue());

                            Constructor<? extends PathGenerator> constructor = null;
                            try {
                                constructor = execution.getPathGenerator().getConstructor(StopCondition.class);
                            } catch (Throwable _) {
                                // ignore
                            }
                            if (null == constructor) {
                                constructor = execution.getPathGenerator().getConstructor(NamedStopCondition.class);
                            }
                            PathGenerator pathGenerator = constructor.newInstance(stopCondition);

                            Context context = execution.getContext();
                            context.setPathGenerator(pathGenerator);

                            if (null != execution.getStart() && !"".equals(execution.getStart())) {
                                List<Vertex.RuntimeVertex> vertices = context.getModel().findVertices(execution.getStart());
                                if (null != vertices && !vertices.isEmpty()) {
                                    context.setNextElement(vertices.get(new Random(System.nanoTime()).nextInt(vertices.size())));
                                }
                            }

                            implementations.put(context, context);
                            contexts.add(context);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException(execution.getName()); // TODO: change exception, this occurs when the implementation is not a execution context
                    }
                }
                machines.add(new SimpleMachine(contexts));
            }
            try {
                ExecutorService executorService = Executors.newFixedThreadPool(machines.size());
                for (final Machine machine : machines) {
                    executorService.execute(new Runnable() {
                        public void run() {
                            for (Context context: machine.getContexts()) {
                                AnnotationUtils.execute(BeforeExecution.class, implementations.get(context));
                            }
                            try {
                                while (machine.hasNextStep()) {
                                    machine.getNextStep();
                                }
                            } catch (MachineException e) {
                                failures.put(e.getContext(), e);
                            }
                            for (Context context: machine.getContexts()) {
                                AnnotationUtils.execute(AfterExecution.class, implementations.get(context));
                            }
                        }
                    });
                }
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                throw new RuntimeException(); // TODO: byt exception
            }
        }
    }

    public boolean isFailure(Context context) {
        return failures.containsKey(context);
    }

    public MachineException getFailure(Context context) {
        return failures.get(context);
    }
}
