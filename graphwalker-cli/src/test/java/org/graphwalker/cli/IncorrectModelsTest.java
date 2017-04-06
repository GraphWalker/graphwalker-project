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


public class IncorrectModelsTest extends CLITestRoot {

  /**
   * wrong vertex syntax
   */
  @Test
  public void wrongVertexSyntax() {
    String args[] = {"offline", "-m", "graphml/IncorrectModels/wrongVertexSyntax.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(),
                      is("When parsing model: 'graphml/IncorrectModels/wrongVertexSyntax.graphml' The string '1' did not conform to GraphWalker syntax rules."
                         + System.lineSeparator()
                         + System.lineSeparator()
                         + "An error occurred when running command: offline -m graphml/IncorrectModels/wrongVertexSyntax.graphml random(edge_coverage(100))"
                         + System.lineSeparator()
                         + "Model syntax error" + System.lineSeparator() + System.lineSeparator()));
    Assert.assertThat(result.getOutput(), is(""));
  }

  /**
   * missing Start vertex
   */
  @Test
  public void onlyOneVertex() {
    String args[] = {"offline", "-m", "graphml/IncorrectModels/singleVertex.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is("An error occurred when running command: " +
                                            "offline -m graphml/IncorrectModels/singleVertex.graphml random(edge_coverage(100))" +
                                            System.lineSeparator() + "No start context found" + System.lineSeparator() + System.lineSeparator()));
    Assert.assertThat(result.getOutput(), is(""));
  }

  /**
   * single [start] vertex
   */
  @Test
  public void singleStartVertex() {
    String args[] = {"offline", "-m", "graphml/IncorrectModels/singleStartVertex.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(), is("An error occurred when running command: " +
                                            "offline -m graphml/IncorrectModels/singleStartVertex.graphml random(edge_coverage(100))" +
                                            System.lineSeparator() + "No start context found" + System.lineSeparator() + System.lineSeparator()));
    Assert.assertThat(result.getOutput(), is(""));
  }

  /**
   * bad edge name
   */
  @Test
  public void badEdgeName() {
    String args[] = {"offline", "-m", "graphml/IncorrectModels/badEdgeName.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(),
                      is("When parsing model: 'graphml/IncorrectModels/badEdgeName.graphml' The string '1_badName' did not conform to GraphWalker syntax rules."
                         + System.lineSeparator()
                         + System.lineSeparator()
                         + "An error occurred when running command: offline -m graphml/IncorrectModels/badEdgeName.graphml random(edge_coverage(100))"
                         + System.lineSeparator()
                         + "Model syntax error" + System.lineSeparator() + System.lineSeparator()));
    Assert.assertThat(result.getOutput(), is(""));
  }

  /**
   * bad vertex name
   */
  @Test
  public void badVertexName() {
    String args[] = {"offline", "-m", "graphml/IncorrectModels/badVertexName.graphml", "random(edge_coverage(100))"};
    Result result = runCommand(args);
    Assert.assertThat(result.getError(),
                      is("When parsing model: 'graphml/IncorrectModels/badVertexName.graphml' The string '1_badName' did not conform to GraphWalker syntax rules."
                         + System.lineSeparator()
                         + System.lineSeparator()
                         + "An error occurred when running command: offline -m graphml/IncorrectModels/badVertexName.graphml random(edge_coverage(100))"
                         + System.lineSeparator()
                         + "Model syntax error" + System.lineSeparator() + System.lineSeparator()));
    Assert.assertThat(result.getOutput(), is(""));
  }
}
