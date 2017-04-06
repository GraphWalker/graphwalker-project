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
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Requirement;

/**
 * @author Nils Olsson
 */
public class JsonEdge {

  private String id;
  private String name;
  private String guard;
  private List<String> actions;
  private List<String> requirements;
  private Map<String, Object> properties;
  private Double weight;
  private Integer dependency;
  private String sourceVertexId;
  private String targetVertexId;

  public String getSourceVertexId() {
    return sourceVertexId;
  }

  public String getTargetVertexId() {
    return targetVertexId;
  }

  public Edge getEdge() {
    Edge edge = new Edge();
    edge.setId(id);
    edge.setName(name);

    if (weight != null) {
      edge.setWeight(weight);
    } else {
      edge.setWeight(0.);
    }
    if (dependency != null) {
      edge.setDependency(dependency);
    } else {
      edge.setDependency(0);
    }
    edge.setGuard(new Guard(guard));

    if (actions != null) {
      for (String action : actions) {
        edge.addAction(new Action(action));
      }
    }

    if (requirements != null) {
      for (String requirement : requirements) {
        edge.addRequirement(new Requirement(requirement));
      }
    }

    if (properties != null) {
      edge.setProperties(properties);
    }

    return edge;
  }

  public void setEdge(Edge.RuntimeEdge edge) {
    id = edge.getId();
    name = edge.getName();
    weight = edge.getWeight();
    dependency = edge.getDependency();
    if (edge.hasGuard()) {
      guard = edge.getGuard().getScript();
    }

    if (edge.getSourceVertex() != null) {
      sourceVertexId = edge.getSourceVertex().getId();
    }
    if (edge.getTargetVertex() != null) {
      targetVertexId = edge.getTargetVertex().getId();
    }

    if (edge.hasActions()) {
      actions = new ArrayList<>();
      for (Action action : edge.getActions()) {
        actions.add(action.getScript());
      }
    }

    if (edge.hasRequirements()) {
      requirements = new ArrayList<>();
      for (Requirement requirement : edge.getRequirements()) {
        requirements.add(requirement.getKey());
      }
    }

    if (edge.hasProperties()) {
      properties = new HashMap<>();
      properties.putAll(edge.getProperties());
    }
  }

  public void setEdge(Edge edge) {
    setEdge(edge.build());
  }

  public void copyValues(Edge edge) {
    if (id != null) {
      edge.setId(id);
    }

    if (name != null) {
      edge.setName(name);
    }

    edge.setWeight(weight);
    edge.setDependency(dependency);

    if (guard != null) {
      edge.setGuard(new Guard(guard));
    }

    if (actions != null) {
      for (String action : actions) {
        edge.addAction(new Action(action));
      }
    }

    if (requirements != null) {
      for (String requirement : requirements) {
        edge.addRequirement(new Requirement(requirement));
      }
    }

    if (properties != null) {
      edge.setProperties(properties);
    }
  }
}
