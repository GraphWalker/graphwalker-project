package org.graphwalker.core.machine;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class AccessModelTest {

  @Test
  public void read() {
    ExecutionContext context = createContext();
    assertThat(round(context.getAttribute("x")), is(1));
  }

  private int round(Object value) {
    if (value instanceof Double) {
      return (int) Math.round((Double) value);
    } else {
      return (Integer) value;
    }
  }

  @Test
  public void write() {
    ExecutionContext context = createContext();
    context.setAttribute("y", 2);
    assertThat((Integer) context.getAttribute("y"), is(2));
  }

  private ExecutionContext createContext() {
    Model model = new Model();
    model.addEdge(new Edge()
                      .setSourceVertex(new Vertex())
                      .setTargetVertex(new Vertex()));
    ExecutionContext context = new TestExecutionContext(model, new ShortestAllPaths(new VertexCoverage(100)));
    context.execute(new Action("x = 1;"));
    return context;
  }
}
