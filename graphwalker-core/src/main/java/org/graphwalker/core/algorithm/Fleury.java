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
import org.graphwalker.core.model.ElementVisitor;
import org.graphwalker.core.model.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public class Fleury implements Algorithm {

  private final Context context;

  public Fleury(Context context) {
    this.context = context;
  }

  public Path<Element> getTrail(Element element) {
    if (element instanceof RuntimeEdge) {
      RuntimeEdge edge = (RuntimeEdge)element;
      Set<RuntimeEdge> visitedEdges = new HashSet<>();
      visitedEdges.add(edge);
      Path<Element> path = getTrail(edge.getTargetVertex(), visitedEdges);
      path.addFirst(edge.getTargetVertex());
      return path;
    } else {
      return getTrail((RuntimeVertex)element, new HashSet<>());
    }
  }

  private Path<Element> getTrail(RuntimeVertex vertex, Set<RuntimeEdge> visitedEdges) {
    // Step 1
    Path<Element> trail = new Path<>();
    RuntimeVertex currentVertex = vertex;
    List<RuntimeEdge> availableEdges = new ArrayList<>(context.getModel().getEdges());
    availableEdges.removeAll(visitedEdges);
    while (!availableEdges.isEmpty()) {
      // Step 2
      RuntimeEdge edge = getNextEdge(context.getModel(), visitedEdges, currentVertex);
      // Step 3
      trail.add(edge);
      visitedEdges.add(edge);
      currentVertex = edge.getTargetVertex();
      trail.add(currentVertex);
      // Step 4
      availableEdges.remove(edge);
    }
    return trail;
  }

  private RuntimeEdge getNextEdge(RuntimeModel model, Set<RuntimeEdge> visitedEdges, RuntimeVertex vertex) {
    List<RuntimeEdge> bridges = new ArrayList<>();
    for (RuntimeEdge edge : model.getOutEdges(vertex)) {
      if (!visitedEdges.contains(edge)) {
        if (!isBridge(model, visitedEdges, edge)) {
          return edge;
        } else {
          bridges.add(edge);
        }
      }
    }
    if (!bridges.isEmpty()) {
      return bridges.get(0);
    }
    throw new AlgorithmException();
  }

  private boolean isBridge(RuntimeModel model, Set<RuntimeEdge> visitedElements, RuntimeEdge edge) {
    VertexCounter counter1 = new VertexCounter(model, visitedElements, edge);
    VertexCounter counter2 = new VertexCounter(model);
    edge.getSourceVertex().accept(counter1);
    edge.getSourceVertex().accept(counter2);
    return counter1.getCount() < counter2.getCount();
  }

  private static class VertexCounter implements ElementVisitor {

    private final RuntimeModel model;
    private final Set<Element> visitedElements = new HashSet<>();
    private int count = 0;

    public VertexCounter(RuntimeModel model) {
      this.model = model;
    }

    public VertexCounter(RuntimeModel model, Set<RuntimeEdge> visitedElements, RuntimeEdge edge) {
      this.model = model;
      this.visitedElements.addAll(visitedElements);
      this.visitedElements.add(edge);
    }

    public int getCount() {
      return count;
    }

    private boolean isNotVisited(Element element) {
      return !visitedElements.contains(element);
    }

    @Override
    public void visit(Element element) {
      visitedElements.add(element);
      if (element instanceof RuntimeVertex) {
        RuntimeVertex vertex = (RuntimeVertex) element;
        count++;
        for (RuntimeEdge edge : model.getOutEdges(vertex)) {
          if (isNotVisited(edge)) {
            edge.accept(this);
          }
        }
      } else if (element instanceof RuntimeEdge) {
        RuntimeEdge edge = (RuntimeEdge) element;
        if (isNotVisited(edge.getTargetVertex())) {
          edge.getTargetVertex().accept(this);
        }
      }
    }
  }
}
