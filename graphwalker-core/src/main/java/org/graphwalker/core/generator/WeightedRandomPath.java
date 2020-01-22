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
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>WeightedRandomPath</h1>
 * The WeightedRandomPath generator will generate a random path through a model, but will select
 * out-edges according to probabilities.
 * </p>
 * WeightedRandomPath generates a random path, but will take into account {@link Edge#setWeight} as
 * a probability. When selecting an edge from the list of all out-edges from a current vertex, the weights
 * of those edges are considered. For example, an edge with the weight 0.5 is given 50% chance of being selected.
 * </p>
 *
 * @author Kristian Karl
 */
public class WeightedRandomPath extends PathGeneratorBase<StopCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(WeightedRandomPath.class);

  public WeightedRandomPath(StopCondition stopCondition) {
    setStopCondition(stopCondition);
  }

  @Override
  public Context getNextStep() {
    Context context = super.getNextStep();
    Element currentElement = context.getCurrentElement();
    List<Element> elements = context.filter(context.getModel().getElements(currentElement));
    if (elements.isEmpty()) {
      throw new NoPathFoundException(context.getCurrentElement());
    }
    if (currentElement instanceof Vertex.RuntimeVertex) {
      context.setCurrentElement(getWeightedEdge(elements, currentElement));
    } else {
      context.setCurrentElement(elements.get(SingletonRandomGenerator.nextInt(elements.size())));
    }
    return context;
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }

  private Element getWeightedEdge(List<Element> elements, Element currentElement) {

    Map<Edge.RuntimeEdge, Double> probabilities = new HashMap<>();
    int numberOfZeros = 0;
    double sum = 0;

    for (Element element : elements) {
      if (element instanceof Edge.RuntimeEdge) {
        Edge.RuntimeEdge edge = (Edge.RuntimeEdge) element;
        if (edge.getWeight() > 0) {
          probabilities.put(edge, edge.getWeight());
          sum += edge.getWeight();
          if (sum > 1) {
            throw new MachineException("The sum of all weights in edges from vertex: '"
              + currentElement.getName()
              + "', adds up to more than 1.00");
          }
        } else {
          numberOfZeros++;
          probabilities.put(edge, 0d);
        }
      }
    }

    double rest;
    if (numberOfZeros > 0) {
      rest = (1 - sum) / numberOfZeros;
    } else {
      rest = 1 - sum;
    }
    int index = SingletonRandomGenerator.nextInt(100);
    double weight = 0;
    for (Element element : elements) {
      if (element instanceof Edge.RuntimeEdge) {
        Edge.RuntimeEdge edge = (Edge.RuntimeEdge) element;

        if (probabilities.get(edge) == 0) {
          probabilities.put(edge, rest);
        }

        weight = weight + probabilities.get(edge) * 100;
        if (index < weight) {
          return edge;
        }
      }
    }

    if (elements.size() == 1 && elements.get(0) instanceof Edge.RuntimeEdge) {
      return (Edge.RuntimeEdge) elements.get(0);
    }

    throw new MachineException("Could not calculate which weighted edge to choose from vertex: "
      + currentElement.getName()
      + "'");
  }
}

