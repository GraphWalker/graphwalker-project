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
import org.graphwalker.core.model.Path;

import java.util.HashMap;
import java.util.Map;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public class Eulerian implements Algorithm {

  private final Context context;
  private final Map<RuntimeVertex, PolarityCounter> polarities;

  public Eulerian(Context context) {
    this.context = context;
    this.polarities = new HashMap<>(context.getModel().getVertices().size());
    polarize();
  }

  public enum EulerianType {
    EULERIAN, SEMI_EULERIAN, NOT_EULERIAN
  }

  private void polarize() {
    for (RuntimeEdge edge : context.getModel().getEdges()) {
      getPolarityCounter(edge.getSourceVertex()).decrease();
      getPolarityCounter(edge.getTargetVertex()).increase();
    }
    for (RuntimeVertex vertex : context.getModel().getVertices()) {
      if (!polarities.get(vertex).hasPolarity()) {
        polarities.remove(vertex);
      }
    }
  }

  private PolarityCounter getPolarityCounter(RuntimeVertex vertex) {
    if (!polarities.containsKey(vertex)) {
      polarities.put(vertex, new PolarityCounter());
    }
    return polarities.get(vertex);
  }

  public EulerianType getEulerianType() {
    if (polarities.isEmpty()) {
      return EulerianType.EULERIAN;
    }
    if (2 == polarities.size()) {
      return EulerianType.SEMI_EULERIAN;
    }
    return EulerianType.NOT_EULERIAN;
  }

  public Path<Element> getEulerPath(Element element) {
    if (EulerianType.NOT_EULERIAN.equals(getEulerianType())) {
      throw new AlgorithmException("The model is not eulerian or semi eulerian, no single path can cover the entire graph");
    }
    return context.getAlgorithm(Fleury.class).getTrail(element);
  }

  class PolarityCounter {

    private int polarity = 0;

    public void increase() {
      polarity += 1;
    }

    public void decrease() {
      polarity -= 1;
    }

    public boolean hasPolarity() {
      return 0 != getPolarity();
    }

    public int getPolarity() {
      return polarity;
    }
  }
}
