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
import org.graphwalker.core.model.NamedElement;

import java.util.List;

/**
 * @author Nils Olsson
 */
public final class Classification implements Builder<Classification.ImmutableClassification> {

    private ImmutableClassification classification = null;
    private final BuilderSet<Builder<ImmutableClassification>, ImmutableClassification> classifications = new BuilderSet<>();
    private Classification parent = null;
    private String name;

    public Classification setParent(Classification parent) {
        this.parent = parent;
        return this;
    }

    public Classification getParent() {
        return parent;
    }

    public Classification addClassification(Classification classification) {
        this.classifications.add(classification);
        classification.setParent(this);
        return this;
    }

    public BuilderSet<Builder<ImmutableClassification>, ImmutableClassification> getClassifications() {
        return classifications;
    }

    public Classification setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public ImmutableClassification build() {
        if (null == classification) {
            classification = new ImmutableClassification(this);
        }
        return classification;
    }

    public static final class ImmutableClassification extends NamedElement {

        private final ImmutableClassification parent = null;
        private final List<ImmutableClassification> classifications;

        private ImmutableClassification(Classification classification) {
            super(classification.getName());
            this.classifications = classification.getClassifications().build();
            //this.parent = classification.getParent().build();
        }

        public ImmutableClassification getParent() {
            return parent;
        }

        public List<ImmutableClassification> getClassifications() {
            return classifications;
        }
    }
}
