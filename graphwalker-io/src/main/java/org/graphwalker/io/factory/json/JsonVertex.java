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
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;

/**
 * @author Nils Olsson
 */
public class JsonVertex {

  private String id;
  private String name;
  private String sharedState;
  private List<String> actions;
  private List<String> requirements;
  private Map<String, Object> properties;

  public Vertex getVertex() {
    Vertex vertex = new Vertex();
    vertex.setId(id);
    vertex.setName(name);
    vertex.setSharedState(sharedState);

    if (requirements != null) {
      for (String requirement : requirements) {
        vertex.addRequirement(new Requirement(requirement));
      }
    }

    if (actions != null) {
      for (String action : actions) {
        vertex.addAction(new Action(action));
      }
    }

    if (properties != null) {
      vertex.setProperties(properties);
    }

    return vertex;
  }

  public void setVertex(Vertex.RuntimeVertex vertex) {
    id = vertex.getId();
    name = vertex.getName();
    sharedState = vertex.getSharedState();

    if (vertex.hasRequirements()) {
      requirements = new ArrayList<>();
      for (Requirement requirement : vertex.getRequirements()) {
        requirements.add(requirement.getKey());
      }
    }

    if (vertex.hasActions()) {
      actions = new ArrayList<>();
      for (Action action : vertex.getActions()) {
        actions.add(action.getScript());
      }
    }

    if (vertex.hasProperties()) {
      properties = new HashMap<>();
      properties.putAll(vertex.getProperties());
    }
  }

  public void setVertex(Vertex vertex) {
    setVertex(vertex.build());
  }

  public void copyValues(Vertex vertex) {
    if (id != null) {
      vertex.setId(id);
    }

    if (name != null) {
      vertex.setName(name);
    }

    if (sharedState != null) {
      vertex.setSharedState(sharedState);
    }

    if (requirements != null) {
      for (String requirement : requirements) {
        vertex.addRequirement(new Requirement(requirement));
      }
    }

    if (actions != null) {
      for (String action : actions) {
        vertex.addAction(new Action(action));
      }
    }

    if (properties != null) {
      vertex.setProperties(properties);
    }
  }
}
