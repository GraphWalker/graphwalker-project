package org.graphwalker.modelchecker;

import static org.hamcrest.core.Is.is;

import java.util.List;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-08.
 */
public class EdgeCheckerTest {

  @Test
  public void testDefault() {
    List<String> issues = EdgeChecker.hasIssues(new Edge().build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Edge must have a target vertex."));

    issues = EdgeChecker.hasIssues(new Edge().setTargetVertex(new Vertex()).build());
    Assert.assertThat(issues.size(), is(0));
  }

  @Test
  public void testName() {
    Edge edge = new Edge().setTargetVertex(new Vertex());
    List<String> issues = EdgeChecker.hasIssues(edge.setName("").build());
    Assert.assertThat(issues.size(), is(0));

    issues = EdgeChecker.hasIssues(edge.setName("spaces in name").build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Name of edge cannot have any white spaces."));
  }

  @Test
  public void testWeight() {
    Edge edge = new Edge().setTargetVertex(new Vertex());
    edge.setWeight(-1.);
    List<String> issues = EdgeChecker.hasIssues(edge.setName("").build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("The weight must be a value between 0 and 1."));

    edge.setWeight(.5);
    issues = EdgeChecker.hasIssues(edge.setName("").build());
    Assert.assertThat(issues.size(), is(0));
  }
}
