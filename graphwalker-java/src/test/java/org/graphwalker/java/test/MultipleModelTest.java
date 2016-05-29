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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kristian Karl
 */
public class MultipleModelTest {

  public final static Path MODEL_PATH_1 = Paths.get("org/graphwalker/java/test/MultipleModel_1.graphml");
  public final static Path MODEL_PATH_2 = Paths.get("org/graphwalker/java/test/MultipleModel_2.graphml");

  @Test
  public void run() throws IOException {
    MultipleModel_1 model_1 = new MultipleModel_1();
    MultipleModel_2 model_2 = new MultipleModel_2();

    new TestBuilder()
      .addContext(model_1.setPathGenerator(new RandomPath(new EdgeCoverage(100))), MODEL_PATH_1)
      .addContext(model_2.setPathGenerator(new RandomPath(new EdgeCoverage(100))), MODEL_PATH_2)
      .execute();
    Assert.assertTrue(model_1.count >= 4);
    Assert.assertTrue(model_2.count >= 3);
  }
}
