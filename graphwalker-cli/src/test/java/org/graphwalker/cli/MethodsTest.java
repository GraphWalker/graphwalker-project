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

package org.graphwalker.cli;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;


public class MethodsTest extends CLITestRoot {

  @Test
  public void methodsDontUseBlockedFeature() throws IOException {
    String args[] = {"methods", "-b", "false", "-m", "json/graphWithBlockedElements.json"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(Arrays.asList(result.getOutput().split("\\s+")),
                      containsInAnyOrder("e1",
                                         "e2",
                                         "e3",
                                         "e4",
                                         "e5",
                                         "e6",
                                         "e7",
                                         "e8",
                                         "e9",
                                         "e10",
                                         "v1",
                                         "v2",
                                         "v3",
                                         "v4",
                                         "v5"));
  }

  @Test
  public void methodsUseBlockedFeature() throws IOException {
    String args[] = {"methods", "-m", "json/graphWithBlockedElements.json"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(Arrays.asList(result.getOutput().split("\\s+")),
                      containsInAnyOrder("e1",
                                         "e2",
                                         "e3",
                                         "e4",
                                         "e7",
                                         "e8",
                                         "e9",
                                         "v1",
                                         "v2",
                                         "v3",
                                         "v4"));
  }
}
