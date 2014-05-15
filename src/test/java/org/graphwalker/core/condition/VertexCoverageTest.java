package org.graphwalker.core.condition;

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

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class VertexCoverageTest {

    @Test
    public void testConstructor() {
        VertexCoverage vertexCoverage = new VertexCoverage(55);
        Assert.assertThat(vertexCoverage.getPercent(), is(55));
    }

    @Test
    public void testFulfilment() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));
        VertexCoverage vertexCoverage = new VertexCoverage(100);
        ExecutionContext context = new ExecutionContext(model, new RandomPath(vertexCoverage));
        Assert.assertThat(vertexCoverage.getFulfilment(context), is(0.0));
        context.setCurrentElement(v1.build());
        context.getProfiler().start();
        Assert.assertThat(vertexCoverage.getFulfilment(context), is(0.5));
        context.setCurrentElement(v2.build());
        context.getProfiler().start();
        Assert.assertThat(vertexCoverage.getFulfilment(context), is(1.0));
    }

    @Test
    public void testIsFulfilled() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));
        VertexCoverage vertexCoverage = new VertexCoverage(100);
        ExecutionContext context = new ExecutionContext(model, new RandomPath(vertexCoverage));
        Assert.assertFalse(vertexCoverage.isFulfilled(context));
        context.setCurrentElement(v1.build());
        context.getProfiler().start();
        Assert.assertFalse(vertexCoverage.isFulfilled(context));
        context.setCurrentElement(v2.build());
        context.getProfiler().start();
        Assert.assertTrue(vertexCoverage.isFulfilled(context));
    }
}
