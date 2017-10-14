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

import org.graphwalker.core.algorithm.AStar;
import org.graphwalker.core.algorithm.FloydWarshall;
import org.graphwalker.core.condition.ReachedStopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import java.util.List;

/**
 * <h1>AStarPath</h1>
 * The AStarPath generator will find the shortest path between 2 elements.
 * </p>
 * Given the current element in the execution context and the {@link ReachedStopCondition}
 * the generator will create the shortest path between the 2 elements.
 * </p>
 *
 * @author Nils Olsson
 */
public class AStarPath extends PathGeneratorBase<ReachedStopCondition> {

  public AStarPath(ReachedStopCondition stopCondition) {
    setStopCondition(stopCondition);
  }

  @Override
  public Context getNextStep() {
    Context context = super.getNextStep();
    List<Element> elements = context.filter(context.getModel().getElements(context.getCurrentElement()));
    if (elements.isEmpty()) {
      throw new NoPathFoundException(context.getCurrentElement());
    }
    Element target = null;
    int distance = Integer.MAX_VALUE;
    FloydWarshall floydWarshall = context.getAlgorithm(FloydWarshall.class);
    for (Element element : context.filter(getStopCondition().getTargetElements())) {
      int edgeDistance = floydWarshall.getShortestDistance(context.getCurrentElement(), element);
      if (edgeDistance < distance) {
        distance = edgeDistance;
        target = element;
      }
    }
    AStar astar = context.getAlgorithm(AStar.class);
    return context.setCurrentElement(astar.getNextElement(context.getCurrentElement(), target));
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }
}
