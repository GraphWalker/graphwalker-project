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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

/**
 * @author Nils Olsson
 */
public final class Reflector {

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
    private final Method reportResults;
    private final Method setTestCount;
    private final Method getTestCount;
    private final Method setCompletedCount;
    private final Method getCompletedCount;
    private final Method setIncompleteCount;
    private final Method getIncompleteCount;
    private final Method setFailedCount;
    private final Method getFailedCount;
    private final Method setNotExecutedCount;
    private final Method getNotExecutedCount;
    private final Method getMachineConfiguration;
    private final Method getContextConfigurations;
    private final Method getTestClassName;
    private final Method getPathGeneratorName;
    private final Method getStopConditionName;
    private final Method getStopConditionValue;
    private final Method setTestClassName;
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
        this.reportResults = Reflections.getMethod(executorClass, "reportResults", File.class, Date.class, Properties.class);
        this.setTestCount = Reflections.getMethod(Result.class, "setTestCount", Integer.TYPE);
        this.getTestCount = Reflections.getMethod(resultClass, "getTestCount");
        this.setCompletedCount = Reflections.getMethod(Result.class, "setCompletedCount", Integer.TYPE);
        this.getCompletedCount = Reflections.getMethod(resultClass, "getCompletedCount");
        this.setIncompleteCount = Reflections.getMethod(Result.class, "setIncompleteCount", Integer.TYPE);
        this.getIncompleteCount = Reflections.getMethod(resultClass, "getIncompleteCount");
        this.setFailedCount = Reflections.getMethod(Result.class, "setFailedCount", Integer.TYPE);
        this.getFailedCount = Reflections.getMethod(resultClass, "getFailedCount");
        this.setNotExecutedCount = Reflections.getMethod(Result.class, "setNotExecutedCount", Integer.TYPE);
        this.getNotExecutedCount = Reflections.getMethod(resultClass, "getNotExecutedCount");
        this.getMachineConfiguration = Reflections.getMethod(executorClass, "getMachineConfiguration");
        this.getContextConfigurations = Reflections.getMethod(machineConfigurationClass, "getContextConfigurations");
        this.getTestClassName = Reflections.getMethod(contextConfigurationClass, "getTestClassName");
        this.getPathGeneratorName = Reflections.getMethod(contextConfigurationClass, "getPathGeneratorName");
        this.getStopConditionName = Reflections.getMethod(contextConfigurationClass, "getStopConditionName");
        this.getStopConditionValue = Reflections.getMethod(contextConfigurationClass, "getStopConditionValue");
        this.setTestClassName = Reflections.getMethod(ContextConfiguration.class, "setTestClassName", String.class);
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

    public Result execute() {
        ClassLoader contextClassLoader = switchClassLoader(classLoader);
        Result result = createResult(Reflections.invoke(executor, execute));
        switchClassLoader(contextClassLoader);
        return result;
    }

    private Result createResult(Object result) {
        Result newResult = new Result();
        Reflections.invoke(newResult, setTestCount, Reflections.invoke(result, getTestCount));
        Reflections.invoke(newResult, setCompletedCount, Reflections.invoke(result, getCompletedCount));
        Reflections.invoke(newResult, setIncompleteCount, Reflections.invoke(result, getIncompleteCount));
        Reflections.invoke(newResult, setFailedCount, Reflections.invoke(result, getFailedCount));
        Reflections.invoke(newResult, setNotExecutedCount, Reflections.invoke(result, getNotExecutedCount));
        return newResult;
    }

    @SuppressWarnings("unchecked")
    public MachineConfiguration getMachineConfiguration() {
        ClassLoader contextClassLoader = switchClassLoader(classLoader);
        MachineConfiguration newMachineConfiguration = new MachineConfiguration();
        Object machineConfiguration = Reflections.invoke(executor, getMachineConfiguration);
        for (Object contextConfiguration : (Collection<Object>) Reflections.invoke(machineConfiguration, getContextConfigurations)) {
            ContextConfiguration newContextConfiguration = new ContextConfiguration();
            Reflections.invoke(newContextConfiguration, setTestClassName, Reflections.invoke(contextConfiguration, getTestClassName));
            Reflections.invoke(newContextConfiguration, setPathGeneratorName, Reflections.invoke(contextConfiguration, getPathGeneratorName));
            Reflections.invoke(newContextConfiguration, setStopConditionName, Reflections.invoke(contextConfiguration, getStopConditionName));
            Reflections.invoke(newContextConfiguration, setStopConditionValue, Reflections.invoke(contextConfiguration, getStopConditionValue));
            newMachineConfiguration.addContextConfiguration(newContextConfiguration);
        }
        switchClassLoader(contextClassLoader);
        return newMachineConfiguration;
    }

    public void reportResults(File file, Date startTime, Properties properties) {
        ClassLoader contextClassLoader = switchClassLoader(classLoader);
        Reflections.invoke(executor, reportResults, file, startTime, properties);
        switchClassLoader(contextClassLoader);
    }
}
