package org.graphwalker.core.model;

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

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class VisitorTest {

  RuntimeModel model = new Model().addEdge(
      new Edge()
          .setName("edge1")
          .setSourceVertex(new Vertex()
                               .setName("vertex1"))
          .setTargetVertex(new Vertex()
                               .setName("vertex2")))
      .build();

  @Test
  public void visitVertex() throws Exception {
    ElementVisitor visitor = new MyElementVisitor();
    new Vertex().setName("vertex").build().accept(visitor);
  }

  @Test
  public void visitVertices() throws Exception {
    MyNamedVertexCounter visitor = new MyNamedVertexCounter();
    model.accept(visitor);
    assertThat(visitor.count, is(2));
  }

  @Test
  public void visitEdge() throws Exception {
    ElementVisitor visitor = new MyEdgeVisitor();
    new Edge().setName("edge1").setSourceVertex(new Vertex()).setTargetVertex(new Vertex()).build().accept(visitor);
  }

  @Test
  public void visitEdges() throws Exception {
    Vertex startVertex = new Vertex().setName("start");
    Vertex endVertex = new Vertex().setName("end");
    RuntimeModel pseudograph = new Model()
        .addEdge(new Edge().setSourceVertex(startVertex).setTargetVertex(endVertex))
        .addEdge(new Edge().setSourceVertex(endVertex).setTargetVertex(endVertex))
        .build();
    MyLoopEdgeFinder visitor = new MyLoopEdgeFinder();
    pseudograph.accept(visitor);
    assertThat(visitor.count, is(1));
  }

  @Test
  public void detectBridge() throws Exception {
    Vertex start = new Vertex();
    Vertex v1 = new Vertex().setName("V1");
    Vertex v2 = new Vertex().setName("V2");
    Edge e1 = new Edge().setSourceVertex(start).setTargetVertex(v2);
    Model model = new Model().addEdge(e1)
        .addEdge(new Edge().setSourceVertex(start).setTargetVertex(v1));

    MyVertexCounter count1 = new MyVertexCounter(model.build());
    MyVertexCounter count2 = new MyVertexCounter(model.build(), e1.build());

    start.build().accept(count1);
    start.build().accept(count2);

    // if e1 is a bridge then the vertex count will differ between count1 and count2
    assertNotEquals(count1.count, count2.count);

    // if we add a edge between v1 and v2 then e1 is no longer a bridge and count1 and count3 should be the same
    model.addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));
    MyVertexCounter count3 = new MyVertexCounter(model.build(), e1.build());
    start.build().accept(count3);
    assertEquals(count1.count, count3.count);

  }

  private class MyElementVisitor implements ElementVisitor {

    @Override
    public void visit(Element element) {
    }
  }

  private class MyNamedVertexCounter implements ElementVisitor {

    int count = 0;

    @Override
    public void visit(Element element) {
      if (element instanceof RuntimeModel) {
        RuntimeModel model = (RuntimeModel) element;
        // We don't need to visit() all edges to count the vertices with names, it's just PoC (we could just loop over them)
        for (Element childElement : model.getElements()) {
          childElement.accept(this);
        }
      } else if (element instanceof RuntimeVertex && element.hasName()) {
        count++;
      }
    }
  }

  private class MyLoopEdgeFinder implements ElementVisitor {

    int count = 0;

    @Override
    public void visit(Element element) {
      if (element instanceof RuntimeModel) {
        RuntimeModel model = (RuntimeModel) element;
        for (RuntimeEdge edge : model.getEdges()) {
          if (edge.getSourceVertex().equals(edge.getTargetVertex())) {
            count++;
          }
        }
      }
    }
  }

  private class MyEdgeVisitor implements ElementVisitor {

    @Override
    public void visit(Element element) {
    }
  }

  private class MyVertexCounter implements ElementVisitor {

    private RuntimeModel model;
    private int count = 0;
    private Set<Element> visited = new HashSet<>();

    MyVertexCounter(RuntimeModel model) {
      this(model, null);
    }

    MyVertexCounter(RuntimeModel model, RuntimeEdge skipEdge) {
      this.model = model;
      if (null != skipEdge) {
        visited.add(skipEdge);
      }
    }

    @Override
    public void visit(Element element) {
      visited.add(element);
      if (element instanceof RuntimeVertex) {
        visit((RuntimeVertex) element);
      } else if (element instanceof RuntimeEdge) {
        visit((RuntimeEdge) element);
      }
    }

    private void visit(RuntimeVertex vertex) {
      count++;
      for (RuntimeEdge edge : model.getOutEdges(vertex)) {
        if (isNotVisited(edge)) {
          edge.accept(this);
        }
      }
    }

    private void visit(RuntimeEdge edge) {
      if (isNotVisited(edge.getTargetVertex())) {
        edge.getTargetVertex().accept(this);
      }
    }

    private boolean isNotVisited(Element element) {
      return !visited.contains(element);
    }
  }
}
