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

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;

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
    private final Map<ExecutionContext, Object> implementations = new HashMap<>();
    private final List<Machine> machines = new ArrayList<>();

    public Executor(Manager manager) {
        this.manager = manager;
    }

    public List<Machine> getMachines() {
        return Collections.unmodifiableList(machines);
    }

    public Map<ExecutionContext, Object> getImplementations() {
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
                List<ExecutionContext> executionContexts = new ArrayList<>();
                for (Execution execution: group.getExecutions()) {
                    try {
                        StopCondition stopCondition = createStopCondition(execution.getStopCondition(), execution.getStopConditionValue());
                        PathGenerator pathGenerator = execution.getPathGenerator().getConstructor(StopCondition.class).newInstance(stopCondition);
                        Object implementation = execution.getTestClass().newInstance();
                        Model model = execution.getModel();
                        ExecutionContext executionContext = new ExecutionContext(model, pathGenerator);
                        List<Element> elements = model.build().findElements(execution.getStart());
                        executionContext.setNextElement(elements.get(new Random(System.nanoTime()).nextInt(elements.size())));
                        implementations.put(executionContext, implementation);
                        executionContexts.add(executionContext);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                machines.add(new SimpleMachine(executionContexts));
            }
            try {
                ExecutorService executorService = Executors.newFixedThreadPool(machines.size());
                for (final Machine machine : machines) {
                    executorService.execute(new Runnable() {
                        public void run() {
                            //AnnotationUtils.execute(BeforeExecution.class, machine.getCurrentExecutionContext(), implementations.get(machine.getCurrentExecutionContext()));
                            while (machine.hasNextStep()) {
                                ExecutionContext context = (ExecutionContext)machine.getNextStep();

                                System.out.println(context.getCurrentElement().getName());

                                /*
                                if (null != context.getCurrentElement().getName() && !"Start".equals(context.getName())) {
                                    try {
                                        ReflectionUtils.execute(implementations.get(machine.getCurrentExecutionContext())
                                                , context.getName(), machine.getCurrentExecutionContext().getScriptContext());
                                    } catch (Throwable throwable) {
                                        //machine.failCurrentStep();
                                        //AnnotationUtils.execute(machine.getCurrentExecutionContext(), implementations.get(machine.getCurrentExecutionContext()), throwable);
                                    }
                                }
                                */
                            }
                            //AnnotationUtils.execute(AfterExecution.class, machine.getCurrentExecutionContext(), implementations.get(machine.getCurrentExecutionContext()));
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
}
