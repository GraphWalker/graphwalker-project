package org.graphwalker.core.example;

/*-
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ObjectMirrorTest extends ExecutionContext {

  private List<String> values = new ArrayList<>();

  public boolean contains(String value) {
    return values.contains(value);
  }

  public void add(String value) {
    values.add(value);
  }

  @Test
  public void verifyMirror() throws Exception {
    Vertex start = new Vertex();
    Model model = new Model()
        .addEdge(new Edge().setName("edge1")
                     .setGuard(new Guard("ObjectMirrorTest.contains('value1')"))
                     .setSourceVertex(start.setName("vertex1"))
                     .setTargetVertex(new Vertex().setName("vertex2")))
        .addEdge(new Edge()
                     .setGuard(new Guard("!ObjectMirrorTest.contains('value1')"))
                     .setSourceVertex(start)
                     .setTargetVertex(start)
                     .addAction(new Action("ObjectMirrorTest.add('value1')")));
    this.setModel(model.build());
    this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
    setNextElement(start);
    Machine machine = new SimpleMachine(this);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }
}
