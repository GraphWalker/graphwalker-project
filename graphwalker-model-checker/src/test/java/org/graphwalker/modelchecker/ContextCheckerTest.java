package org.graphwalker.modelchecker;

import static org.hamcrest.core.Is.is;

import java.util.List;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.factory.json.JsonContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-08.
 */
public class ContextCheckerTest {

  @Test
  public void testDefault() {
    Context context = new JsonContext();
    List<String> issues = ContextChecker.hasIssues(context);
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("No model found in context"));

    Model model = new Model();
    context.setModel(model.build());
    issues = ContextChecker.hasIssues(context);
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("The model has neither a start element or a defined shared state."));

    Vertex v1 = new Vertex().setName("v1").setId("v1");
    Vertex v2 = new Vertex().setName("v2").setId("v2");
    model.addVertex(v1).addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));
    context.setNextElement(v1);
    context.setModel(model.build());
    issues = ContextChecker.hasIssues(context);
    Assert.assertThat(issues.size(), is(0));
  }

  @Test
  public void testRandomGeneratorFullEdgeCoverageNonStronglyConnectedGraph() {
    Vertex v1 = new Vertex().setName("v1").setId("v1");
    Vertex v2 = new Vertex().setName("v2").setId("v2");
    Vertex v3 = new Vertex().setName("v3").setId("v3");
    Vertex v4 = new Vertex().setName("v4").setId("v4");

    Model model = new Model();
    model.addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1").setId("e1"));
    model.addEdge(new Edge().setSourceVertex(v2).setTargetVertex(v3).setName("e2").setId("e2"));
    model.addEdge(new Edge().setSourceVertex(v2).setTargetVertex(v4).setName("e3").setId("e3"));
    model.addEdge(new Edge().setTargetVertex(v1).setName("e0").setId("e0"));

    Context context = new JsonContext();
    context.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
    context.setNextElement(context.getModel().findElements("e0").get(0));

    List<String> issues = ContextChecker.hasIssues(context);
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("The model has multiple cul-de-sacs, and is requested to run using a random " +
                                        "path generator and 100% edge coverage. That will not work."));

    model.addEdge(new Edge().setSourceVertex(v4).setTargetVertex(v2).setName("e4").setId("e4"));
    context = new JsonContext();
    context.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
    context.setNextElement(context.getModel().findElements("e0").get(0));

    issues = ContextChecker.hasIssues(context);
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("The model has one cul-de-sacs, and is requested to run using a random " +
                                        "path generator and 100% edge coverage. That might not work."));
  }
}
