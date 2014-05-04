package org.graphwalker.core.model.efsm;

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
import org.graphwalker.core.model.NamedElement;

/**
 * @author Nils Olsson
 */
public final class Vertex implements Builder<Vertex.ImmutableVertex> {

    private ImmutableVertex vertex = null;
    private String name;

    public Vertex setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public ImmutableVertex build() {
        if (null == vertex) {
            vertex = new ImmutableVertex(this);
        }
        return vertex;
    }

    public static final class ImmutableVertex extends NamedElement {

        private ImmutableVertex(Vertex vertex) {
            super(vertex.getName());
        }
    }
}
