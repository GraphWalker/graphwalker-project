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

import static org.hamcrest.core.Is.is;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ConfigurationTest {

  @Test
  public void configurationTest() {
    Configuration configuration = new Configuration();
    configuration.addExclude("exclude");
    Assert.assertThat(configuration.getExcludes().size(), is(1));
    configuration.addInclude("include");
    Assert.assertThat(configuration.getIncludes().size(), is(1));
    configuration.addGroup("group");
    Assert.assertThat(configuration.getGroups().size(), is(1));
  }

  @Test
  public void minimalConfigurationTest() {
    Configuration configuration = new Configuration();
    Assert.assertNotNull(configuration.getExcludes());
    Assert.assertNotNull(configuration.getIncludes());
    Assert.assertNotNull(configuration.getGroups());
  }
}
