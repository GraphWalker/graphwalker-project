package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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
import java.util.List;

/**
 * @author Nils Olsson
 */
public final class Classification extends CachedBuilder<Classification.RuntimeClassification> {

    private String id;
    private String name;
    private final List<Classification> classifications = new ArrayList<>();

    public String getId() {
        return id;
    }

    public Classification setId(String id) {
        this.id = id;
        return this;
    }

    public Classification setName(String name) {
        this.name = name;
        invalidateCache();
        return this;
    }

    public String getName() {
        return name;
    }

    public Classification addClassification(Classification classification) {
        this.classifications.add(classification);
        invalidateCache();
        return this;
    }

    public List<Classification> getClassifications() {
        return classifications;
    }

    @Override
    protected RuntimeClassification createCache() {
        return new RuntimeClassification(this);
    }

    public static final class RuntimeClassification implements Element {

        private final String id;
        private final String name;
        private final List<RuntimeClassification> classifications;

        private RuntimeClassification(Classification classification) {
            this.id = classification.getId();
            this.name = classification.getName();
            this.classifications = BuilderFactory.build(classification.getClassifications());
        }

        public String getId() {
            return id;
        }

        public boolean hasId() {
            return id != null && !"".equals(id);
        }

        public String getName() {
            return name;
        }

        public boolean hasName() {
            return name != null && !"".equals(name);
        }

        public List<RuntimeClassification> getClassifications() {
            return classifications;
        }

        @Override
        public void accept(ElementVisitor visitor) {
            visitor.visit(this);
        }
    }
}
