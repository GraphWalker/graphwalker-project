package org.graphwalker.core.generator;

/*
* #%L
 * * GraphWalker Core
 * *
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
 * *
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
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Kristian Karl
 *         <p/>
 *         Generates a random path, but tries to reach unvisited edges first
 */
public final class QuickRandomPath implements PathGenerator {

  private final Random random = new Random(System.nanoTime());
  private final StopCondition stopCondition;
  private Path<Element> littleWalk = null;
  private ArrayList<Element> unvisitedElements = null;

  public QuickRandomPath(StopCondition stopCondition) {
    this.stopCondition = stopCondition;
  }

  public StopCondition getStopCondition() {
    return stopCondition;
  }

  @Override
  public ExecutionContext getNextStep(ExecutionContext context) {
    if (unvisitedElements==null) {
      unvisitedElements = new ArrayList<>();
      unvisitedElements.addAll(context.getModel().getElements());
      unvisitedElements.remove(context.getCurrentElement());
    }
    if (littleWalk==null || littleWalk.isEmpty()) {
      Element e = unvisitedElements.get(random.nextInt(unvisitedElements.size()));
      littleWalk = findShortestPath(context, e);
    }

    if (littleWalk.isEmpty()) {
      throw new NoPathFoundException();
    }

    context.setCurrentElement(littleWalk.pop());
    unvisitedElements.remove(context.getCurrentElement());
    return context;
  }

  @Override
  public boolean hasNextStep(ExecutionContext context) {
    return !getStopCondition().isFulfilled(context);
  }

  private Path<Element> findShortestPath(ExecutionContext context, Element target) {
    AStar astar = context.getAlgorithm(AStar.class);
    Path<Element> path = astar.getShortestPath(context.getCurrentElement(), target);
    path.pop();
    return path;
  }
}

