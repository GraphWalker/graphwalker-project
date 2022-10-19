package org.graphwalker.modelchecker;

import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-11-08.
 */
public class VertexCheckerTest {

  @Test
  public void testDefault() {
    List<String> issues = VertexChecker.hasIssues(new Vertex().build());
    assertThat(issues.size(), is(1));
    assertThat(issues.get(0), is("Name of vertex cannot be null"));

    issues = VertexChecker.hasIssues(new Vertex().setName("name").build());
    assertThat(issues.size(), is(0));
  }

  @Test
  public void testName() {
    Vertex vertex = new Vertex();
    List<String> issues = VertexChecker.hasIssues(vertex.setName("").build());
    assertThat(issues.size(), is(1));
    assertThat(issues.get(0), is("Name of vertex cannot be an empty string"));

    issues = VertexChecker.hasIssues(vertex.setName("spaces in name").build());
    assertThat(issues.size(), is(1));
    assertThat(issues.get(0), is("Name of vertex cannot have any white spaces."));
  }
}
