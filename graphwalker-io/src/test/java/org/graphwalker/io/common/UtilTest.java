package org.graphwalker.io.common;

/*
 * #%L
 * GraphWalker Input/Output
 * %%
 * Copyright (C) 2005 - 2015 GraphWalker
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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.io.factory.yed.YEdContextFactory;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-04.
 */
public class UtilTest {

  @Test
  public void filterBlockedElementsGrapmlVertex1() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex1.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(4));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(2));
  }

  @Test
  public void filterBlockedElementsGrapmlVertex2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex2.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(4));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(1));
  }

  @Test
  public void filterBlockedElementsGrapmlVertex3() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex3.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(3));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(1));
  }

  @Test
  public void filterBlockedElementsGrapmlBranch1() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedBranch1.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(4));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(3));
  }

  @Test
  public void filterBlockedElementsGrapmlBranch2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedBranch2.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(5));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(3));
    assertThat(context.getModel().getEdges().size(), is(4));
  }

  @Test
  public void filterBlockedElementsGrapmlSingleEdge() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/singleEdge.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(3));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(2));
  }

  @Test
  public void filterBlockedElementsGrapmlSingleVertex() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(2));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(1));
    assertThat(context.getModel().getEdges().size(), is(1));
  }


  @Test
  public void filterBlockedElementsGrapmlSingleVertex2() throws IOException {
    List<Context> contexts = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex2.graphml"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(2));
    assertThat(context.getModel().getEdges().size(), is(3));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(1));
    assertThat(context.getModel().getEdges().size(), is(1));
  }

  @Test
  public void filterBlockedElementsJson() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/graphWithBlockedElements.json"));
    assertNotNull(contexts);
    assertThat(contexts.size(), is(1));

    Context context = contexts.get(0);
    assertThat(context.getModel().getVertices().size(), is(5));
    assertThat(context.getModel().getEdges().size(), is(10));

    Util.filterBlockedElements(contexts);
    assertThat(context.getModel().getVertices().size(), is(4));
    assertThat(context.getModel().getEdges().size(), is(7));
  }
}
