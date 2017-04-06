package org.graphwalker.java.annotation;

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

import java.util.Set;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.java.annotation.resources.MyTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class AnnotationTest {

  @Test
  public void getAnnotationsTest() {
    Set<GraphWalker> annotations = AnnotationUtils.getAnnotations(MyTest.class, GraphWalker.class);
    Assert.assertThat(annotations.size(), is(1));
    Assert.assertTrue(annotations.toArray()[0] instanceof GraphWalker);
    GraphWalker annotation = (GraphWalker) annotations.toArray()[0];
    Assert.assertThat(annotation.start(), is("vertex1"));
    Assert.assertThat(annotation.groups(), is(new String[]{"MyTests"}));
    Assert.assertTrue(RandomPath.class.isAssignableFrom(annotation.pathGenerator()));
    Assert.assertTrue(VertexCoverage.class.isAssignableFrom(annotation.stopCondition()));
    Assert.assertThat(annotation.stopConditionValue(), is("100"));
    Assert.assertThat(AnnotationUtils.getAnnotations(MyTest.class, Vertex.class).size(), is(0));
  }

  @Test
  public void executeTest() {
    MyTest myTest = new MyTest();
    AnnotationUtils.execute(BeforeExecution.class, myTest);
    Assert.assertThat(myTest.getCount(), is(1));
    AnnotationUtils.execute(AfterExecution.class, myTest);
    Assert.assertThat(myTest.getCount(), is(2));
    AnnotationUtils.execute(BeforeElement.class, myTest);
    Assert.assertThat(myTest.getCount(), is(3));
  }

  @Test
  public void executePrivateTest() {
    MyTest myTest = new MyTest();
    AnnotationUtils.execute(AfterElement.class, myTest);
  }
}
