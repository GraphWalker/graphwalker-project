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

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * <h1>ReachedEdge</h1>
 * The ReachedEdge stop condition is fulfilled when the traversing of the model reaches the named edge.
 * </p>
 *
 * @author Nils Olsson
 */
public final class ReachedEdge extends ReachedStopConditionBase {

  public ReachedEdge(String target) {
    super(target);
  }

  public Set<Element> getTargetElements() {
    Set<Element> elements = new HashSet<>();
    elements.addAll(getContext().getModel().findEdges(getValue()));
    return elements;
  }

  @Override
  public double getFulfilment() {
    Context context = getContext();
    if ( context.getProfiler() == null ) {
      return super.getFulfilment();
    }
    for (Element target : getTargetElements()) {
      if ( context.getProfiler().isVisited(target) ) {
        return 1;
      } else {
        return 0;
      }
    }
    return 0;
  }
}
