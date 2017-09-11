package org.graphwalker.core.algorithm;

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

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Path;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class FleuryTest {

  private static final Vertex v1 = new Vertex().setName("v1");
  private static final Vertex v2 = new Vertex().setName("v2");
  private static final Vertex v3 = new Vertex().setName("v3");
  private static final Vertex v4 = new Vertex().setName("v4");
  private static final Vertex v5 = new Vertex().setName("v5");
  private static final Vertex v6 = new Vertex().setName("v6");

  private static final Edge e1 = new Edge().setName("e1").setSourceVertex(v1).setTargetVertex(v2);
  private static final Edge e2 = new Edge().setName("e2").setSourceVertex(v2).setTargetVertex(v3);
  private static final Edge e3 = new Edge().setName("e3").setSourceVertex(v3).setTargetVertex(v1);
  private static final Edge e4 = new Edge().setName("e4").setSourceVertex(v1).setTargetVertex(v4);
  private static final Edge e5 = new Edge().setName("e5").setSourceVertex(v4).setTargetVertex(v5);
  private static final Edge e6 = new Edge().setName("e6").setSourceVertex(v5).setTargetVertex(v6);
  private static final Edge e7 = new Edge().setName("e7").setSourceVertex(v6).setTargetVertex(v4);

  private static final Model model = new Model()
      .addEdge(e1)
      .addEdge(e2)
      .addEdge(e3)
      .addEdge(e4)
      .addEdge(e5)
      .addEdge(e6)
      .addEdge(e7);


  private static final Path<Element> expectedPath = new Path<>(Arrays.<Element>asList(
      e1.build(), v2.build(), e2.build(), v3.build(), e3.build(), v1.build()
      , e4.build(), v4.build(), e5.build(), v5.build(), e6.build(), v6.build(), e7.build(), v4.build()
  ));

  @Test
  public void findTrail() throws Exception {
    Context context = new TestExecutionContext(model, null);
    Fleury fleury = new Fleury(context);
    Path<Element> path = fleury.getTrail(v1.build());
    assertArrayEquals(expectedPath.toArray(), path.toArray());
  }

}
