package org.graphwalker.maven.plugin;

/*
 * #%L
 * GraphWalker Maven Plugin
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.util.StringUtils;
import org.graphwalker.java.test.Configuration;
import org.graphwalker.java.test.Group;
import org.graphwalker.java.test.Manager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Nils Olsson
 */
@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE, lifecycle = "graphwalker")
public final class TestMojo extends DefaultMojoBase {

    @Parameter(property = "project.testClasspathElements")
    private List<String> classpathElements;

    @Parameter(defaultValue="${project.build.testOutputDirectory}")
    private File testClassesDirectory;

    @Parameter(defaultValue="${project.build.outputDirectory}")
    private File classesDirectory;

    @Parameter(defaultValue = "${project.build.directory}/graphwalker-reports")
    private File reportsDirectory;

    @Parameter(property = "maven.test.skip", defaultValue="false")
    private boolean mavenTestSkip;

    @Parameter(property = "skipTests", defaultValue = "false")
    private boolean skipTests;

    @Parameter(property = "graphwalker.test.skip", defaultValue = "false")
    private boolean graphwalkerTestSkip;

    @Parameter(property = "includes")
    private Set<String> includes;

    @Parameter(property = "excludes")
    private Set<String> excludes;

    @Parameter(property = "test", defaultValue = "*")
    private String test;

    @Parameter(property = "groups", defaultValue = "*")
    private String groups;

    protected List<String> getClasspathElements() {
        return classpathElements;
    }

    protected File getTestClassesDirectory() {
        return testClassesDirectory;
    }

    protected File getClassesDirectory() {
        return classesDirectory;
    }

    protected File getReportsDirectory() {
        return reportsDirectory;
    }

    protected boolean getSkipTests() {
        return mavenTestSkip || graphwalkerTestSkip || skipTests;
    }

    protected Set<String> getIncludes() {
        return includes;
    }

    protected Set<String> getExcludes() {
        return excludes;
    }

    protected String getTest() {
        if (System.getProperties().containsKey("test")) {
            return System.getProperty("test");
        }
        return test;
    }

    protected String getGroups() {
        if (System.getProperties().containsKey("groups")) {
            return System.getProperty("groups");
        }
        return groups;
    }

    protected ClassLoader createClassLoader() throws MojoExecutionException {
        try {
            return new URLClassLoader(convertToURL(getClasspathElements()), getClass().getClassLoader());
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Couldn''t create class loader");
        }
    }

    private URL[] convertToURL(List<String> elements) throws MalformedURLException {
        List<URL> urlList = new ArrayList<>();
        for (String element : elements) {
            urlList.add(new File(element).toURI().toURL());
        }
        return urlList.toArray(new URL[urlList.size()]);
    }

