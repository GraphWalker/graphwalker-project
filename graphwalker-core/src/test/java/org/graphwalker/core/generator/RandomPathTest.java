package org.graphwalker.core.generator;

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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class RandomPathTest {

    private final Vertex source = new Vertex();
    private final Vertex target = new Vertex();
    private final Edge edge = new Edge().setSourceVertex(source).setTargetVertex(target);
    private final Model model = new Model().addEdge(edge);

    @Test
    public void simpleTest() {
        PathGenerator generator = new RandomPath(new VertexCoverage(100));
        Context context = new TestExecutionContext().setModel(model.build()).setNextElement(source);
        context.setPathGenerator(generator);
        context.setCurrentElement(source.build());
        Assert.assertEquals(context.getCurrentElement(), source.build());
        Assert.assertEquals(generator.getNextStep().getCurrentElement(), edge.build());
        Assert.assertEquals(generator.getNextStep().getCurrentElement(), target.build());
        Assert.assertNotNull(generator.getContext());
        Assert.assertNotNull(generator.getStopCondition());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void newStopConditionTest() {
        Context context = new TestExecutionContext().setModel(model.build()).setNextElement(source);
        context.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        context.setCurrentElement(source.build());
        Assert.assertEquals(context.getCurrentElement(), source.build());
        Assert.assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), edge.build());
        Assert.assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), target.build());
        context.getPathGenerator().setStopCondition(new EdgeCoverage(100));
        Assert.assertTrue(EdgeCoverage.class.isAssignableFrom(context.getPathGenerator().getStopCondition().getClass()));
    }

    @Test(expected = NoPathFoundException.class)
    public void failTest() {
        Context context = new TestExecutionContext().setModel(model.build()).setNextElement(source);
        context.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        Assert.assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), source.build());
        Assert.assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), edge.build());
        Assert.assertEquals(context.getPathGenerator().getNextStep().getCurrentElement(), target.build());
        context.getPathGenerator().getNextStep(); // should fail
    }
}
