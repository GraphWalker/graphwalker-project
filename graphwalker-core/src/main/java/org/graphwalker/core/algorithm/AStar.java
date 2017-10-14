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

import java.util.*;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.common.Objects.isNull;

/**
 * <h1>AStar</h1>
 * The AStar implements the <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">A* algorithm</a>.
 * </p>
 * The algorithm calculates the shortest path between two elements.
 * </p>
 *
 * @author Nils Olsson
 */
public class AStar implements Algorithm {

  private final Context context;

  public AStar(Context context) {
    this.context = context;
  }

  public Element getNextElement(Element origin, Element destination) {
    Map<Element, AStarNode> openSet = new HashMap<>();
    PriorityQueue<AStarNode> queue = new PriorityQueue<>(10, new AStarNodeComparator());
    Map<Element, AStarNode> closeSet = new HashMap<>();
    FloydWarshall floydWarshall = context.getAlgorithm(FloydWarshall.class);
    AStarNode sourceNode = new AStarNode(origin, 0, floydWarshall.getShortestDistance(origin, destination));
    openSet.put(origin, sourceNode);
    queue.add(sourceNode);
    AStarNode node = queue.poll();
    if (node == null) {
      throw new AlgorithmException();
    }
    if (node.getElement().equals(destination)) {
      return node.getElement();
    } else {
      closeSet.put(node.getElement(), node);
      List<Element> neighbors = context.filter(context.getModel().getElements(node.getElement()));
      calculate(destination, openSet, queue, closeSet, floydWarshall, node, neighbors);
    }
    if (!queue.isEmpty()) {
      AStarNode result = queue.poll();
      if (null != result) {
        return result.getElement();
      }
    }
    throw new AlgorithmException();
  }

  private void calculate(Element destination, Map<Element, AStarNode> openSet, PriorityQueue<AStarNode> queue, Map<Element, AStarNode> closeSet,
                         FloydWarshall floydWarshall, AStarNode node, List<Element> neighbors) {
    for (Element neighbor : neighbors) {
      AStarNode visited = closeSet.get(neighbor);
      if (isNull(visited)) {
        double g = node.getG() + floydWarshall.getShortestDistance(node.getElement(), neighbor);
        AStarNode neighborNode = openSet.get(neighbor);
        if (isNull(neighborNode)) {
          neighborNode = new AStarNode(neighbor, g, floydWarshall.getShortestDistance(neighbor, destination));
          neighborNode.setParent(node);
          openSet.put(neighbor, neighborNode);
          queue.add(neighborNode);
        } else if (g < neighborNode.getG()) {
          neighborNode.setParent(node);
          neighborNode.setG(g);
          neighborNode.setH(floydWarshall.getShortestDistance(neighbor, destination));
        }
      }
    }
  }

  public Path<Element> getShortestPath(Element origin, Element destination) {
    Map<Element, AStarNode> openSet = new HashMap<>();
    PriorityQueue<AStarNode> queue = new PriorityQueue<>(10, new AStarNodeComparator());
    Map<Element, AStarNode> closeSet = new HashMap<>();
    FloydWarshall floydWarshall = context.getAlgorithm(FloydWarshall.class);
    AStarNode sourceNode = new AStarNode(origin, 0, floydWarshall.getShortestDistance(origin, destination));
    openSet.put(origin, sourceNode);
    queue.add(sourceNode);
    AStarNode targetNode = null;
    while (openSet.size() > 0) {
      AStarNode node = queue.poll();
      if (null != node) {
        openSet.remove(node.getElement());
        if (node.getElement().equals(destination)) {
          targetNode = node;
          break;
        } else {
          closeSet.put(node.getElement(), node);
          List<Element> neighbors = context.filter(context.getModel().getElements(node.getElement()));
          calculate(destination, openSet, queue, closeSet, floydWarshall, node, neighbors);
        }
      }
    }
    if (isNotNull(targetNode)) {
      List<Element> path = new ArrayList<>();
      path.add(targetNode.getElement());
      AStarNode node = targetNode.getParent();
      while (isNotNull(node)) {
        path.add(node.getElement());
        node = node.getParent();
      }
      Collections.reverse(path);
      return new Path<>(path);
    }
    throw new AlgorithmException();
  }

  private class AStarNode {

    private final Element element;
    private AStarNode parent;
    private double g;
    private double h;

    AStarNode(Element element, double g, double h) {
      this.element = element;
      this.g = g;
      this.h = h;
    }

    private Element getElement() {
      return element;
    }

    private AStarNode getParent() {
      return parent;
    }

    private void setParent(AStarNode parent) {
      this.parent = parent;
    }

    private double getG() {
      return g;
    }

    private void setG(double g) {
      this.g = g;
    }

    private void setH(double h) {
      this.h = h;
    }

    public double getF() {
      return g + h;
    }
  }

  private class AStarNodeComparator implements Comparator<AStarNode> {

    public int compare(AStarNode first, AStarNode second) {
      if (first.getF() < second.getF()) {
        return -1;
      } else if (first.getF() > second.getF()) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
