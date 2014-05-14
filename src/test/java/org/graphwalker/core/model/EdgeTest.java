package org.graphwalker.core.model;

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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class EdgeTest {

    @Test
    public void create() {
        Edge edge = new Edge()
                .setGuard(new Guard("script"))
                .setName("name")
                .setSourceVertex(new Vertex())
                .setTargetVertex(new Vertex())
                .setBlocked(true)
                .addAction(new Action("action1"))
                .addActions(Arrays.asList(new Action("action2"), new Action("action3")))
                .setWeight(2.0);
        Assert.assertNotNull(edge);
        Assert.assertTrue(edge.isBlocked());
        Assert.assertTrue(edge.build().isBlocked());
        Assert.assertEquals("name", edge.getName());
        Assert.assertEquals("name", edge.build().getName());
        Assert.assertNotNull(edge.getSourceVertex());
        Assert.assertNotNull(edge.build().getTargetVertex());
        Assert.assertNotNull(edge.getTargetVertex());
        Assert.assertNotNull(edge.build().getTargetVertex());
        Assert.assertNotNull(edge.getGuard());
        Assert.assertNotNull(edge.build().getGuard());
        Assert.assertEquals(edge.getGuard(), edge.build().getGuard());
        Assert.assertNotNull(edge.getActions());
        Assert.assertThat(edge.getActions().size(), is(3));
        Assert.assertThat(edge.getWeight(), is(2.0));
        Assert.assertNotNull(edge.build().getActions());
        Assert.assertThat(edge.build().getActions().size(), is(3));
        Assert.assertThat(edge.build().getWeight(), is(2.0));
    }
}
