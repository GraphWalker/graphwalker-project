package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
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

import org.junit.Test;

import static org.graphwalker.core.model.Classification.RuntimeClassification;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Nils Olsson
 */
public class ClassificationTest {

  @Test
  public void create() {
    Classification root = new Classification();
    root.setName("root");
    root.addClassification(new Classification().setName("leaf1"));
    root.addClassification(new Classification().setName("leaf2"));
    assertNotNull(root);
    assertEquals("root", root.getName());
    assertThat(root.getClassifications().size(), is(2));
    RuntimeClassification runtimeRoot = root.build();
    assertNotNull(runtimeRoot);
    assertThat(runtimeRoot.getClassifications().size(), is(2));
    assertNotNull(runtimeRoot.getClassifications().get(0));
    assertNotNull(runtimeRoot.getClassifications().get(0).getName());
    assertNotNull(runtimeRoot.getClassifications().get(0).getClassifications());
    assertThat(runtimeRoot.getClassifications().get(0).getClassifications().size(), is(0));
    assertNotNull(runtimeRoot.getClassifications().get(1));
    assertNotNull(runtimeRoot.getClassifications().get(1).getName());
    assertNotNull(runtimeRoot.getClassifications().get(1).getClassifications());
    assertThat(runtimeRoot.getClassifications().get(1).getClassifications().size(), is(0));
  }
}
