package org.graphwalker.io.factory.dot;

/*
 * #%L
 * GraphWalker Input/Output
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.common.ResourceNotFoundException;
import org.junit.Test;

/**
 * @author Kristian Karl
 */
public class DotContextFactoryTest {

  @Test(expected = ResourceNotFoundException.class)
  public void readNotAvailable() throws IOException {
    new DotContextFactory().create(Paths.get("dot/KDAJHDUYDJSKJ.dot"));
  }

  @Test
  public void simplestGraph() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/SimplestGraph.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(1));
    assertThat(context.getModel().findVertices("b").get(0).getName(), is("b"));
    assertThat(context.getModel().findVertices("b").get(0).getId(), is("b"));
    assertThat(context.getModel().findVertices("a").get(0).getName(), is("a"));
    assertThat(context.getModel().findVertices("a").get(0).getId(), is("a"));
    assertNotNull(context.getModel().getEdges().get(0).getId());
    assertThat(context.getModel().getEdges().get(0).getSourceVertex().getId(), is("a"));
    assertThat(context.getModel().getEdges().get(0).getTargetVertex().getId(), is("b"));
  }

  @Test
  public void g3v2e() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/3v2e.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(2));
  }

  @Test
  public void g3v2e_withEdgeLabel() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/3v2e_withEdgeLabel.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(2));
    assertThat(context.getModel().findVertices("b").get(0).getName(), is("b"));
    assertThat(context.getModel().findVertices("b").get(0).getId(), is("b"));
    assertThat(context.getModel().findVertices("c").get(0).getName(), is("c"));
    assertThat(context.getModel().findVertices("c").get(0).getId(), is("c"));
    assertThat(context.getModel().findEdges("e1").get(0).getId(), is("e1"));
    assertThat(context.getModel().findEdges("e1").get(0).getSourceVertex().getId(), is("a"));
    assertThat(context.getModel().findEdges("e1").get(0).getTargetVertex().getId(), is("b"));
    assertThat(context.getModel().findEdges("e2").get(0).getId(), is("e2"));
    assertThat(context.getModel().findEdges("e2").get(0).getSourceVertex().getId(), is("b"));
    assertThat(context.getModel().findEdges("e2").get(0).getTargetVertex().getId(), is("c"));
  }

  @Test
  public void simple3v2e() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/Simple3v2e.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(2));
    assertThat(context.getModel().findVertices("b").get(0).getName(), is("b"));
    assertThat(context.getModel().findVertices("b").get(0).getId(), is("b"));
    assertThat(context.getModel().findVertices("c").get(0).getName(), is("c"));
    assertThat(context.getModel().findVertices("c").get(0).getId(), is("c"));
    assertThat(context.getModel().findVertices("a").get(0).getName(), is("a"));
    assertThat(context.getModel().findVertices("a").get(0).getId(), is("a"));
    assertNull(context.getModel().getEdges().get(0).getName());
    assertNotNull(context.getModel().getEdges().get(0).getId());
    assertNull(context.getModel().getEdges().get(1).getName());
    assertNotNull(context.getModel().getEdges().get(1).getId());
  }

  @Test
  public void simple4v3e() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/Simple4v3e.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(4));
    assertThat(context.getModel().getEdges().size(), is(3));
  }

  @Test
  public void simplestGWGraph() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/SimplestGWGraph.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(1));
    assertThat(context.getModel().getEdges().size(), is(1));
    assertThat(context.getModel().getVertices().get(0).getName(), is("v1"));
    assertThat(context.getModel().getVertices().get(0).getId(), is("v1"));
    assertThat(context.getModel().getEdges().get(0).getName(), is("e1"));
    assertThat(context.getModel().getEdges().get(0).getId(), is("e1"));
  }

  @Test
  public void simpleGWGraph() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/SimpleGW.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(1));
    assertThat(context.getModel().getEdges().size(), is(1));
    assertThat(context.getModel().getVertices().get(0).getName(), is("v1"));
    assertThat(context.getModel().getVertices().get(0).getId(), is("n1"));
    assertThat(context.getModel().getEdges().get(0).getName(), is("e1"));
    assertThat(context.getModel().getEdges().get(0).getId(), is("e1"));
    assertNull(context.getModel().getEdges().get(0).getSourceVertex());
    assertThat(context.getModel().getEdges().get(0).getTargetVertex().getId(), is("n1"));
  }

  @Test
  public void gwLogin() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/Login.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(9));
  }

  @Test
  public void doubleQuote() throws IOException {
    List<Context> contexts = new DotContextFactory().create(Paths.get("dot/doubleQuote.dot"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));
    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(0));
    assertThat(context.getModel().getVertices().get(0).getName(), is("v1"));
    assertThat(context.getModel().getVertices().get(1).getName(), is("v2"));
    assertThat(context.getModel().getVertices().get(0).getId(), is("n1"));
    assertThat(context.getModel().getVertices().get(1).getId(), is("n2"));
  }
}
