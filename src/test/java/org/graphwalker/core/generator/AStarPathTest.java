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

import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class AStarPathTest {

    private final Vertex v1 = new Vertex().setName("v1");
    private final Vertex v2 = new Vertex().setName("v2");
    private final Vertex v3 = new Vertex().setName("v3");
    private final Vertex v4 = new Vertex().setName("end");
    private final Vertex v5 = new Vertex().setName("v5");

    private final Edge e1 = new Edge().setName("e1").setSourceVertex(v1).setTargetVertex(v2);
    private final Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v3);
    private final Edge e3 = new Edge().setName("e3").setSourceVertex(v3).setTargetVertex(v4);
    private final Edge e4 = new Edge().setName("e4").setSourceVertex(v1).setTargetVertex(v5).addAction(new Action("var closed = 0;"));
    private final Edge e5 = new Edge().setName("e5").setSourceVertex(v5).setTargetVertex(v4).setGuard(new Guard("closed == 1"));
    private final Edge e6 = new Edge().setName("e6").setSourceVertex(v5).setTargetVertex(v1);

    private final Model model = new Model()
            .addEdge(e1)
            .addEdge(e2)
            .addEdge(e3)
            .addEdge(e4)
            .addEdge(e5)
            .addEdge(e6);
/*
    @Test
    public void simpleTest() {
        ExecutionContext context = new ExecutionContext(model, new AStarPath(new ReachedVertex("end")))
                .setNextElement(v1);
        Machine machine = new SimpleMachine(context);
        Deque<Builder<? extends Element>> expectedElements = new ArrayDeque<Builder<? extends Element>>(
                Arrays.asList(v1, e4, v5, e6, v1, e1, v2, e2, v3, e3, v4)
        );
        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
            //Assert.assertEquals(expectedElements.removeFirst().build(), context.getCurrentElement());
        }
        Assert.assertThat(expectedElements.size(), is(0));
    }
*/
    @Test(expected = NoPathFoundException.class)
    public void allPathsBlocked() {
        e1.setBlocked(true);
        e4.setBlocked(true);
        ExecutionContext context = new ExecutionContext(model, new AStarPath(new ReachedVertex("end")))
                .setNextElement(v1);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep(); // should fail due to that there is no way to travel to the end
        }
    }


    @Test(expected = NoPathFoundException.class)
    public void failTest() {
        ExecutionContext context = new ExecutionContext(model, new AStarPath(new ReachedVertex("end")));
        Machine machine = new SimpleMachine(context);
        // Missing a start point, shall generate an exception
        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement());
        }
    }
}
