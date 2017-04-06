package org.graphwalker.cli;

/*
 * #%L
 * GraphWalker Command Line Interface
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


public class IOErrorsTest extends CLITestRoot {

  /**
   * Non existent file
   */
  @Test
  public void nonExistentFile() {
    String args[] = {"offline", "-m", "sdsdtkdsjhsl.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is("An error occurred when running command: " +
                                            "offline -m sdsdtkdsjhsl.graphml random(edge_coverage(100))" +
                                            System.lineSeparator() + "Could not read the file." + System.lineSeparator() + System.lineSeparator()));
    Assert.assertThat(result.getOutput(), is(""));
  }
}
