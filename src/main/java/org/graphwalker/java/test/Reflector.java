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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Nils Olsson
 */
public final class Reflector implements Executor {

    private final ClassLoader classLoader;
    private final Class<?> collectionClass;
    private final Class<?> executorClass;
    private final Class<?> configurationClass;
    private final Class<?> resultClass;
    private final Class<?> machineConfigurationClass;
    private final Class<?> contextConfigurationClass;
    private final Method setIncludes;
    private final Method setExcludes;
    private final Method setGroups;
    private final Method execute;
    private final Method getValue;
    private final Method setValue;
    private final Method getMachineConfiguration;
    private final Method getContextConfigurations;
    private final Method getTestClass;
    private final Method getPathGeneratorName;
    private final Method getStopConditionName;
    private final Method getStopConditionValue;
    private final Method setTestClass;
    private final Method setPathGeneratorName;
    private final Method setStopConditionName;
    private final Method setStopConditionValue;
    private final Object executor;

    public Reflector(Configuration configuration, ClassLoader classLoader) {
        ClassLoader contextClassLoader = switchClassLoader(classLoader);
        this.classLoader = classLoader;
        this.collectionClass = Reflections.loadClass(classLoader, Collection.class);
        this.executorClass = Reflections.loadClass(classLoader, TestExecutor.class);
        this.configurationClass = Reflections.loadClass(classLoader, Configuration.class);
        this.resultClass = Reflections.loadClass(classLoader, Result.class);
        this.machineConfigurationClass = Reflections.loadClass(classLoader, MachineConfiguration.class);
        this.contextConfigurationClass = Reflections.loadClass(classLoader, ContextConfiguration.class);
        this.setIncludes = Reflections.getMethod(configurationClass, "setIncludes", collectionClass);
        this.setExcludes = Reflections.getMethod(configurationClass, "setExcludes", collectionClass);
        this.setGroups = Reflections.getMethod(configurationClass, "setGroups", collectionClass);
        this.execute = Reflections.getMethod(executorClass, "execute");
        this.getValue = Reflections.getMethod(resultClass, "getValue");
        this.setValue = Reflections.getMethod(Result.class, "setValue", Integer.TYPE);
        this.getMachineConfiguration = Reflections.getMethod(executorClass, "getMachineConfiguration");
        this.getContextConfigurations = Reflections.getMethod(machineConfigurationClass, "getContextConfigurations");
        this.getTestClass = Reflections.getMethod(contextConfigurationClass, "getTestClass");
        this.getPathGeneratorName = Reflections.getMethod(contextConfigurationClass, "getPathGeneratorName");
        this.getStopConditionName = Reflections.getMethod(contextConfigurationClass, "getStopConditionName");
        this.getStopConditionValue = Reflections.getMethod(contextConfigurationClass, "getStopConditionValue");
        this.setTestClass = Reflections.getMethod(ContextConfiguration.class, "setTestClass", Class.class);
        this.setPathGeneratorName = Reflections.getMethod(ContextConfiguration.class, "setPathGeneratorName", String.class);
        this.setStopConditionName = Reflections.getMethod(ContextConfiguration.class, "setStopConditionName", String.class);
        this.setStopConditionValue = Reflections.getMethod(ContextConfiguration.class, "setStopConditionValue", String.class);
        this.executor = createExecutor(configuration);
        switchClassLoader(contextClassLoader);
    }

    private Object createExecutor(Configuration configuration) {
        Constructor<?> constructor = Reflections.getConstructor(classLoader, executorClass, configurationClass);
        return Reflections.newInstance(constructor, createConfiguration(configuration));
    }

    private Object createConfiguration(Configuration configuration) {
        Object newConfiguration = Reflections.newInstance(classLoader, configurationClass);
        Reflections.invoke(newConfiguration, setIncludes, configuration.getIncludes());
        Reflections.invoke(newConfiguration, setExcludes, configuration.getExcludes());
        Reflections.invoke(newConfiguration, setGroups, configuration.getGroups());
        return newConfiguration;
    }

    private ClassLoader switchClassLoader(ClassLoader classLoader) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        return contextClassLoader;
    }

    @Override
    public Result execute() {
        ClassLoader contextClassLoader = switchClassLoader(classLoader);
        Result result = createResult(Reflections.invoke(executor, execute));
        switchClassLoader(contextClassLoader);
        return result;
    }

    private Result createResult(Object result) {
        Result newResult = new Result();
        Reflections.invoke(newResult, setValue, Reflections.invoke(result, getValue));
        return newResult;
    }

    @SuppressWarnings("unchecked")
    public MachineConfiguration getMachineConfiguration() {
        ClassLoader contextClassLoader = switchClassLoader(classLoader);
        MachineConfiguration newMachineConfiguration = new MachineConfiguration();
        Object machineConfiguration = Reflections.invoke(executor, getMachineConfiguration);
        for (Object contextConfiguration: (Collection<Object>)Reflections.invoke(machineConfiguration, getContextConfigurations)) {
            ContextConfiguration newContextConfiguration = new ContextConfiguration();
            Class<?> testClass = (Class<?>)Reflections.invoke(contextConfiguration, getTestClass);
            Reflections.invoke(newContextConfiguration, setTestClass, Reflections.loadClass(contextClassLoader, testClass));
            Reflections.invoke(newContextConfiguration, setPathGeneratorName, Reflections.invoke(contextConfiguration, getPathGeneratorName));
            Reflections.invoke(newContextConfiguration, setStopConditionName, Reflections.invoke(contextConfiguration, getStopConditionName));
            Reflections.invoke(newContextConfiguration, setStopConditionValue, Reflections.invoke(contextConfiguration, getStopConditionValue));
            newMachineConfiguration.addContextConfiguration(newContextConfiguration);
        }
        switchClassLoader(contextClassLoader);
        return newMachineConfiguration;
    }
}
