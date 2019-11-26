package org.graphwalker.core.generator;

/*
 * #%L
 * * GraphWalker Core
 * *
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
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
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.statistics.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.common.Objects.isNull;

/**
 * <h1>QuickRandomPath</h1>
 * The QuickRandomPath generator will generate a random path through a model, but will try to cover
 * the unvisited elements first.
 * </p>
 * QuickRandomPath generates a random path, but tries to reach unvisited elements first.
 * This is quite effective for FSM, but for a EFSM this may not work since edges
 * can be inaccessible.
 * </p>
 *
 * @author Kristian Karl
 */
public class QuickRandomPath extends PathGeneratorBase<StopCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(QuickRandomPath.class);
  private final List<Element> elements = new ArrayList<>();
  private Element target = null;

  public QuickRandomPath(StopCondition stopCondition) {
    setStopCondition(stopCondition);
  }

  @Override
  public Context getNextStep() {
    Context context = super.getNextStep();
    if (elements.isEmpty()) {
      elements.addAll(context.getModel().getElements());
      elements.remove(context.getCurrentElement());
      Collections.shuffle(elements, SingletonRandomGenerator.random());
    }
    if (isNull(target) || target.equals(context.getCurrentElement())) {
      if (elements.isEmpty()) {
        throw new NoPathFoundException(context.getCurrentElement());
      } else {
        orderElementsUnvisitedFirst(elements);
        target = elements.get(0);
        LOG.debug("New selected target is: {} - {}", target.getId(), target.getName());
      }
    }
    Element nextElement = context.getAlgorithm(AStar.class).getNextElement(context.getCurrentElement(), target);
    elements.remove(nextElement);
    return context.setCurrentElement(nextElement);
  }

  private void orderElementsUnvisitedFirst(List<Element> elements) {
    final Context context = getContext();
    final Profiler profiler = context.getProfiler();
    if (isNotNull(profiler)) {
      elements.sort((a, b) -> Boolean.compare(profiler.isVisited(context, a), profiler.isVisited(context, b)));
    }
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }
}

