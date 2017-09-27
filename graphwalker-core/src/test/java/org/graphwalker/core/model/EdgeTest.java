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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class EdgeTest {

  @Test
  public void create() throws Exception {
    Edge edge = new Edge()
        .setGuard(new Guard("script"))
        .setName("name")
        .setSourceVertex(new Vertex())
        .setTargetVertex(new Vertex())
        .setActions(Arrays.asList(new Action("action2"), new Action("action3")))
        .addAction(new Action("action1"))
        .setWeight(.5);
    assertNotNull(edge);
    assertEquals("name", edge.getName());
    assertEquals("name", edge.build().getName());
    assertNotNull(edge.getSourceVertex());
    assertNotNull(edge.build().getTargetVertex());
    assertNotNull(edge.getTargetVertex());
    assertNotNull(edge.build().getTargetVertex());
    assertNotNull(edge.getGuard());
    assertNotNull(edge.build().getGuard());
    assertTrue(edge.build().hasGuard());
    assertEquals(edge.getGuard(), edge.build().getGuard());
    assertNotNull(edge.getActions());
    assertThat(edge.getActions().size(), is(3));
    assertThat(edge.getWeight(), is(.5));
    assertNotNull(edge.build().getActions());
    assertThat(edge.build().getActions().size(), is(3));
    assertThat(edge.build().getWeight(), is(.5));
    assertFalse(edge.setGuard(null).build().hasGuard());
    assertFalse(edge.setGuard(new Guard("")).build().hasGuard());
  }

  @Test
  public void edgeWithAction() throws Exception {
    Edge edge = new Edge();
    assertFalse(edge.build().hasActions());
    assertTrue(edge.build().getActions().isEmpty());
    assertTrue(edge.addAction(new Action("")).build().hasActions());
    assertTrue(edge.addActions(new Action("")).build().hasActions());
    assertTrue(edge.setActions(Arrays.asList(new Action(""))).build().hasActions());
    assertFalse(edge.build().getActions().isEmpty());
  }

  @Test
  public void testEquality() throws Exception {
    Edge e1 = new Edge().setId("ID1");
    Edge e2 = new Edge().setId("ID1");
    assertThat(e1.build(), is(e2.build()));
  }

  @Test
  public void testInequality() throws Exception {
    Edge e1 = new Edge().setId("ID1");
    Edge e2 = new Edge().setId("ID2");
    assertThat(e1.build(), not(e2.build()));
  }

  @Test
  public void testProperties() throws Exception {
    Edge edge = new Edge().setId("ID");
    assertFalse(edge.build().hasProperties());
    assertTrue(edge.setProperty("x", "y").build().hasProperties());
    assertThat((String) edge.getProperty("x"), is("y"));
    assertThat((String) edge.build().getProperty("x"), is("y"));
  }
}
