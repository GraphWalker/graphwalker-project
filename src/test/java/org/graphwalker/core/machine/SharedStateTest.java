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
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class SharedStateTest {

    @Test
    public void singleSharedStates() {
        Vertex vertex = new Vertex().setName("A").setSharedState("CUSTOM_STATE");
        Edge edge = new Edge().setName("B").setSourceVertex(vertex).setTargetVertex(vertex);
        Model model = new Model().addEdge(edge);
        Context context = new TestExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
        context.setNextElement(vertex);
        Context sharedContext = new TestExecutionContext(new Model().addVertex(new Vertex().setName("C").setSharedState("CUSTOM_STATE")), new RandomPath(new VertexCoverage(100)));
        Machine machine = new SimpleMachine(context, sharedContext);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertThat(context.getProfiler().getUnvisitedElements(context).isEmpty(), is(true));
        Assert.assertThat(sharedContext.getProfiler().getUnvisitedElements(context).isEmpty(), is(true));
    }

    @Test
    public void multipleSharedStates() {
        Vertex shared1 = new Vertex().setName("A");
        Vertex shared2 = new Vertex().setName("B");
        Vertex shared3 = new Vertex().setName("E");
        Model model1 = new Model().addVertex(shared1.setSharedState("SHARED1")).addEdge(new Edge().setName("I").setSourceVertex(new Vertex().setName("H").setSharedState("SHARED3")).setTargetVertex(shared1));
        Model model2 = new Model().addVertex(shared2.setSharedState("SHARED1")).addEdge(new Edge().setName("C").setSourceVertex(shared2).setTargetVertex(new Vertex().setName("D").setSharedState("SHARED2")));
        Model model3 = new Model().addVertex(shared3.setSharedState("SHARED2")).addEdge(new Edge().setName("F").setSourceVertex(shared3).setTargetVertex(new Vertex().setName("G").setSharedState("SHARED3")));
        Context context1 = new TestExecutionContext(model1, new RandomPath(new EdgeCoverage(100))).setNextElement(shared1);
        Context context2 = new TestExecutionContext(model2, new RandomPath(new VertexCoverage(100)));
        Context context3 = new TestExecutionContext(model3, new RandomPath(new VertexCoverage(100)));
        Machine machine = new SimpleMachine(context1, context2, context3);
        while (machine.hasNextStep()) {
            Context context = machine.getNextStep();
            System.out.println(context.getCurrentElement().getName()
                +" "+context1.getPathGenerator().getStopCondition().getFulfilment(context1)
                +" "+context2.getPathGenerator().getStopCondition().getFulfilment(context2)
                +" "+context3.getPathGenerator().getStopCondition().getFulfilment(context3));
        }
        Assert.assertThat(machine.getProfiler().getUnvisitedElements(context1).isEmpty(), is(true));
        Assert.assertThat(machine.getProfiler().getUnvisitedElements(context2).isEmpty(), is(true));
        Assert.assertThat(machine.getProfiler().getUnvisitedElements(context3).isEmpty(), is(true));
        List<String> names = new ArrayList<>();
        for (Element element: machine.getProfiler().getPath()) {
            names.add(element.getName());
        }
        Assert.assertArrayEquals(names.toArray(), Arrays.asList("A", "I", "H", "G", "F", "E", "D", "C", "B", "A").toArray());
    }
}
