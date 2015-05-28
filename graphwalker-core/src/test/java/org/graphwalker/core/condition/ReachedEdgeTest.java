package org.graphwalker.core.condition;

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

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class ReachedEdgeTest {

    @Test(expected = StopConditionException.class)
    public void testMissingEdge() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1");
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setName("e2");
        Model model = new Model().addEdge(e1).addEdge(e2);
        StopCondition condition = new ReachedEdge("NOT_FOUND");
        new TestExecutionContext(model, new RandomPath(condition));
    }

    @Test
    public void testFulfilment() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1");
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setName("e2");
        Model model = new Model().addEdge(e1).addEdge(e2);
        StopCondition condition = new ReachedEdge("e2");
        Context context = new TestExecutionContext(model, new RandomPath(condition));
        context.setCurrentElement(v1.build());
        Assert.assertThat(condition.getFulfilment(), is(0.25));
        context.setCurrentElement(e1.build());
        Assert.assertThat(condition.getFulfilment(), is(0.50));
        context.setCurrentElement(v2.build());
        Assert.assertThat(condition.getFulfilment(), is(0.75));
        context.setCurrentElement(e2.build());
        Assert.assertThat(condition.getFulfilment(), is(1.0));
    }

    @Test
    public void testIsFulfilled() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1");
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setName("e2");
        Model model = new Model().addEdge(e1).addEdge(e2);
        StopCondition condition = new ReachedEdge("e2");
        Context context = new TestExecutionContext(model, new RandomPath(condition));
        Assert.assertFalse(condition.isFulfilled());
        context.setCurrentElement(e1.build());
        Assert.assertFalse(condition.isFulfilled());
        context.setCurrentElement(e2.build());
        Assert.assertTrue(condition.isFulfilled());
    }


    @Test
    public void testInterface() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1");
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v1).setName("e2");
        Model model = new Model().addEdge(e1).addEdge(e2);
        ReachedStopCondition condition = new ReachedEdge("e2");
        Context context = new TestExecutionContext(model, new RandomPath(condition));
        Assert.assertFalse(condition.isFulfilled());
        context.setCurrentElement(e1.build());
        Assert.assertFalse(condition.isFulfilled());
        context.setCurrentElement(e2.build());
        Assert.assertTrue(condition.isFulfilled());
        Assert.assertFalse(condition.getTargetElements().isEmpty());
    }
}
