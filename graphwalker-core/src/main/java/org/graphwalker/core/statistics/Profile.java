package org.graphwalker.core.statistics;

/*-
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
import org.graphwalker.core.model.Element;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public class Profile {

  private final Context context;
  private final Element element;
  private final List<Execution> executions;

  public Profile(Context context, Element element, List<Execution> executions) {
    this.context = context;
    this.element = element;
    this.executions = executions;
  }

  public Context getContext() {
    return context;
  }

  public Element getElement() {
    return element;
  }

  public long getExecutionCount() {
    return executions.size();
  }

  public long getMinExecutionTime() {
    return getMinExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getMinExecutionTime(TimeUnit unit) {
    return unit.convert(executions.stream()
      .mapToLong(Execution::getDuration).min()
      .orElseThrow(MissingExecutionException::new), TimeUnit.NANOSECONDS);
  }

  public long getMaxExecutionTime() {
    return getMaxExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getMaxExecutionTime(TimeUnit unit) {
    return unit.convert(executions.stream()
      .mapToLong(Execution::getDuration).max()
      .orElseThrow(MissingExecutionException::new), TimeUnit.NANOSECONDS);
  }

  public long getTotalExecutionTime() {
    return getTotalExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getTotalExecutionTime(TimeUnit unit) {
    return unit.convert(executions.stream()
      .mapToLong(Execution::getDuration).sum(), TimeUnit.NANOSECONDS);
  }

  public long getAverageExecutionTime() {
    return getAverageExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getAverageExecutionTime(TimeUnit unit) {
    return unit.convert(Math.round(executions.stream()
      .mapToLong(Execution::getDuration).average()
      .orElseThrow(MissingExecutionException::new)), TimeUnit.NANOSECONDS);
  }

  public long getFirstExecutionTime() {
    return getFirstExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getFirstExecutionTime(TimeUnit unit) {
    return executions.stream().findFirst()
      .orElseThrow(MissingExecutionException::new).getDuration(unit);
  }

  public long getLastExecutionTime() {
    return getLastExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getLastExecutionTime(TimeUnit unit) {
    return executions.stream().reduce((first, second) -> second)
      .orElseThrow(MissingExecutionException::new).getDuration(unit);
  }
}
