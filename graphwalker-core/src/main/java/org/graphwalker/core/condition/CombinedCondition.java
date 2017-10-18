package org.graphwalker.core.condition;

/*
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

import org.graphwalker.core.machine.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>CombinedCondition</h1>
 * The CombinedCondition evaluates to fulfilled if all of its conditions are fulfilled.
 * </p>
 * The CombinedCondition holds a list of stop conditions. Only if all its conditions in its
 * list is fulfilled, the CombinedCondition is fulfilled.
 * </p>
 *
 * @author Nils Olsson
 */
public class CombinedCondition extends StopConditionBase {

  private final List<StopCondition> conditions = new ArrayList<>();

  public CombinedCondition() {
    super("");
  }

  public void addStopCondition(StopCondition condition) {
    this.conditions.add(condition);
    condition.setContext(getContext());
  }


  @Override
  public void setContext(Context context) {
    super.setContext(context);
    for (StopCondition condition : conditions) {
      condition.setContext(context);
    }
  }

  public List<StopCondition> getStopConditions() {
    return conditions;
  }

  @Override
  public boolean isFulfilled() {
    for (StopCondition condition : conditions) {
      if (!condition.isFulfilled()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public double getFulfilment() {
    double fulfilment = 0;
    for (StopCondition condition : conditions) {
      fulfilment += condition.getFulfilment();
    }
    return fulfilment / conditions.size();
  }

  @Override
  public StringBuilder toString(StringBuilder builder) {
    return builder.append(conditions.stream().map(StopCondition::toString)
      .collect(Collectors.joining(" AND ")));
  }
}
