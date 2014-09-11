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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;

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
        configuration.setTestClassesDirectory(new File("my/classes"));
        Assert.assertThat(configuration.getTestClassesDirectory(), is(new File("my/classes")));
        configuration.setClassesDirectory(new File("my/test-classes"));
        Assert.assertThat(configuration.getClassesDirectory(), is(new File("my/test-classes")));
        configuration.setReportsDirectory(new File("my/report"));
        Assert.assertThat(configuration.getReportsDirectory(), is(new File("my/report")));
    }

    @Test
    public void minimalConfigurationTest() {
        Configuration configuration = new Configuration();
        Assert.assertNotNull(configuration.getExcludes());
        Assert.assertNotNull(configuration.getIncludes());
        Assert.assertNotNull(configuration.getTestClassesDirectory());
        Assert.assertNotNull(configuration.getClassesDirectory());
        Assert.assertNotNull(configuration.getGroups());
        Assert.assertNotNull(configuration.getReportsDirectory());
    }
}
