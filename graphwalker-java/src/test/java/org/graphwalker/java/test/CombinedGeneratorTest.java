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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.CombinedPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kristian Karl
 */
public class CombinedGeneratorTest extends ExecutionContext implements CombinedGeneratorModel {

  public final static Path MODEL_PATH = Paths.get("org/graphwalker/java/test/CombinedGeneratorModel.graphml");

  public int count = 0;

  @Override
  public void v_1() {
    count++;
  }

  @Override
  public void v_2() {
    count++;
  }

  @Override
  public void e_1() {
    count++;
  }

  @Override
  public void e_2() {
    count++;
  }

  @Override
  public void e_3() {
    count++;
  }

  @Override
  public void e_4() {
    count++;
  }

  @Test
  public void run() throws IOException {
    CombinedGeneratorTest context = new CombinedGeneratorTest();
    CombinedPath combinedPath = new CombinedPath();
    combinedPath.addPathGenerator(new RandomPath(new EdgeCoverage(100)));
    combinedPath.addPathGenerator(new RandomPath(new VertexCoverage(100)));
    new TestBuilder()
        .addContext(context.setPathGenerator(combinedPath).setNextElement(new Edge().setName("e_1").build()), MODEL_PATH)
        .execute();
    Assert.assertTrue(context.count >= 6);
  }
}
