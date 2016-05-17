package org.graphwalker.core;

import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

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

  private static final RuntimeModel EMPTY_MODEL = new Model().build();

  private static final RuntimeModel SINGLE_MODEL = new Model()
    .addVertex(VERTEX_A)
    .build();

  private static final RuntimeModel SIMPLE_MODEL = new Model()
    .addEdge(EDGE_AB)
    .build();

  public static RuntimeVertex findVertex(RuntimeModel model, String id) {
    return (RuntimeVertex)model.getElementById(id);
  }

  public static RuntimeEdge findEdge(RuntimeModel model, String id) {
    return (RuntimeEdge)model.getElementById(id);
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
}
