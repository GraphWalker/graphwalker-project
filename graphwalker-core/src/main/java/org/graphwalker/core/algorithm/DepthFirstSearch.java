package org.graphwalker.core.algorithm;

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

import java.util.*;

import static org.graphwalker.core.common.Objects.unmodifiableList;
import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public class DepthFirstSearch implements Algorithm {

  private final Context context;

  public DepthFirstSearch(Context context) {
    this.context = context;
  }

  public List<Element> getConnectedComponent(Element root) {
    return createConnectedComponent(createElementStatusMap(context.getModel().getElements()), root);
  }

  private Map<Element, ElementStatus> createElementStatusMap(List<Element> elements) {
    Map<Element, ElementStatus> elementStatusMap = new HashMap<>();
    for (Element element : elements) {
      elementStatusMap.put(element, ElementStatus.UNREACHABLE);
    }
    return elementStatusMap;
  }

  private List<Element> createConnectedComponent(Map<Element, ElementStatus> elementStatusMap, Element root) {
    List<Element> connectedComponent = new ArrayList<>();
    Deque<Element> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      Element element = stack.pop();
      if (ElementStatus.UNREACHABLE.equals(elementStatusMap.get(element))) {
        connectedComponent.add(element);
        elementStatusMap.put(element, ElementStatus.REACHABLE);
        if (element instanceof RuntimeVertex) {
          RuntimeVertex vertex = (RuntimeVertex) element;
          for (RuntimeEdge edge : context.getModel().getOutEdges(vertex)) {
            stack.push(edge);
          }
        } else if (element instanceof RuntimeEdge) {
          RuntimeEdge edge = (RuntimeEdge) element;
          stack.push(edge.getTargetVertex());
        }
      }
    }
    return unmodifiableList(connectedComponent);
  }

  private enum ElementStatus {
    UNREACHABLE, REACHABLE
  }
}
