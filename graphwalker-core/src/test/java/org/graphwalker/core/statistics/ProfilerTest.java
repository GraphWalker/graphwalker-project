package org.graphwalker.core.statistics;

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
public final class ProfilerTest {

    private static final Vertex start = new Vertex();
    private static final Context context = new TestExecutionContext()
            .setModel(new Model()
                    .addEdge(new Edge()
                            .setSourceVertex(start)
                            .setTargetVertex(new Vertex())).build())
            .setCurrentElement(start.build());

    @Test
    public void create() {
        Profiler profiler = new Profiler();
        Assert.assertNotNull(profiler);
        Assert.assertFalse(profiler.isVisited(start.build()));
        Assert.assertThat(profiler.getTotalVisitCount(), is(0l));
        Assert.assertThat(profiler.getVisitCount(start.build()), is(0l));
        profiler.start(context);
        profiler.stop(context);
        Assert.assertTrue(profiler.isVisited(start.build()));
        Assert.assertThat(profiler.getTotalVisitCount(), is(1l));
        Assert.assertThat(profiler.getVisitCount(start.build()), is(1l));
        Assert.assertThat(profiler.getUnvisitedElements(context).size(), is(2));
        Assert.assertThat(profiler.getUnvisitedEdges(context).size(), is(1));
        Assert.assertThat(profiler.getUnvisitedVertices(context).size(), is(1));
        Assert.assertThat(profiler.getPath().size(), is(1));
    }

}
