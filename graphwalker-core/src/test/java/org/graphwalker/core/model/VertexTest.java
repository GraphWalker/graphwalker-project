package org.graphwalker.core.model;

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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Nils Olsson
 */
public class VertexTest {

    @Test
    public void create() {
        Vertex vertex = new Vertex()
                .setName("vertex")
                .setSharedState("MY_STATE")
                .addRequirement(new Requirement("REQ1"))
                .addRequirement(new Requirement("REQ2"));
        Assert.assertNotNull(vertex);
        Assert.assertNotNull(vertex.getName());
        Assert.assertEquals(vertex.getName(), "vertex");
        Assert.assertNotNull(vertex.getSharedState());
        Assert.assertThat(vertex.getSharedState(), is("MY_STATE"));
        Assert.assertNotNull(vertex.getRequirements());
        Assert.assertThat(vertex.getRequirements().size(), is(2));
        Assert.assertNotNull(vertex.build());
        Assert.assertNotEquals(vertex, vertex.build());
        Assert.assertEquals(vertex.build(), vertex.build());
        Assert.assertEquals(vertex.build().getName(), vertex.getName());
        Assert.assertNotNull(vertex.build().getRequirements());
        Assert.assertThat(vertex.build().getRequirements().size(), is(2));
    }

    @Test
    public void testEquality() throws Exception {
        Vertex v1 = new Vertex().setId("n0").setName("SomeName");
        Vertex v2 = new Vertex().setId("n0").setName("SomeName");
        Assert.assertThat(v1.build(), is(v2.build()));
    }

    @Test
    public void testInequality() throws Exception {
        Vertex v1 = new Vertex().setId("n0").setName("SomeName");
        Vertex v2 = new Vertex().setId("n1").setName("SomeName");
        Assert.assertThat(v1.build(), not(v2.build()));
    }
}
