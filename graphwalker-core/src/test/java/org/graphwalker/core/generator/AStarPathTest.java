package org.graphwalker.core.generator;

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

import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.StopConditionException;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class AStarPathTest {

  private final Vertex v1 = new Vertex().setName("v1");
  private final Vertex v2 = new Vertex().setName("v2");
  private final Vertex v3 = new Vertex().setName("v3");
  private final Vertex v4 = new Vertex().setName("end");
  private final Vertex v5 = new Vertex().setName("v5");

  private final Edge e1 = new Edge().setName("e1").setSourceVertex(v1).setTargetVertex(v2);
  private final Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v3);
  private final Edge e3 = new Edge().setName("e3").setSourceVertex(v3).setTargetVertex(v4);
  private final Edge e4 = new Edge().setName("e4").setSourceVertex(v1).setTargetVertex(v5).addAction(new Action("var closed = 0;"));
  private final Edge e5 = new Edge().setName("e5").setSourceVertex(v5).setTargetVertex(v4).setGuard(new Guard("closed == 1"));
  private final Edge e6 = new Edge().setName("e6").setSourceVertex(v5).setTargetVertex(v1);

  private final Model model = new Model()
      .addEdge(e1)
      .addEdge(e2)
      .addEdge(e3)
      .addEdge(e4)
      .addEdge(e5)
      .addEdge(e6);

  @Test(expected = NoPathFoundException.class)
  public void failTest() throws Exception {
    Context context = new TestExecutionContext(model, new AStarPath(new ReachedVertex("end")));
    context.getPathGenerator().getNextStep();
  }

  @Test(expected = StopConditionException.class)
  public void failTest2() throws Exception {
    Context context = new TestExecutionContext(new Model().addEdge(e1), new AStarPath(new ReachedVertex("end")));
    context.setNextElement(v1);
    while (context.getPathGenerator().hasNextStep()) {
      context.getPathGenerator().getNextStep();
    }
  }

  @Test(expected = NoPathFoundException.class)
  public void noPath() throws Exception {
    Model blockedModel = new Model().addEdge(new Edge()
      .setGuard(new Guard("false"))
      .setSourceVertex(new Vertex().setId("start"))
      .setTargetVertex(new Vertex().setName("end")));
    Context context = new TestExecutionContext(blockedModel, new AStarPath(new ReachedVertex("end")));
    context.setCurrentElement(context.getModel().getElementById("start"));
    context.getPathGenerator().getNextStep();
  }
}
