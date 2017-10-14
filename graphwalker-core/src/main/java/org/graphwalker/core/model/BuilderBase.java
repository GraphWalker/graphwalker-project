package org.graphwalker.core.model;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.graphwalker.core.common.Objects.unmodifiableMap;
import static org.graphwalker.core.common.Objects.unmodifiableSet;

public abstract class BuilderBase<B, T> implements Builder<T> {

  private String id;
  private String name;
  private Set<Requirement> requirements = new HashSet<>();
  private Map<String, Object> properties = new HashMap<>();

  public String getId() {
    return id;
  }

  @SuppressWarnings("unchecked")
  public B setId(String id) {
    this.id = id;
    return (B) this;
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public B setName(String name) {
    this.name = name;
    return (B) this;
  }

  public Set<Requirement> getRequirements() {
    return unmodifiableSet(requirements);
  }

  @SuppressWarnings("unchecked")
  public B addRequirement(Requirement requirement) {
    requirements.add(requirement);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B setRequirements(Set<Requirement> requirements) {
    this.requirements = new HashSet<>(requirements);
    return (B) this;
  }

  public boolean hasProperty(String key) {
    return properties.containsKey(key);
  }

  public Map<String, Object> getProperties() {
    return unmodifiableMap(properties);
  }

  @SuppressWarnings("unchecked")
  public B setProperties(Map<String, Object> properties) {
    this.properties = new HashMap<>(properties);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B setProperty(String key, Object value) {
    properties.put(key, value);
    return (B) this;
  }

  public Object getProperty(String key) {
    return properties.get(key);
  }
}
