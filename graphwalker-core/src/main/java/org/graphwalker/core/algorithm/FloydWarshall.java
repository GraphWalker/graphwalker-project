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

import java.util.Arrays;
import java.util.List;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public class FloydWarshall implements Algorithm {

  private final RuntimeModel model;
  private final int[][] distances;

  public FloydWarshall(Context context) {
    this.model = context.getModel();
    this.distances = createDistanceMatrix(model, model.getElements());
    createPredecessorMatrix(model.getElements(), distances);
  }

  private int[][] createDistanceMatrix(RuntimeModel model, List<Element> elements) {
    int[][] distances = new int[elements.size()][elements.size()];
    for (int[] row : distances) {
      Arrays.fill(row, Integer.MAX_VALUE);
    }
    for (Element element : elements) {
      if (element instanceof RuntimeEdge) {
        RuntimeEdge edge = (RuntimeEdge) element;
        RuntimeVertex target = edge.getTargetVertex();
        distances[elements.indexOf(edge)][elements.indexOf(target)] = 1;
      } else if (element instanceof RuntimeVertex) {
        RuntimeVertex vertex = (RuntimeVertex) element;
        for (RuntimeEdge edge : model.getOutEdges(vertex)) {
          distances[elements.indexOf(vertex)][elements.indexOf(edge)] = 1;
        }
      }
    }
    return distances;
  }

  private Element[][] createPredecessorMatrix(List<Element> elements, int[][] distances) {
    Element[][] predecessors = new Element[elements.size()][elements.size()];
    int size = elements.size();
    for (int k = 0; k < size; k++) {
      for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
          if (distances[i][k] != Integer.MAX_VALUE
              && distances[k][j] != Integer.MAX_VALUE
              && distances[i][k] + distances[k][j] < distances[i][j]) {
            distances[i][j] = distances[i][k] + distances[k][j];
            predecessors[i][j] = elements.get(k);
          }
        }
      }
    }
    return predecessors;
  }

  public int getShortestDistance(Element origin, Element destination) {
    if (!model.getElements().contains(destination)) {
      return Integer.MAX_VALUE;
    } else if (origin.equals(destination)) {
      return 0;
    } else {
      return distances[model.getElements().indexOf(origin)][model.getElements().indexOf(destination)];
    }
  }

  public int getMaximumDistance(Element destination) {
    int maximumDistance = Integer.MIN_VALUE;
    for (int[] distance : distances) {
      int value = distance[model.getElements().indexOf(destination)];
      if (value != Integer.MAX_VALUE && value > maximumDistance) {
        maximumDistance = value;
      }
    }
    return maximumDistance;
  }
}
