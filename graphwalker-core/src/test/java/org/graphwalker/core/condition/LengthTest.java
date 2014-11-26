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
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.statistics.Profiler;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class LengthTest {

    @Test
    public void testConstructor() {
        Length length = new Length(333);
        Assert.assertThat(length.getLength(), is(333l));
    }

    @Test
    public void testFulfilment() {
        Vertex vertex = new Vertex();
        Model model = new Model().addVertex(vertex);
        StopCondition condition = new Length(100);
        Context context = new TestExecutionContext(model, new RandomPath(condition)).setCurrentElement(vertex.build());
        context.setProfiler(new Profiler());
        for (int i = 0; i <= 100; i++) {
            Assert.assertThat(condition.getFulfilment(), is((double)i/100));
            context.getProfiler().start(context);
            context.getProfiler().stop(context);
            Assert.assertThat(condition.getFulfilment(), is((double)(i+1)/100));
        }
    }

    @Test
    public void testIsFulfilled() {
        Vertex vertex = new Vertex();
        Model model = new Model().addVertex(vertex);
        StopCondition condition = new Length(100);
        Context context = new TestExecutionContext(model, new RandomPath(condition)).setCurrentElement(vertex.build());
        context.setProfiler(new Profiler());
        for (int i = 0; i < 100; i++) {
            Assert.assertFalse(condition.isFulfilled());
            context.getProfiler().start(context);
            context.getProfiler().stop(context);
        }
        Assert.assertTrue(condition.isFulfilled());
    }
}
