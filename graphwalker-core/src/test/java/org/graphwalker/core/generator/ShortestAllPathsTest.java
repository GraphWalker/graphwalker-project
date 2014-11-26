package org.graphwalker.core.generator;

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

import org.graphwalker.core.algorithm.AlgorithmException;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.*;
import org.graphwalker.core.statistics.Profiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * @author Nils Olsson
 */
public class ShortestAllPathsTest {

    @Test
    public void bridge() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Vertex v3 = new Vertex();
        Vertex v4 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2);
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v3);
        Edge e3 = new Edge().setSourceVertex(v3).setTargetVertex(v1);
        Edge e4 = new Edge().setSourceVertex(v1).setTargetVertex(v4);
        Edge e5 = new Edge().setSourceVertex(v4).setTargetVertex(v1);
        Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5);
        Context context = new TestExecutionContext(model, new ShortestAllPaths(new EdgeCoverage(100)));
        context.setProfiler(new Profiler());
        Deque<Builder<? extends Element>> expectedElements = new ArrayDeque<Builder<? extends Element>>(
                Arrays.asList(e1, v2, e2, v3, e3, v1, e4, v4, e5, v1)
        );
        context.setNextElement(v1);
        while (context.getPathGenerator().hasNextStep()) {
            context.getPathGenerator().getNextStep();
            context.getProfiler().start(context);
            context.getProfiler().stop(context);
            Assert.assertEquals(expectedElements.removeFirst().build(), context.getCurrentElement());
        }
        Assert.assertTrue(expectedElements.isEmpty());
    }

    @Test
    public void bridgeNotCompleted() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Vertex v3 = new Vertex();
        Vertex v4 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2);
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v3);
        Edge e3 = new Edge().setSourceVertex(v3).setTargetVertex(v1);
        Edge e4 = new Edge().setSourceVertex(v1).setTargetVertex(v4);
        Edge e5 = new Edge().setSourceVertex(v4).setTargetVertex(v1);
        Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4).addEdge(e5);
        Context context = new TestExecutionContext(model, new ShortestAllPaths(new EdgeCoverage(50)));
        context.setProfiler(new Profiler());
        Deque<Builder<? extends Element>> expectedElements = new ArrayDeque<Builder<? extends Element>>(
                Arrays.asList(e1, v2, e2, v3, e3, v1)
        );
        context.setNextElement(v1);
        while (context.getPathGenerator().hasNextStep()) {
            context.getPathGenerator().getNextStep();
            context.getProfiler().start(context);
            context.getProfiler().stop(context);
            Assert.assertEquals(expectedElements.removeFirst().build(), context.getCurrentElement());
        }
        Assert.assertTrue(expectedElements.isEmpty());
    }

    @Test
    public void circle() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Vertex v3 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2);
        Edge e2 = new Edge().setSourceVertex(v2).setTargetVertex(v3);
        Edge e3 = new Edge().setSourceVertex(v3).setTargetVertex(v1);
        Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3);
        Context context = new TestExecutionContext(model, new ShortestAllPaths(new EdgeCoverage(100)));
        context.setProfiler(new Profiler());
        Deque<Builder<? extends Element>> expectedElements = new ArrayDeque<Builder<? extends Element>>(
                Arrays.asList(e1, v2, e2, v3, e3, v1)
        );
        context.setNextElement(v1);
        while (context.getPathGenerator().hasNextStep()) {
            context.getPathGenerator().getNextStep();
            context.getProfiler().start(context);
            context.getProfiler().stop(context);
            Assert.assertEquals(expectedElements.removeFirst().build(), context.getCurrentElement());
        }
        Assert.assertTrue(expectedElements.isEmpty());
    }

    @Test(expected = AlgorithmException.class)
    public void tree() {
        Vertex v1 = new Vertex();
        Vertex v2 = new Vertex();
        Vertex v3 = new Vertex();
        Edge e1 = new Edge().setSourceVertex(v1).setTargetVertex(v2);
        Edge e2 = new Edge().setSourceVertex(v1).setTargetVertex(v3);
        Model model = new Model().addEdge(e1).addEdge(e2);
        Context context = new TestExecutionContext(model, new ShortestAllPaths(new EdgeCoverage(100)));
        context.setProfiler(new Profiler());
        context.setNextElement(v1);
        while (context.getPathGenerator().hasNextStep()) {
            context.getPathGenerator().getNextStep();
            context.getProfiler().start(context);
            context.getProfiler().stop(context);
        }
    }

}
