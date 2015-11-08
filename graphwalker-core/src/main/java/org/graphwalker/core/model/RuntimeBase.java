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

import java.util.*;

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
        this(id, name, new ArrayList<Action>(), new HashSet<Requirement>());
    }

    protected RuntimeBase(String id, String name, Set<Requirement> requirements, Map<String, Object> properties) {
        this(id, name, new ArrayList<Action>(), requirements, properties);
    }

    protected RuntimeBase(String id, String name, List<Action> actions, Set<Requirement> requirements) {
        this(id, name, actions, requirements, new HashMap<String, Object>());
    }

    protected RuntimeBase(String id, String name, List<Action> actions, Set<Requirement> requirements, Map<String, Object> properties) {
        this.id = getIdOrDefault(id);
        this.name = name;
        this.actions = Collections.unmodifiableList(actions);
        this.requirements = Collections.unmodifiableSet(requirements);
        this.properties = Collections.unmodifiableMap(properties);
    }

    private String getIdOrDefault(String id) {
        return null != id && !"".equals(id) ? id : createDefault();
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
        return id != null && !"".equals(id);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasName() {
        return name != null && !"".equals(name);
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
        if (actions == null)
            return false;
        return !actions.isEmpty();
    }

    public Set<Requirement> getRequirements() {
        return requirements;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean hasRequirements() {
        if (requirements == null)
            return false;
        return !requirements.isEmpty();
    }

    public boolean hasProperties() {
        if (properties == null)
            return false;
        return !properties.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuntimeBase that = (RuntimeBase) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(actions, that.actions) &&
                Objects.equals(requirements, that.requirements) &&
                Objects.equals(properties, that.properties);
    }
}