    protected ClassLoader switchClassLoader(ClassLoader newClassLoader) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(newClassLoader);
        return oldClassLoader;
    }

    protected Properties createProperties() {
        Properties properties = (Properties) System.getProperties().clone();
        properties.putAll((Properties) getMavenProject().getProperties().clone());
        properties.put("groups", groups);
        properties.put("test", test);
        properties.putAll((Properties) getSession().getUserProperties().clone());
        return properties;
    }

    protected Properties switchProperties(Properties properties) {
        Properties oldProperties = (Properties) System.getProperties().clone();
        System.setProperties(properties);
        return oldProperties;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!getSkipTests()) {
            ClassLoader classLoader = switchClassLoader(createClassLoader());
            Properties properties = switchProperties(createProperties());
            displayHeader();
            Configuration configuration = createConfiguration();
            Manager manager = new Manager(); //configuration, scanner.scan(getTestClassesDirectory(), getClassesDirectory()));
            displayConfiguration(manager);

// TODO: Implement the execution of tests
getLog().info("TODO: Implement the execution of tests");

            displayResult(manager);
            switchProperties(properties);
            switchClassLoader(classLoader);
            reportResults(manager);
        }
    }

    private void displayHeader() {
        if (getLog().isInfoEnabled()) {
            getLog().info("------------------------------------------------------------------------");
            getLog().info(" G r a p h W a l k e r                                                  ");
            getLog().info("------------------------------------------------------------------------");
        }
    }

    private Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        if (StringUtils.isBlank(getTest())) {
            configuration.setIncludes(getIncludes());
            configuration.setExcludes(getExcludes());
        } else {
            Set<String> include = new HashSet<>();
            Set<String> exclude = new HashSet<>();
            for (String test: getTest().split(",")) {
                test = test.trim();
                if (StringUtils.isNotBlank(test)) {
                    if (test.startsWith("!")) {
                        test = test.substring(1);
                        if (StringUtils.isNotBlank(test)) {
                            exclude.add(test);
                        }
                    } else {
                        include.add(test);
                    }
                }
            }
            configuration.setIncludes(include);
            configuration.setExcludes(exclude);
        }
        configuration.setClassesDirectory(getClassesDirectory());
        configuration.setTestClassesDirectory(getTestClassesDirectory());
        configuration.setReportsDirectory(getReportsDirectory());
        Set<String> groups = new HashSet<>();
        for (String group: getGroups().split(",")) {
            groups.add(group.trim());
        }
        configuration.setGroups(groups);
        return configuration;
    }

    private void displayConfiguration(Manager manager) {
        /*
        if (getLog().isInfoEnabled()) {
            getLog().info("Configuration:");
            getLog().info("    Include = "+manager.getConfiguration().getIncludes());
            getLog().info("    Exclude = "+manager.getConfiguration().getExcludes());
            getLog().info("     Groups = "+manager.getConfiguration().getGroups());
            getLog().info("   Parallel = false"); // TODO: gör så att man kan låta flera trådar köra samma test (kunna utföra lasttest)
            getLog().info("");
            getLog().info("Tests:");
            if (manager.getExecutionGroups().isEmpty()) {
                getLog().info("  No tests found");
            } else {
                for (Group group: manager.getExecutionGroups()) {
                    getLog().info("  [" + group.getName()+"]");
                    for (Execution execution: group.getExecutions()) {
                        getLog().info("    "+execution.getName()+"("+execution.getPathGenerator().getSimpleName()+", "+execution.getStopCondition().getSimpleName()+", \""+execution.getStopConditionValue()+"\")");
                    }
                    getLog().info("");
                }
            }
            getLog().info("------------------------------------------------------------------------");
        }
        */
    }

    private void displayResult(Manager manager) {
        /*
        if (getLog().isInfoEnabled()) {
            getLog().info("------------------------------------------------------------------------");
            getLog().info("");
            getLog().info(ResourceUtils.getText(Bundle.NAME, "result.label"));
            getLog().info("");
            long groups = manager.getGroupCount(), tests = manager.getTestCount(), completed = 0, failed = 0, notExecuted = 0;
            List<ExecutionContext> failedExecutions = new ArrayList<>();
            for (Machine machine: machines) {
                for (ExecutionContext context: machine.getExecutionContexts()) {
                    switch (context.getExecutionStatus()) {
                        case COMPLETED: {
                            completed++;
                        }
                        break;
                        case FAILED: {
                            failed++;
                            failedExecutions.add(context);
                        }
                        break;
                        case NOT_EXECUTED: {
                            notExecuted++;
                        }
                        break;
                    }
                }
            }
            if (!failedExecutions.isEmpty()) {
                getLog().info("Failed executions: ");
                for (ExecutionContext context: failedExecutions) {
                    double fulfilment = context.getPathGenerator().getStopCondition().getFulfilment(context);
                    String pathGenerator = context.getPathGenerator().getClass().getSimpleName();
                    String stopCondition = context.getPathGenerator().getStopCondition().getClass().getSimpleName();
                    String value = context.getPathGenerator().getStopCondition().getValue();
                    getLog().info("  " + implementations.get(context).getClass().getName()+"("+pathGenerator+", "+stopCondition+", "+value+"): "+Math.round(100*fulfilment)+"%");
                }
                getLog().info("");
            }
            getLog().info(ResourceUtils.getText(Bundle.NAME, "result.summary", groups, tests, completed, failed, notExecuted));
            getLog().info("");
        }
        */
    }

    private void reportResults(Manager manager) throws MojoExecutionException {
        /*
        boolean hasExceptions = false;
        for (Machine machine: machines) {
            //reportWriter.writeReport(graphWalker, reportsDirectory, session.getStartTime());
            for (ExecutionContext context: machine.getExecutionContexts()) {
                hasExceptions |= ExecutionStatus.FAILED.equals(context.getExecutionStatus());
            }
        }
        if (hasExceptions) {
            throw new MojoExecutionException(ResourceUtils.getText(Bundle.NAME, "exception.execution.failed", getReportsDirectory().getAbsolutePath()));
        }
        */
    }
}
