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

import org.graphwalker.core.common.Objects;

import java.util.*;

import static org.graphwalker.core.common.Objects.*;

/**
 * @author Nils Olsson
 */
public abstract class RuntimeBase implements Element {

  private final String id;
  private final String name;
  private final List<Action> actions;
  private final Set<Requirement> requirements;
  private final Map<String, Object> properties;

  protected RuntimeBase(String id, String name) {
    this(id, name, new ArrayList<>(), new HashSet<>());
  }

  protected RuntimeBase(String id, String name, List<Action> actions, Set<Requirement> requirements) {
    this(id, name, actions, requirements, new HashMap<>());
  }

  protected RuntimeBase(String id, String name, List<Action> actions, Set<Requirement> requirements, Map<String, Object> properties) {
    this.id = getIdOrDefault(id);
    this.name = name;
    this.actions = unmodifiableList(actions);
    this.requirements = unmodifiableSet(requirements);
    this.properties = unmodifiableMap(properties);
  }

  private String getIdOrDefault(String id) {
    return isNotNullOrEmpty(id) ? id : createDefault();
  }

  private String createDefault() {
    return UUID.randomUUID().toString();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean hasId() {
    return isNotNullOrEmpty(id);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean hasName() {
    return isNotNullOrEmpty(name);
  }

  @Override
  public Object getProperty(String key) {
    return properties.get(key);
  }

  @Override
  public boolean hasProperty(String key) {
    return properties.containsKey(key);
  }

  public List<Action> getActions() {
    return actions;
  }

  public boolean hasActions() {
    return isNotNullOrEmpty(actions);
  }

  public Set<Requirement> getRequirements() {
    return requirements;
  }

  public boolean hasRequirements() {
    return isNotNullOrEmpty(requirements);
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public boolean hasProperties() {
    return isNotNullOrEmpty(properties);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((actions == null) ? 0 : actions.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result
             + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result
             + ((requirements == null) ? 0 : requirements.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (isNull(o) || getClass() != o.getClass()) {
      return false;
    }
    RuntimeBase that = (RuntimeBase) o;
    return Objects.equals(id, that.id) &&
           Objects.equals(name, that.name) &&
           Objects.equals(actions, that.actions) &&
           Objects.equals(requirements, that.requirements) &&
           Objects.equals(properties, that.properties);
  }

}
