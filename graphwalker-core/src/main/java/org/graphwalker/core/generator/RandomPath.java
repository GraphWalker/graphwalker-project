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

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <h1>RandomPath</h1>
 * The RandomPath generator will generate a random path through a model.
 * </p>
 * RandomPath is used when a random, unpredictable path  is to be generated.
 * </p>
 *
 * @author Nils Olsson
 */
public class RandomPath extends PathGeneratorBase<StopCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(RandomPath.class);

  public RandomPath(StopCondition stopCondition) {
    setStopCondition(stopCondition);
  }

  @Override
  public Context getNextStep() {
    Context context = super.getNextStep();
    Element currentElement = context.getCurrentElement();
    List<Element> elements = context.filter(context.getModel().getElements(currentElement));
    if (elements.isEmpty()) {
      LOG.error("currentElement: " + currentElement);
      LOG.error("context.getModel().getElements(): " + context.getModel().getElements());
      throw new NoPathFoundException(context.getCurrentElement());
    }
    context.setCurrentElement(elements.get(SingletonRandomGenerator.nextInt(elements.size())));
    return context;
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }

}

