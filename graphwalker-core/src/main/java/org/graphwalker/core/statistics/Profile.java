package org.graphwalker.core.statistics;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;

/**
 * @author Nils Olsson
 */
public final class Profile extends HashMap<Element, ProfileUnit> {

  private final Path<Element> path = new Path<>();
  private final Set<Element> visited = new HashSet<>();

  public void reset() {
    visited.clear();
  }

  public boolean isVisited(Element element) {
    return visited.contains(element);
  }

  public void addExecution(Element element, Execution execution) {
    path.push(element);
    visited.add(element);
    if (!containsKey(element)) {
      put(element, new ProfileUnit(execution));
    } else {
      get(element).addExecution(execution);
    }
  }

  public Path<Element> getPath() {
    return path;
  }

  public long getTotalExecutionCount() {
    return getTotalExecutionCount(Element.class);
  }

  public long getTotalExecutionCount(Class<? extends Element> type) {
    long count = 0;
    for (Element element : keySet()) {
      if (type.isAssignableFrom(element.getClass())) {
        count += get(element).getExecutionCount();
      }
    }
    return count;
  }

  public long getTotalExecutionCount(Element element) {
    long count = 0;
    for (Element e : keySet()) {
      if (element == e) {
        count += get(e).getExecutionCount();
      }
    }
    return count;
  }

  public long getTotalExecutionTime() {
    return getTotalExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getTotalExecutionTime(TimeUnit unit) {
    return getTotalExecutionTime(Element.class, unit);
  }

  public long getTotalExecutionTime(Class<?> type, TimeUnit unit) {
    long executionTime = 0;
    for (Element element : keySet()) {
      if (type.isAssignableFrom(element.getClass())) {
        executionTime += get(element).getTotalExecutionTime();
      }
    }
    return unit.convert(executionTime, TimeUnit.NANOSECONDS);
  }

  public long getFirstExecutionTime() {
    return getFirstExecutionTime(Element.class);
  }

  public long getFirstExecutionTime(TimeUnit unit) {
    return getFirstExecutionTime(Element.class, unit);
  }

  public long getFirstExecutionTime(Class<? extends Element> type) {
    return getFirstExecutionTime(type, TimeUnit.NANOSECONDS);
  }

  public long getFirstExecutionTime(Class<? extends Element> type, TimeUnit unit) {
    long time = Long.MAX_VALUE;
    for (Element element : keySet()) {
      if (type.isAssignableFrom(element.getClass())) {
        long firstExecutionTime = get(element).getFirstExecutionTime();
        if (time > firstExecutionTime) {
          time = firstExecutionTime;
        }
      }
    }
    return unit.convert(time, TimeUnit.NANOSECONDS);
  }

  public long getLastExecutionTime() {
    return getLastExecutionTime(Element.class);
  }

  public long getLastExecutionTime(Class<? extends Element> type) {
    return getLastExecutionTime(type, TimeUnit.NANOSECONDS);
  }

  public long getLastExecutionTime(Class<? extends Element> type, TimeUnit unit) {
    long time = Long.MIN_VALUE;
    for (Element element : keySet()) {
      if (type.isAssignableFrom(element.getClass())) {
        long lastExecutionTime = get(element).getLastExecutionTime();
        if (time < lastExecutionTime) {
          time = lastExecutionTime;
        }
      }
    }
    return unit.convert(time, TimeUnit.NANOSECONDS);
  }
}
