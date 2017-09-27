package org.graphwalker.core.algorithm;

/*
 * #%L
 * GraphWalker Core
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
import static org.junit.Assert.assertThat;

import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class EulerianTest {

  private final Vertex A = new Vertex().setName("A");
  private final Vertex B = new Vertex().setName("B");
  private final Vertex C = new Vertex().setName("C");

  private Model createModel() {
    return new Model()
        .addEdge(new Edge().setSourceVertex(A).setTargetVertex(B))
        .addEdge(new Edge().setSourceVertex(B).setTargetVertex(C))
        .addEdge(new Edge().setSourceVertex(C).setTargetVertex(A))
        .addEdge(new Edge().setSourceVertex(B).setTargetVertex(A));
  }

  @Test
  public void verifyNotEulerian() throws Exception {
    Model model = createModel().addEdge(new Edge().setSourceVertex(C).setTargetVertex(A));
    Eulerian eulerian = new Eulerian(new TestExecutionContext().setModel(model.build()));
    assertThat(eulerian.getEulerianType(), is(Eulerian.EulerianType.NOT_EULERIAN));
  }

  @Test(expected = AlgorithmException.class)
  public void verifyNotEulerianPathFromVertex() throws Exception {
    Model model = createModel().addEdge(new Edge().setSourceVertex(C).setTargetVertex(A));
    Eulerian eulerian = new Eulerian(new TestExecutionContext().setModel(model.build()));
    assertThat(eulerian.getEulerianType(), is(Eulerian.EulerianType.NOT_EULERIAN));
    eulerian.getEulerPath(A.build());
  }

  @Test(expected = AlgorithmException.class)
  public void verifyNotEulerianPathFromEdge() throws Exception {
    Model model = createModel().addEdge(new Edge().setSourceVertex(C).setTargetVertex(A));
    Eulerian eulerian = new Eulerian(new TestExecutionContext().setModel(model.build()));
    assertThat(eulerian.getEulerianType(), is(Eulerian.EulerianType.NOT_EULERIAN));
    eulerian.getEulerPath(model.getEdges().get(0).build());
  }

  @Test
  public void verifySemiEulerian() throws Exception {
    Eulerian eulerian = new Eulerian(new TestExecutionContext().setModel(createModel().build()));
    assertThat(eulerian.getEulerianType(), is(Eulerian.EulerianType.SEMI_EULERIAN));
  }

  @Test
  public void verifyEulerian() throws Exception {
    Model model = createModel().addEdge(new Edge().setSourceVertex(A).setTargetVertex(B));
    Eulerian eulerian = new Eulerian(new TestExecutionContext().setModel(model.build()));
    assertThat(eulerian.getEulerianType(), is(Eulerian.EulerianType.EULERIAN));
  }
}
