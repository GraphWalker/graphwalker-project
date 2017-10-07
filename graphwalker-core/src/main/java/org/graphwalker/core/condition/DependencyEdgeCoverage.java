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

import static org.graphwalker.core.model.Edge.RuntimeEdge;

import org.graphwalker.core.machine.Context;

/**
 * <h1>DependencyEdgeCoverage</h1> The DependencyEdgeCoverage stop condition is fulfilled
 * when all the edges with dependency greater than, or equal,
 * to the dependency given as a parameter to the constructor, are visited.
 * </p>
 *
 * @author Miroslav Janeski
 */
public final class DependencyEdgeCoverage extends DependencyCoverageStopConditionBase {

  public DependencyEdgeCoverage(int dependency) {
    super(dependency);
  }

  @Override
  public boolean isFulfilled() {
    return getFulfilment() >= FULFILLMENT_LEVEL && super.isFulfilled();
  }

  @Override
  public double getFulfilment() {
    Context context = getContext();
    long totalDependencyEdgesCount = 0;
    long visitedDependencyEdgesCount = 0;
    for (RuntimeEdge edge : context.getModel().getEdges()) {
      if (edge.getDependencyAsDouble() >= super.getDependencyAsDouble()) {
        totalDependencyEdgesCount++;
        if (context.getProfiler().isVisited(context, edge)) {
          visitedDependencyEdgesCount++;
        }
      }
    }

    if (totalDependencyEdgesCount == 0) {
      return totalDependencyEdgesCount;
    } else {
      return ((double) visitedDependencyEdgesCount / totalDependencyEdgesCount);
    }
  }
}
