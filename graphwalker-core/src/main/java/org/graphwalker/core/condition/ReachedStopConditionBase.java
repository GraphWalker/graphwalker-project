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

import org.graphwalker.core.algorithm.FloydWarshall;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.common.Objects.isNull;

/**
 * @author Nils Olsson
 */
public abstract class ReachedStopConditionBase extends StopConditionBase implements ReachedStopCondition {

  private boolean fulfilled = false;

  protected ReachedStopConditionBase(String value) {
    super(value);
  }

  @Override
  public void setContext(Context context) {
    super.setContext(context);
    validate(context);
  }

  protected void validate(Context context) {
    if (isNotNull(context) && isNull(context.getModel())) {
      throw new StopConditionException("Context missing a model");
    }
  }

  @Override
  public boolean isFulfilled() {
    if (fulfilled) {
      return true;
    }
    return fulfilled = getFulfilment() >= FULFILLMENT_LEVEL;
  }

  @Override
  public double getFulfilment() {
    Context context = getContext();
    double maxFulfilment = 0;
    if (isNotNull(context.getCurrentElement())) {
      FloydWarshall floydWarshall = context.getAlgorithm(FloydWarshall.class);
      for (Element target : getTargetElements()) {
        int distance = floydWarshall.getShortestDistance(context.getCurrentElement(), target);
        int max = floydWarshall.getMaximumDistance(target);
        double fulfilment = 1 - (double) distance / max;
        if (maxFulfilment < fulfilment) {
          maxFulfilment = fulfilment;
        }
      }
    }
    return maxFulfilment;
  }
}
