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
// This file is part of the GraphWalker java package
// The MIT License
//
// Copyright (c) 2010 graphwalker.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.graphwalker.cli;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;


public class CorrectModelsTest extends CLITestRoot {

  /**
   * Simplest model
   */
  @Test
  public void simplestModel() {
    String args[] = {"offline", "-m", "graphml/CorrectModels/simplestModel.graphml", "random(vertex_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(result.getOutput(), is("{\"currentElementName\":\"e1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v1\"}" + System.lineSeparator()));
  }

  /**
   * shortest All Paths Vertex Coverage
   */
  @Test
  public void shortestAllPathsVertexCoverage() {
    String args[] = {"offline", "-m", "graphml/CorrectModels/shortestAllPathsVertexCoverage.graphml", "shortest_all_paths(vertex_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(result.getOutput(), is("{\"currentElementName\":\"e1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e2\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v2\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e4\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v4\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e6\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e3\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v3\"}" + System.lineSeparator()));
  }

  /**
   * shortest All Paths Vertex Coverage
   */
  @Test
  public void loginNoErrors() {
    String args[] = {"offline", "-o", "-m", "graphml/Login.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
  }

  /**
   * No start vertex
   */
  @Test
  public void noStartVertex() {
    String args[] = {"offline", "-e", "v1", "-m", "graphml/CorrectModels/modelWithNoStartVertex.graphml", "a_star(reached_edge(e4))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(result.getOutput(), is("{\"currentElementName\":\"v1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e2\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v2\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e4\"}" + System.lineSeparator()));
  }

  /**
   * Don't use blocked feature
   */
  @Test
  public void dontUseBlocked() {
    String args[] = {"offline", "-b", "false", "-m", "graphml/CorrectModels/blockedVertex.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(result.getOutput(), is("{\"currentElementName\":\"e1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"e2\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v2\"}" + System.lineSeparator()));
  }

  /**
   * Use blocked feature
   */
  @Test
  public void useBlocked() {
    String args[] = {"offline", "-m", "graphml/CorrectModels/blockedVertex.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(result.getOutput(), is("{\"currentElementName\":\"e1\"}" + System.lineSeparator() +
                                             "{\"currentElementName\":\"v1\"}" + System.lineSeparator()));
  }

  /**
   * Don't use blocked feature with a json file
   */
  @Test
  public void dontUseBlockedJson() {
    String args[] = {"offline", "-b", "false", "-g", "json/graphWithBlockedElements.json"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(Arrays.asList(result.getOutput().split(System.lineSeparator())),
                      hasItems("{\"currentElementName\":\"e1\"}",
                               "{\"currentElementName\":\"e2\"}",
                               "{\"currentElementName\":\"e3\"}",
                               "{\"currentElementName\":\"e4\"}",
                               "{\"currentElementName\":\"e5\"}",
                               "{\"currentElementName\":\"e6\"}",
                               "{\"currentElementName\":\"e7\"}",
                               "{\"currentElementName\":\"e8\"}",
                               "{\"currentElementName\":\"e9\"}",
                               "{\"currentElementName\":\"e10\"}",
                               "{\"currentElementName\":\"v1\"}",
                               "{\"currentElementName\":\"v2\"}",
                               "{\"currentElementName\":\"v3\"}",
                               "{\"currentElementName\":\"v4\"}",
                               "{\"currentElementName\":\"v5\"}"));
  }

  /**
   * Use blocked feature with a json file
   */
  @Test
  public void useBlockedJson() {
    String args[] = {"offline", "-g", "json/graphWithBlockedElements.json"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is(""));
    Assert.assertThat(Arrays.asList(result.getOutput().split(System.lineSeparator())),
                      hasItems("{\"currentElementName\":\"e1\"}",
                               "{\"currentElementName\":\"e2\"}",
                               "{\"currentElementName\":\"e3\"}",
                               "{\"currentElementName\":\"e4\"}",
                               "{\"currentElementName\":\"e7\"}",
                               "{\"currentElementName\":\"e8\"}",
                               "{\"currentElementName\":\"e9\"}",
                               "{\"currentElementName\":\"v1\"}",
                               "{\"currentElementName\":\"v2\"}",
                               "{\"currentElementName\":\"v3\"}",
                               "{\"currentElementName\":\"v4\"}"));

    Assert.assertThat(Arrays.asList(result.getOutput().split(System.lineSeparator())),
                      not(hasItems("{\"currentElementName\":\"e5\"}",
                                   "{\"currentElementName\":\"e6\"}",
                                   "{\"currentElementName\":\"v5\"}")));
  }
}
