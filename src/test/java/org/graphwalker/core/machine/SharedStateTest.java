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
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class SharedStateTest {

    @Test
    public void shared() {
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
}
