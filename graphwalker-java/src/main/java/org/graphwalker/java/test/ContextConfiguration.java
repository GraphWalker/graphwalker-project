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

import org.graphwalker.java.annotation.GraphWalker;

import static org.graphwalker.core.common.Objects.isNotNull;

/**
 * @author Nils Olsson
 */
public class ContextConfiguration {

  private Class<?> testClass;
  private String testClassName;
  private String pathGeneratorName;
  private String stopConditionName;
  private String stopConditionValue;

  public ContextConfiguration() {
  }

  public ContextConfiguration(Class<?> testClass) {
    setTestClass(testClass);
  }

  public Class<?> getTestClass() {
    return testClass;
  }

  public void setTestClass(Class<?> testClass) {
    this.testClass = testClass;
    setTestClassName(testClass.getSimpleName());
    GraphWalker configuration = testClass.getAnnotation(GraphWalker.class);
    if (isNotNull(configuration)) {
      setPathGeneratorName(configuration.pathGenerator().getSimpleName());
      setStopConditionName(configuration.stopCondition().getSimpleName());
      setStopConditionValue(configuration.stopConditionValue());
    }
  }

  public String getTestClassName() {
    return testClassName;
  }

  public void setTestClassName(String testClassName) {
    this.testClassName = testClassName;
  }

  public String getPathGeneratorName() {
    return pathGeneratorName;
  }

  public void setPathGeneratorName(String pathGeneratorName) {
    this.pathGeneratorName = pathGeneratorName;
  }

  public String getStopConditionName() {
    return stopConditionName;
  }

  public void setStopConditionName(String stopConditionName) {
    this.stopConditionName = stopConditionName;
  }

  public String getStopConditionValue() {
    return stopConditionValue;
  }

  public void setStopConditionValue(String stopConditionValue) {
    this.stopConditionValue = stopConditionValue;
  }
}
