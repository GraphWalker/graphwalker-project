package org.graphwalker.core.machine;

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
import org.graphwalker.core.generator.NoPathFoundException;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class SimpleMachineTest {

    @Test
    public void simpleMachine() {
        Vertex vertex = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(new Vertex()));
        ExecutionContext context = new ExecutionContext(model, new RandomPath(new VertexCoverage(100)));
        context.setNextElement(vertex);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
    }

    @Test
    public void noStartVertex() {
        Edge edge = new Edge().setTargetVertex(new Vertex());
        Model model = new Model().addEdge(edge);
        ExecutionContext context = new ExecutionContext(model, new RandomPath(new VertexCoverage(100)));
        context.setNextElement(edge);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
    }

    @Test
    public void executeAction() {
        Vertex vertex1 = new Vertex();
        Vertex vertex2 = new Vertex();
        Model model = new Model()
                .addEdge(new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2).addAction(new Action("var i = 1;")))
                .addEdge(new Edge().setSourceVertex(vertex2).setTargetVertex(vertex1).setGuard(new Guard("i != 0")));
        ExecutionContext context = new ExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
        context.setNextElement(vertex1);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
        Assert.assertThat(context.getProfiler().getTotalVisitCount(), is(4l));
    }

    @Test(expected = NoPathFoundException.class)
    public void honorGuard() {
        Vertex vertex1 = new Vertex();
        Vertex vertex2 = new Vertex();
        Model model = new Model()
                .addEdge(new Edge().setSourceVertex(vertex1).setTargetVertex(vertex2).addAction(new Action("var i = 1;")))
                .addEdge(new Edge().setSourceVertex(vertex2).setTargetVertex(vertex1).setGuard(new Guard("i == 0")));
        ExecutionContext context = new ExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
        context.setNextElement(vertex1);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
        Assert.assertThat(context.getProfiler().getTotalVisitCount(), is(3l));
    }

    @Test
    public void sharedState() {
        Vertex start = new Vertex();
        Vertex shared1 = new Vertex().setSharedState("MyState");
        Edge edge1 = new Edge().setSourceVertex(start).setTargetVertex(shared1);
        Vertex shared2 = new Vertex().setSharedState("MyState");
        Vertex stop  = new Vertex();
        Edge edge2 = new Edge().setSourceVertex(shared2).setTargetVertex(stop);
        Model model1 = new Model().addEdge(edge1);
        Model model2 = new Model().addEdge(edge2);
        List<ExecutionContext> contexts = new ArrayList<>();
        contexts.add(new ExecutionContext(model1, new RandomPath(new VertexCoverage(100))).setNextElement(start));
        contexts.add(new ExecutionContext(model2, new RandomPath(new VertexCoverage(100))));
        Machine machine = new SimpleMachine(contexts);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        // the profiler path is a stack so the first element is the last visited, therefore we create the list in reverse order
        Path<Element> path1 = new Path<>(Arrays.<Element>asList(shared1.build(), edge1.build(), start.build()));
        Path<Element> path2 = new Path<>(Arrays.<Element>asList(stop.build(), edge2.build(), shared2.build()));
        Assert.assertArrayEquals(contexts.get(0).getProfiler().getPath().toArray(), path1.toArray());
        Assert.assertArrayEquals(contexts.get(1).getProfiler().getPath().toArray(), path2.toArray());
    }

    @Test
    public void singleSharedStates() {
        Vertex start = new Vertex();
        Vertex shared1 = new Vertex().setSharedState("MyState1");
        Edge edge1 = new Edge().setSourceVertex(start).setTargetVertex(shared1);
        Vertex shared2 = new Vertex().setSharedState("MyState2");
        Vertex stop  = new Vertex();
        Edge edge2 = new Edge().setSourceVertex(shared2).setTargetVertex(stop);
        Model model1 = new Model().addEdge(edge1);
        Model model2 = new Model().addEdge(edge2);
        List<ExecutionContext> contexts = new ArrayList<>();
        contexts.add(new ExecutionContext(model1, new RandomPath(new VertexCoverage(100))).setNextElement(start));
        contexts.add(new ExecutionContext(model2, new RandomPath(new VertexCoverage(100))));
        Machine machine = new SimpleMachine(contexts);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        // the profiler path is a stack so the first element is the last visited, therefore we create the list in reverse order
        //Path<Element> path1 = new Path<>(Arrays.<Element>asList(shared1.build(), edge1.build(), start.build()));
        //Path<Element> path2 = new Path<>(Arrays.<Element>asList(stop.build(), edge2.build(), shared2.build()));
        //Assert.assertArrayEquals(contexts.get(0).getProfiler().getPath().toArray(), path1.toArray());
        //Assert.assertArrayEquals(contexts.get(1).getProfiler().getPath().toArray(), path2.toArray());
    }
}
