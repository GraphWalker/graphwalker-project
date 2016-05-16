package org.graphwalker.core.machine;

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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Nils Olsson
 */
public class SharedStateTest {

  @Test
  public void singleSharedStates() {
    Vertex vertex = new Vertex().setName("A").setSharedState("CUSTOM_STATE");
    Edge edge = new Edge().setName("B").setSourceVertex(vertex).setTargetVertex(vertex);
    Model model = new Model().addEdge(edge);
    Context context1 = new TestExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
    context1.setNextElement(vertex);
    Context context2 = new TestExecutionContext(new Model().addVertex(new Vertex().setName("C").setSharedState("CUSTOM_STATE")), new RandomPath(new VertexCoverage(100)));
    Machine machine = new SimpleMachine(context1, context2);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getProfiler().getUnvisitedElements(context1).isEmpty(), is(true));
    assertThat(machine.getProfiler().getUnvisitedElements(context2).isEmpty(), is(true));
  }

  @Test
  public void multipleSharedStates() {
    Vertex shared1 = new Vertex().setName("A");
    Vertex shared2 = new Vertex().setName("B");
    Vertex shared3 = new Vertex().setName("E");
    Model model1 = new Model().addVertex(shared1.setSharedState("SHARED1")).addEdge(new Edge().setName("I").setSourceVertex(new Vertex().setName("H").setSharedState("SHARED3")).setTargetVertex(shared1));
    Model model2 = new Model().addVertex(shared2.setSharedState("SHARED1")).addEdge(new Edge().setName("C").setSourceVertex(shared2).setTargetVertex(new Vertex().setName("D").setSharedState("SHARED2")));
    Model model3 = new Model().addVertex(shared3.setSharedState("SHARED2")).addEdge(new Edge().setName("F").setSourceVertex(shared3).setTargetVertex(new Vertex().setName("G").setSharedState("SHARED3")));
    Context context1 = new TestExecutionContext(model1, new RandomPath(new EdgeCoverage(100))).setNextElement(shared1);
    Context context2 = new TestExecutionContext(model2, new RandomPath(new VertexCoverage(100)));
    Context context3 = new TestExecutionContext(model3, new RandomPath(new VertexCoverage(100)));
    Machine machine = new SimpleMachine(context1, context2, context3);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getProfiler().getUnvisitedElements(context1).isEmpty(), is(true));
    assertThat(machine.getProfiler().getUnvisitedElements(context2).isEmpty(), is(true));
    assertThat(machine.getProfiler().getUnvisitedElements(context3).isEmpty(), is(true));
    List<String> names = new ArrayList<>();
    for (Element element : machine.getProfiler().getPath()) {
      names.add(element.getName());
    }
    assertArrayEquals(names.toArray(), Arrays.asList("A", "I", "H", "G", "F", "E", "D", "C", "B", "A").toArray());
  }

  @Test
  public void issue7() {
    Vertex v_HomePage = new Vertex().setName("v_HomePage").setSharedState("HomePage");
    Vertex v_FindOwners = new Vertex().setName("v_FindOwners").setSharedState("FindOwners");
    Vertex v_Veterinarians = new Vertex().setName("v_Veterinarians").setSharedState("Veterinarians");

    Model modelPetClinic = new Model()
      .addVertex(v_HomePage)
      .addVertex(v_FindOwners)
      .addVertex(v_Veterinarians)
      .addEdge(new Edge().setName("e_HomePage").setSourceVertex(v_FindOwners).setTargetVertex(v_HomePage))
      .addEdge(new Edge().setName("e_HomePage").setSourceVertex(v_Veterinarians).setTargetVertex(v_HomePage))
      .addEdge(new Edge().setName("e_FindOwners").setSourceVertex(v_HomePage).setTargetVertex(v_FindOwners))
      .addEdge(new Edge().setName("e_FindOwners").setSourceVertex(v_Veterinarians).setTargetVertex(v_FindOwners))
      .addEdge(new Edge().setName("e_Veterinarians").setSourceVertex(v_HomePage).setTargetVertex(v_Veterinarians))
      .addEdge(new Edge().setName("e_Veterinarians").setSourceVertex(v_FindOwners).setTargetVertex(v_Veterinarians));


    Vertex v_Veterinarians_ = new Vertex().setName("v_Veterinarians").setSharedState("Veterinarians");
    Vertex v_SearchResult = new Vertex().setName("v_SearchResult");

    Model modelVeterinarians = new Model()
      .addVertex(v_Veterinarians_)
      .addVertex(v_SearchResult)
      .addEdge(new Edge().setName("e_Search").setSourceVertex(v_Veterinarians_).setTargetVertex(v_SearchResult))
      .addEdge(new Edge().setSourceVertex(v_SearchResult).setTargetVertex(v_Veterinarians_));


    Context contextPetClinic = new TestExecutionContext(modelPetClinic, new RandomPath(new EdgeCoverage(100))).setNextElement(v_HomePage);
    Context contextVeterinarians = new TestExecutionContext(modelVeterinarians, new RandomPath(new EdgeCoverage(100)));

    Machine machine = new SimpleMachine(contextPetClinic, contextVeterinarians);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
    assertThat(machine.getProfiler().getUnvisitedElements(contextPetClinic).isEmpty(), is(true));
    assertThat(machine.getProfiler().getUnvisitedElements(contextVeterinarians).isEmpty(), is(true));

  }

  @Test
  public void accessAttribute() {
    Vertex shared1 = new Vertex().setName("A");
    Vertex shared2 = new Vertex().setName("B");
    Model model1 = new Model()
      .addVertex(shared1)
      .addEdge(new Edge()
        .setName("I")
        .addAction(new Action("global.put('myVariable', true)"))
        .setSourceVertex(shared1)
        .setTargetVertex(new Vertex()
          .setName("H")
          .setSharedState("SHARED1")));
    Model model2 = new Model()
      .addVertex(shared2.setSharedState("SHARED1"))
      .addEdge(new Edge()
        .setName("C")
        .setGuard(new Guard("global.get('myVariable')"))
        .setSourceVertex(shared2)
        .setTargetVertex(new Vertex()
          .setName("D")));
    Context context1 = new TestExecutionContext(model1, new RandomPath(new EdgeCoverage(100))).setNextElement(shared1);
    Context context2 = new TestExecutionContext(model2, new RandomPath(new VertexCoverage(100)));
    Machine machine = new SimpleMachine(context1, context2);
    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }
}
