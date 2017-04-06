package org.graphwalker.modelchecker;

import static org.hamcrest.core.Is.is;

import java.util.List;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-08.
 */
public class ElementCheckerTest {

  @Test
  public void testDefault() {
    List<String> issues = ElementChecker.hasIssues(new Vertex().build());
    Assert.assertThat(issues.size(), is(0));
  }

  @Test
  public void testRequirement() {
    Vertex vertex = new Vertex().addRequirement(new Requirement(""));
    List<String> issues = ElementChecker.hasIssues(vertex.build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Requirement cannot be an empty string"));
  }

  @Test
  public void testActions() {
    Edge edge = new Edge().addAction(new Action(""));
    List<String> issues = ElementChecker.hasIssues(edge.build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Script statement cannot be an empty string"));
  }
}
