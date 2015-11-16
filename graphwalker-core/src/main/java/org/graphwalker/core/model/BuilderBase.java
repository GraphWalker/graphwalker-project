package org.graphwalker.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        return (B)this;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public B setName(String name) {
        this.name = name;
        return (B)this;
    }

    public Set<Requirement> getRequirements() {
        return requirements;
    }

    @SuppressWarnings("unchecked")
    public B addRequirement(Requirement requirement) {
        requirements.add(requirement);
        return (B)this;
    }

    @SuppressWarnings("unchecked")
    public B setRequirements(Set<Requirement> requirements) {
        this.requirements = new HashSet<>(requirements);
        return (B)this;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    public B setProperties(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
        return (B)this;
    }

    @SuppressWarnings("unchecked")
    public B setProperty(String key, Object value) {
        properties.put(key, value);
        return (B)this;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
}
