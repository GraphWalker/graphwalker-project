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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

/**
 * @author Nils Olsson
 */
public class VertexTest {

  @Test
  public void create() throws Exception {
    Vertex vertex = new Vertex()
        .setName("vertex")
        .setSharedState("MY_STATE")
        .setActions(Arrays.asList(new Action("action2"), new Action("action3")))
        .addAction(new Action("action1"))
        .addRequirement(new Requirement("REQ1"))
        .addRequirement(new Requirement("REQ2"));
    assertNotNull(vertex);
    assertNotNull(vertex.getName());
    assertEquals(vertex.getName(), "vertex");
    assertNotNull(vertex.getSharedState());
    assertThat(vertex.getSharedState(), is("MY_STATE"));
    assertNotNull(vertex.getActions());
    assertThat(vertex.getActions().size(), is(3));
    assertNotNull(vertex.getRequirements());
    assertThat(vertex.getRequirements().size(), is(2));
    assertNotNull(vertex.build());
    assertNotEquals(vertex, vertex.build());
    assertEquals(vertex.build(), vertex.build());
    assertEquals(vertex.build().getName(), vertex.getName());
    assertNotNull(vertex.build().getRequirements());
    assertThat(vertex.build().getRequirements().size(), is(2));
  }

  @Test
  public void vertexWithAction() throws Exception {
    Vertex vertex = new Vertex();
    assertFalse(vertex.build().hasActions());
    assertTrue(vertex.build().getActions().isEmpty());
    assertTrue(vertex.addAction(new Action("")).build().hasActions());
    assertTrue(vertex.addActions(new Action("")).build().hasActions());
    assertTrue(vertex.setActions(Arrays.asList(new Action(""))).build().hasActions());
    assertFalse(vertex.build().getActions().isEmpty());
  }

  @Test
  public void testEquality() throws Exception {
    Vertex v1 = new Vertex().setId("n0").setName("SomeName");
    Vertex v2 = new Vertex().setId("n0").setName("SomeName");
    assertThat(v1.build(), is(v2.build()));
  }

  @Test
  public void testInequality() throws Exception {
    Vertex v1 = new Vertex().setId("n0").setName("SomeName");
    Vertex v2 = new Vertex().setId("n1").setName("SomeName");
    assertThat(v1.build(), not(v2.build()));
  }

  @Test
  public void testProperties() throws Exception {
    Vertex vertex = new Vertex();
    assertFalse(vertex.build().hasProperties());
    vertex.setProperty("test", "value");
    assertTrue(vertex.build().hasProperties());
  }
}
