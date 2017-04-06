package org.graphwalker.modelchecker;

import static org.hamcrest.core.Is.is;

import java.util.List;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-08.
 */
public class VertexCheckerTest {

  @Test
  public void testDefault() {
    List<String> issues = VertexChecker.hasIssues(new Vertex().build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Name of vertex cannot be null"));

    issues = VertexChecker.hasIssues(new Vertex().setName("name").build());
    Assert.assertThat(issues.size(), is(0));
  }

  @Test
  public void testName() {
    Vertex vertex = new Vertex();
    List<String> issues = VertexChecker.hasIssues(vertex.setName("").build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Name of vertex cannot be an empty string"));

    issues = VertexChecker.hasIssues(vertex.setName("spaces in name").build());
    Assert.assertThat(issues.size(), is(1));
    Assert.assertThat(issues.get(0), is("Name of vertex cannot have any white spaces."));
  }
}
