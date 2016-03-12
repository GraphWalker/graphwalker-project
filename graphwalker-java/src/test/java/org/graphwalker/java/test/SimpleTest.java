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

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class SimpleTest extends ExecutionContext implements SimpleModel {

  public final static Path MODEL_PATH = Paths.get("org/graphwalker/java/test/SimpleModel.graphml");

  public int count = 0;

  @Override
  public void vertex() {
    count++;
  }

  @Override
  public void edge() {
    count++;
  }

  @Test
  public void run2() {
    SimpleTest context = new SimpleTest();
    new TestBuilder()
      .setModel(MODEL_PATH)
      .setContext(context)
      .setPathGenerator(new RandomPath(new VertexCoverage(100)))
      .setStart("edge").execute();
    Assert.assertThat(context.count, is(2));
  }

  @Test
  public void run() {
    Result result = new TestBuilder().addModel(MODEL_PATH
      , new SimpleTest().setPathGenerator(new RandomPath(new VertexCoverage(100)))).execute();
    Assert.assertThat(result.getCompletedCount(), is(1));
  }
}
