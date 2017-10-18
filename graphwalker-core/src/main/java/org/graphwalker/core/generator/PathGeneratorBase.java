package org.graphwalker.core.generator;

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

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.common.Objects.isNull;

/**
 * @author Nils Olsson
 */
public abstract class PathGeneratorBase<T extends StopCondition> implements PathGenerator<T> {

  private Context context;
  private T stopCondition;

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
    if (isNotNull(getStopCondition())) {
      getStopCondition().setContext(getContext());
    }
  }

  public T getStopCondition() {
    return stopCondition;
  }

  public void setStopCondition(T stopCondition) {
    this.stopCondition = stopCondition;
    if (isNotNull(getContext())) {
      this.stopCondition.setContext(getContext());
    }
  }

  public Context getNextStep() {
    if (isNull(getContext().getCurrentElement())) {
      throw new NoPathFoundException("Execution context has no current element set");
    }
    return getContext();
  }

  @Override
  public String toString() {
    return toString(new StringBuilder()).toString();
  }

  public StringBuilder toString(StringBuilder builder) {
    return getStopCondition().toString(builder.append(getClass().getSimpleName()).append("(")).append(")");
  }
}
