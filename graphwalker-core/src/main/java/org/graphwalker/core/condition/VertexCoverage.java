package org.graphwalker.core.condition;

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

import static org.graphwalker.core.model.Vertex.RuntimeVertex;

import java.util.List;
import org.graphwalker.core.machine.Context;

/**
 * <h1>VertexCoverage</h1>
 * The VertexCoverage stop condition is fulfilled when the percentage of visited vertices in the
 * model is greater than, or equal, to the percentage given as a parameter to the constructor.
 * </p>
 *
 * @author Nils Olsson
 */
public final class VertexCoverage extends CoverageStopConditionBase {

  public VertexCoverage(int percent) {
    super(percent);
  }

  @Override
  public boolean isFulfilled() {
    return getFulfilment() >= FULFILLMENT_LEVEL && super.isFulfilled();
  }

  @Override
  public double getFulfilment() {
    Context context = getContext();
    List<RuntimeVertex> vertices = context.getModel().getVertices();
    double visitedVertexCount = 0.0;
    for (RuntimeVertex vertex : vertices) {
      if (context.getProfiler().isVisited(context, vertex)) {
        visitedVertexCount++;
      }
    }
    return (visitedVertexCount / vertices.size()) / getPercentAsDouble();
  }
}
