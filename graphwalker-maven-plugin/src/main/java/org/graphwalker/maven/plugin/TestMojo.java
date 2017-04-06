package org.graphwalker.maven.plugin;

/*
 * #%L
 * GraphWalker Maven Plugin
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.StringUtils;
import org.graphwalker.java.test.Configuration;
import org.graphwalker.java.test.ContextConfiguration;
import org.graphwalker.java.test.IsolatedClassLoader;
import org.graphwalker.java.test.Reflector;
import org.graphwalker.java.test.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
@Execute(phase = LifecyclePhase.TEST_COMPILE, lifecycle = "graphwalker")
public final class TestMojo extends DefaultMojoBase {

  private static final Logger logger = LoggerFactory.getLogger(TestMojo.class);

  @Parameter(property = "project.testClasspathElements")
  private List<String> classpathElements;

  @Parameter(defaultValue = "${project.build.testOutputDirectory}")
  private File testClassesDirectory;

  @Parameter(defaultValue = "${project.build.outputDirectory}")
  private File classesDirectory;

  @Parameter(defaultValue = "${project.build.directory}/graphwalker-reports")
  private File reportsDirectory;

  @Parameter(property = "maven.test.skip", defaultValue = "false")
  private boolean mavenTestSkip;

  @Parameter(property = "skipTests", defaultValue = "false")
  private boolean skipTests;

  @Parameter(property = "graphwalker.test.skip", defaultValue = "false")
  private boolean graphwalkerTestSkip;

  @Parameter(property = "graphwalker.includes")
  private Set<String> includes;

  @Parameter(property = "graphwalker.excludes")
  private Set<String> excludes;

  @Parameter(property = "graphwalker.test", defaultValue = "*")
  private String test;

  @Parameter(property = "graphwalker.groups", defaultValue = "*")
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

  protected Collection<String> getIncludes() {
    return includes;
  }

  protected Collection<String> getExcludes() {
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
      displayHeader();
      ClassLoader classLoader = new IsolatedClassLoader(classpathElements);
      Properties properties = switchProperties(createProperties());
      Configuration configuration = createConfiguration();
      Reflector reflector = new Reflector(configuration, classLoader);
      displayConfiguration(configuration, reflector);
      Result result = reflector.execute();
      displayResult(result);
      reflector.reportResults(getReportsDirectory(), getSession().getStartTime(), getSession().getSystemProperties());
      switchProperties(properties);
    }
  }

  private void displayHeader() {
    if (getLog().isInfoEnabled()) {
      getLog().info("------------------------------------------------------------------------");
      getLog().info("  _____             _   _ _ _     _ _                                   ");
      getLog().info(" |   __|___ ___ ___| |_| | | |___| | |_ ___ ___                         ");
      getLog().info(" |  |  |  _| .'| . |   | | | | .'| | '_| -_|  _|                        ");
      getLog().info(" |_____|_| |__,|  _|_|_|_____|__,|_|_,_|___|_|                          ");
      getLog().info("               |_|         (" + getVersion() + ")                            ");
      getLog().info("------------------------------------------------------------------------");
    }
  }

  private String getVersion() {
    Properties properties = new Properties();
    InputStream inputStream = getClass().getResourceAsStream("/version.properties");
    if (null != inputStream) {
      try {
        properties.load(inputStream);
      } catch (IOException e) {
        logger.error(e.getMessage());
        return "unknown";
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    }
    return properties.getProperty("graphwalker.version");
  }

  private Configuration createConfiguration() {
    Configuration configuration = new Configuration();
    if (StringUtils.isBlank(getTest())) {
      configuration.setIncludes(getIncludes());
      configuration.setExcludes(getExcludes());
    } else {
      for (String test : getTest().split(",")) {
        test = test.trim();
        if (StringUtils.isNotBlank(test)) {
          if (test.startsWith("!")) {
            test = test.substring(1);
            if (StringUtils.isNotBlank(test)) {
              configuration.addExclude(test);
            }
          } else {
            configuration.addInclude(test);
          }
        }
      }
    }
    for (String group : getGroups().split(",")) {
      configuration.addGroup(group.trim());
    }
    return configuration;
  }

  private void displayConfiguration(Configuration configuration, Reflector reflector) {
    if (getLog().isInfoEnabled()) {
      getLog().info("Configuration:");
      getLog().info("    Include = " + configuration.getIncludes());
      getLog().info("    Exclude = " + configuration.getExcludes());
      getLog().info("     Groups = " + configuration.getGroups());
      getLog().info("");
      getLog().info("Tests:");
      if (null == reflector.getMachineConfiguration() || reflector.getMachineConfiguration().getContextConfigurations().isEmpty()) {
        getLog().info("  No tests found");
      } else {
        for (ContextConfiguration context : reflector.getMachineConfiguration().getContextConfigurations()) {
          getLog().info("    "
                        + context.getTestClassName() + "("
                        + context.getPathGeneratorName() + ", "
                        + context.getStopConditionName() + ", "
                        + context.getStopConditionValue() + ")");
        }
        getLog().info("");
      }
      getLog().info("------------------------------------------------------------------------");
    }
  }

  private void displayResult(Result result) {
    if (getLog().isErrorEnabled() && result.hasErrors()) {
      getLog().info("------------------------------------------------------------------------");
      for (String error : result.getErrors()) {
        getLog().error(error);

      }
    }
    if (getLog().isInfoEnabled()) {
      getLog().info("------------------------------------------------------------------------");
      getLog().info("");
      getLog().info("Result :");
      getLog().info("");
      getLog().info(result.getResultsAsString());
      getLog().info("");
    }
  }
}
