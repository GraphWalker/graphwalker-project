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

import org.graphwalker.core.machine.Context;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;

/**
 * @author Kristian Karl
 */
public class DotContextFactoryTest {

  @Test
  public void SimplestGraph() {
    Context context = new DotContextFactory().create(Paths.get("dot/SimplestGraph.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(2));
    Assert.assertThat(context.getModel().getEdges().size(), is(1));

    Assert.assertThat(context.getModel().findVertices("b").get(0).getName(), is("b"));
    Assert.assertThat(context.getModel().findVertices("b").get(0).getId(), is("b"));

    Assert.assertThat(context.getModel().findVertices("a").get(0).getName(), is("a"));
    Assert.assertThat(context.getModel().findVertices("a").get(0).getId(), is("a"));

    Assert.assertNotNull(context.getModel().getEdges().get(0).getId());
    Assert.assertThat(context.getModel().getEdges().get(0).getSourceVertex().getId(), is("a"));
    Assert.assertThat(context.getModel().getEdges().get(0).getTargetVertex().getId(), is("b"));
  }

  @Test
  public void g3v2e() {
    Context context = new DotContextFactory().create(Paths.get("dot/3v2e.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(3));
    Assert.assertThat(context.getModel().getEdges().size(), is(2));
  }

  @Test
  public void g3v2e_withEdgeLabel() {
    Context context = new DotContextFactory().create(Paths.get("dot/3v2e_withEdgeLabel.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(3));
    Assert.assertThat(context.getModel().getEdges().size(), is(2));


    Assert.assertThat(context.getModel().findVertices("b").get(0).getName(), is("b"));
    Assert.assertThat(context.getModel().findVertices("b").get(0).getId(), is("b"));

    Assert.assertThat(context.getModel().findVertices("c").get(0).getName(), is("c"));
    Assert.assertThat(context.getModel().findVertices("c").get(0).getId(), is("c"));

    Assert.assertThat(context.getModel().findEdges("e1").get(0).getId(), is("e1"));
    Assert.assertThat(context.getModel().findEdges("e1").get(0).getSourceVertex().getId(), is("a"));
    Assert.assertThat(context.getModel().findEdges("e1").get(0).getTargetVertex().getId(), is("b"));

    Assert.assertThat(context.getModel().findEdges("e2").get(0).getId(), is("e2"));
    Assert.assertThat(context.getModel().findEdges("e2").get(0).getSourceVertex().getId(), is("b"));
    Assert.assertThat(context.getModel().findEdges("e2").get(0).getTargetVertex().getId(), is("c"));
  }

  @Test
  public void Simple3v2e() {
    Context context = new DotContextFactory().create(Paths.get("dot/Simple3v2e.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(3));
    Assert.assertThat(context.getModel().getEdges().size(), is(2));

    Assert.assertThat(context.getModel().findVertices("b").get(0).getName(), is("b"));
    Assert.assertThat(context.getModel().findVertices("b").get(0).getId(), is("b"));

    Assert.assertThat(context.getModel().findVertices("c").get(0).getName(), is("c"));
    Assert.assertThat(context.getModel().findVertices("c").get(0).getId(), is("c"));

    Assert.assertThat(context.getModel().findVertices("a").get(0).getName(), is("a"));
    Assert.assertThat(context.getModel().findVertices("a").get(0).getId(), is("a"));

    Assert.assertNull(context.getModel().getEdges().get(0).getName());
    Assert.assertNotNull(context.getModel().getEdges().get(0).getId());

    Assert.assertNull(context.getModel().getEdges().get(1).getName());
    Assert.assertNotNull(context.getModel().getEdges().get(1).getId());
  }


  @Test
  public void Simple4v3e() {
    Context context = new DotContextFactory().create(Paths.get("dot/Simple4v3e.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(4));
    Assert.assertThat(context.getModel().getEdges().size(), is(3));
  }

  @Test
  public void SimplestGWGraph() {
    Context context = new DotContextFactory().create(Paths.get("dot/SimplestGWGraph.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(1));
    Assert.assertThat(context.getModel().getEdges().size(), is(1));

    Assert.assertThat(context.getModel().getVertices().get(0).getName(), is("v1"));
    Assert.assertThat(context.getModel().getVertices().get(0).getId(), is("v1"));

    Assert.assertThat(context.getModel().getEdges().get(0).getName(), is("e1"));
    Assert.assertThat(context.getModel().getEdges().get(0).getId(), is("e1"));
  }

  @Test
  public void SimpleGWGraph() {
    Context context = new DotContextFactory().create(Paths.get("dot/SimpleGW.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(1));
    Assert.assertThat(context.getModel().getEdges().size(), is(1));

    Assert.assertThat(context.getModel().getVertices().get(0).getName(), is("v1"));
    Assert.assertThat(context.getModel().getVertices().get(0).getId(), is("n1"));

    Assert.assertThat(context.getModel().getEdges().get(0).getName(), is("e1"));
    Assert.assertThat(context.getModel().getEdges().get(0).getId(), is("e1"));

    Assert.assertNull(context.getModel().getEdges().get(0).getSourceVertex());
    Assert.assertThat(context.getModel().getEdges().get(0).getTargetVertex().getId(), is("n1"));
  }

  @Test
  public void GW_Login() {
    Context context = new DotContextFactory().create(Paths.get("dot/Login.dot"));
    Assert.assertThat(context.getModel().getVertices().size(), is(3));
    Assert.assertThat(context.getModel().getEdges().size(), is(9));
  }
}
