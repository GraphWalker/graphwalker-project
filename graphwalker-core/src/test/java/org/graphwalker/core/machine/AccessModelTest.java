package org.graphwalker.core.machine;

/*-
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import org.graalvm.polyglot.Value;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class AccessModelTest {

  @Test
  public void read() throws Exception {
    ExecutionContext context = createContext();
    assertThat(context.getAttribute("x").asInt(), is(1));
  }

  private int round(Object value) {
    if (value instanceof Double) {
      return (int) Math.round((Double) value);
    } else {
      return (Integer) value;
    }
  }

  @Test
  public void write() throws Exception {
    ExecutionContext context = createContext();
    context.setAttribute("y", Value.asValue(2));
    assertThat(context.getAttribute("y").asInt(), is(2));
  }

  private ExecutionContext createContext() {
    Model model = new Model();
    model.addEdge(new Edge()
      .setSourceVertex(new Vertex())
      .setTargetVertex(new Vertex()));
    ExecutionContext context = new TestExecutionContext(model, new ShortestAllPaths(new VertexCoverage(100)));
    context.execute(new Action("x = 1;"));
    return context;
  }
}
