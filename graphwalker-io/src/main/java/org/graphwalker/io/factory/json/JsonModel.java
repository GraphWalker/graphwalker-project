package org.graphwalker.io.factory.json;

/*
 * #%L
 * GraphWalker Input/Output
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;

/**
 * @author Nils Olsson
 */
public class JsonModel {

  private String name;
  private String id;
  private String generator;
  private String startElementId;
  private List<String> actions;
  private List<String> requirements;
  private Map<String, Object> properties;
  private List<JsonVertex> vertices;
  private List<JsonEdge> edges;

  public String getId() {
    return id;
  }

  public List<String> getActions() {
    return actions;
  }

  public void setActions(List<String> actions) {
    this.actions = actions;
  }

  public List<String> getRequirements() {
    return requirements;
  }

  public void setRequirements(List<String> requirements) {
    this.requirements = requirements;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStartElementId() {
    return startElementId;
  }

  public void setStartElementId(String startElementId) {
    this.startElementId = startElementId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGenerator() {
    return generator;
  }

  public void setGenerator(String generator) {
    this.generator = generator;
  }

  public List<JsonVertex> getVertices() {
    return vertices;
  }

  public void setVertices(List<JsonVertex> vertices) {
    this.vertices = vertices;
  }

  public List<JsonEdge> getEdges() {
    return edges;
  }

  public void setEdges(List<JsonEdge> edges) {
    this.edges = edges;
  }

  public boolean isModel() {
    return !(null == name || null == generator || null == edges || null == vertices);
  }

  public Model getModel() {
    Model model = new Model();
    model.setName(name);
    model.setId(id);

    if (properties != null) {
      model.setProperties(properties);
    }

    if (actions != null) {
      for (String action : actions) {
        model.addAction(new Action(action));
      }
    }

    if (vertices != null) {
      for (JsonVertex jsonVertex : getVertices()) {
        model.addVertex(jsonVertex.getVertex());
      }
    }

    if (edges != null) {
      for (JsonEdge jsonEdge : getEdges()) {
        Edge edge = jsonEdge.getEdge();
        for (Vertex vertex : model.getVertices()) {
          if (vertex.getId().equals(jsonEdge.getSourceVertexId())) {
            edge.setSourceVertex(vertex);
          }
          if (vertex.getId().equals(jsonEdge.getTargetVertexId())) {
            edge.setTargetVertex(vertex);
          }
        }
        model.addEdge(edge);
      }
    }
    return model;
  }

  public void setModel(Model.RuntimeModel model) {
    name = model.getName();
    id = model.getId();

    if (model.hasActions()) {
      actions = new ArrayList<>();
      for (Action action : model.getActions()) {
        actions.add(action.getScript());
      }
    }

    if (model.hasRequirements()) {
      requirements = new ArrayList<>();
      for (Requirement requirement : model.getRequirements()) {
        requirements.add(requirement.getKey());
      }
    }

    if (model.hasProperties()) {
      properties = new HashMap<>();
      properties.putAll(model.getProperties());
    }

    vertices = new ArrayList<>();
    for (Vertex.RuntimeVertex vertex : model.getVertices()) {
      JsonVertex jsonVertex = new JsonVertex();
      jsonVertex.setVertex(vertex);
      vertices.add(jsonVertex);
    }

    edges = new ArrayList<>();
    for (Edge.RuntimeEdge edge : model.getEdges()) {
      JsonEdge jsonEdge = new JsonEdge();
      jsonEdge.setEdge(edge);
      edges.add(jsonEdge);
    }
  }

  public void setModel(Model model) {
    setModel(model.build());
  }

  public void copyValuesTo(Model model) {
    if (name != null) {
      model.setName(name);
    }
    if (id != null) {
      model.setId(id);
    }

    if (actions != null && !actions.isEmpty()) {
      actions = new ArrayList<>();
      for (String action : actions) {
        model.addAction(new Action(action));
      }
    }

    if (requirements != null && !requirements.isEmpty()) {
      requirements = new ArrayList<>();
      for (String requirement : requirements) {
        model.addRequirement(new Requirement(requirement));
      }
    }

    if (properties != null && !properties.isEmpty()) {
      model.getProperties().putAll(properties);
    }
  }
}
