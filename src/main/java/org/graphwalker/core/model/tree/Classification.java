package org.graphwalker.core.model.tree;

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

import org.graphwalker.core.model.Builder;
import org.graphwalker.core.model.BuilderSet;
import org.graphwalker.core.model.EmptyBuilder;
import org.graphwalker.core.model.NamedElement;

import java.util.List;

/**
 * @author Nils Olsson
 */
public final class Classification extends NamedElement {

    private final Classification parent;
    private final List<Classification> classifications;

    private Classification(ClassificationBuilder builder) {
        super(builder.getName());
        this.parent = builder.getParent().build();
        this.classifications = builder.getClassifications().build();
    }

    public Classification getParent() {
        return parent;
    }

    public List<Classification> getClassifications() {
        return classifications;
    }

    public static class ClassificationBuilder implements Builder<Classification> {

        public static final Builder<Classification> EMPTY = new EmptyBuilder<>();

        private final BuilderSet<Builder<Classification>, Classification> classifications = new BuilderSet<>();
        private ClassificationBuilder parent;
        private String name;

        public ClassificationBuilder setParent(ClassificationBuilder parent) {
            this.parent = parent;
            return this;
        }

        public ClassificationBuilder getParent() {
            return parent;
        }

        public ClassificationBuilder addClassification(ClassificationBuilder classification) {
            this.classifications.add(classification);
            return this;
        }

        public BuilderSet<Builder<Classification>, Classification> getClassifications() {
            return classifications;
        }

        public ClassificationBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        @Override
        public Classification build() {
            return new Classification(this);
        }
    }
}
