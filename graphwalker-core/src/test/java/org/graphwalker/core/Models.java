package org.graphwalker.core;

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

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;

public abstract class Models {

  private static final Vertex VERTEX_A = new Vertex()
      .setId("A")
      .setName("A");

  private static final Vertex VERTEX_B = new Vertex()
      .setId("B")
      .setName("B");

  private static final Edge EDGE_AB = new Edge()
    .setId("ab")
    .setName("ab")
    .setSourceVertex(VERTEX_A)
    .setTargetVertex(VERTEX_B);

  private static final Edge EDGE_AB_2 = new Edge()
    .setId("ab_2")
    .setName("ab_2")
    .setSourceVertex(VERTEX_A)
    .setTargetVertex(VERTEX_B);

  private static final Edge EDGE_AB_3 = new Edge()
    .setId("ab_3")
    .setName("ab_3")
    .setSourceVertex(VERTEX_A)
    .setTargetVertex(VERTEX_B);

  private static final Edge EDGE_BA = new Edge()
    .setId("ba")
    .setName("ba")
    .setSourceVertex(VERTEX_B)
    .setTargetVertex(VERTEX_A);

  private static final RuntimeModel EMPTY_MODEL = new Model().build();

  private static final RuntimeModel SINGLE_MODEL = new Model()
      .addVertex(VERTEX_A)
      .build();

  private static final RuntimeModel SIMPLE_MODEL = new Model()
    .addEdge(EDGE_AB)
    .build();

  private static final RuntimeModel FOUR_EDGES_MODEL = new Model()
    .addEdge(EDGE_AB)
    .addEdge(EDGE_AB_2)
    .addEdge(EDGE_AB_3)
    .addEdge(EDGE_BA)
    .build();

  public static RuntimeVertex findVertex(RuntimeModel model, String id) {
    return (RuntimeVertex) model.getElementById(id);
  }

  public static RuntimeEdge findEdge(RuntimeModel model, String id) {
    return (RuntimeEdge) model.getElementById(id);
  }

  public static Model emptyModel() {
    return new Model(EMPTY_MODEL);
  }

  public static Model singleModel() {
    return new Model(SINGLE_MODEL);
  }

  public static Model simpleModel() {
    return new Model(SIMPLE_MODEL);
  }

  public static Model fourEdgesModel() {
    return new Model(FOUR_EDGES_MODEL);
  }
}
