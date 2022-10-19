package org.graphwalker.modelchecker;

import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-11-08.
 */
public class ElementCheckerTest {

  @Test
  public void testDefault() {
    List<String> issues = ElementChecker.hasIssues(new Vertex().build());
    assertThat(issues.size(), is(0));
  }

  @Test
  public void testRequirement() {
    Vertex vertex = new Vertex().addRequirement(new Requirement(""));
    List<String> issues = ElementChecker.hasIssues(vertex.build());
    assertThat(issues.size(), is(1));
    assertThat(issues.get(0), is("Requirement cannot be an empty string"));
  }

  @Test
  public void testActions() {
    Edge edge = new Edge().addAction(new Action(""));
    List<String> issues = ElementChecker.hasIssues(edge.build());
    assertThat(issues.size(), is(1));
    assertThat(issues.get(0), is("Script statement cannot be an empty string"));
  }
}
