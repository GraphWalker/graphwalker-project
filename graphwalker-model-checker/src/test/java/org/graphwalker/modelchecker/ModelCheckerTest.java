package org.graphwalker.modelchecker;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;

import java.util.List;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-08.
 */
public class ModelCheckerTest {

  @Test
  public void testDefault() {
    List<String> issues = ModelChecker.hasIssues(new Model().build());
    Assert.assertThat(issues.size(), is(0));
  }

  @Test
  public void testInvalidElement() {
    Model model = new Model();
    model.addVertex(new Vertex());
    List<String> issues = ModelChecker.hasIssues(model.build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Name of vertex cannot be null"));
  }

  @Test
  public void testNotUniqueElementIds() {
    Model model = new Model();
    model.addVertex(new Vertex().setId("NOTUNIQUEID").setName("SomeName"));
    model.addVertex(new Vertex().setId("NOTUNIQUEID").setName("SomeOtherName"));
    List<String> issues = ModelChecker.hasIssues(model.build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Id of the vertex is not unique: NOTUNIQUEID"));
  }

  @Test
  public void testUnnamedSelfLoop() {
    Model model = new Model();
    Vertex vertex = new Vertex().setName("SomeName").setId("SomeId");
    model.addVertex(vertex).addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
    List<String> issues = ModelChecker.hasIssues(model.build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), containsString(", have a unnamed self loop edge."));
  }
}
