package org.graphwalker.java.source;

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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class GenerateTest {

  @Test
  public void generate() {
    String source = new CodeGenerator().generate("/org/graphwalker/java/annotation/MyModel.graphml");
    Assert.assertTrue(source.contains("edge12"));
    Assert.assertTrue(source.contains("vertex2"));
    Assert.assertFalse(source.contains("SHARED"));
  }

  @Test
  public void generatePathWithSpace() {
    String source = new CodeGenerator().generate("/org/graphwalker/java/path with space/MyModel.graphml");
    Assert.assertTrue(source.contains("edge12"));
    Assert.assertTrue(source.contains("vertex2"));
    Assert.assertFalse(source.contains("SHARED"));
    Assert.assertTrue(source.contains("package org.graphwalker.java.path_with_space;"));
  }
}
