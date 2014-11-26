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
public abstract class ElementBase implements Element {

    private final String id;
    private final String name;
    private final List<Action> actions;
    private final Set<Requirement> requirements;

    protected ElementBase(String id, String name) {
        this(id, name, new ArrayList<Action>(), new HashSet<Requirement>());
    }

    protected ElementBase(String id, String name, Set<Requirement> requirements) {
        this(id, name, new ArrayList<Action>(), requirements);
    }

    protected ElementBase(String id, String name, List<Action> actions, Set<Requirement> requirements) {
        this.id = id;
        this.name = name;
        this.actions = Collections.unmodifiableList(actions);
        this.requirements = Collections.unmodifiableSet(requirements);
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

    public List<Action> getActions() {
        return actions;
    }

    public boolean hasActions() {
        return !actions.isEmpty();
    }

    public Set<Requirement> getRequirements() {
        return requirements;
    }

    public boolean hasRequirements() {
        return !requirements.isEmpty();
    }
}
