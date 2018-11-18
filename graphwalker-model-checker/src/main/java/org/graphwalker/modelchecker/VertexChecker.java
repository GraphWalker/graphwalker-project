package org.graphwalker.modelchecker;

import com.google.common.base.CharMatcher;
import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.model.Vertex;

/**
 * Created by krikar on 2015-11-08.
 */
public class VertexChecker {

  private VertexChecker() {
  }

  /**
   * Checks the vertex for problems or any possible errors.
   * Any findings will be added to a list of strings.
   * <p/>
   * TODO: Implement a rule framework so that organisations and projects can create their own rule set (think model based code convention)
   *
   * @return A list of issues found in the vertex
   */
  static public List<String> hasIssues(Vertex.RuntimeVertex vertex) {
    List<String> issues = new ArrayList<>(ElementChecker.hasIssues(vertex));

    if (vertex.getName() == null) {
      issues.add("Name of vertex cannot be null");
    } else {
      if (vertex.getName().isEmpty()) {
        issues.add("Name of vertex cannot be an empty string");
      }
      if (CharMatcher.whitespace().matchesAnyOf(vertex.getName())) {
        issues.add("Name of vertex cannot have any white spaces.");
      }
    }
    return issues;
  }
}
