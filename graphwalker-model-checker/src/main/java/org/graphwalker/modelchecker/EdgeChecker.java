package org.graphwalker.modelchecker;

import com.google.common.base.CharMatcher;
import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.model.Edge;

/**
 * Created by krikar on 2015-11-08.
 */
public class EdgeChecker {

  private EdgeChecker() {
  }

  /**
   * Checks the edge for problems or any possible errors.
   * Any findings will be added to a list of strings.
   * <p/>
   * TODO: Implement a rule framework so that organisations and projects can create their own rule set (think model based code convention)
   *
   * @return A list of issues found in the edge
   */
  static public List<String> hasIssues(Edge.RuntimeEdge edge) {
    List<String> issues = new ArrayList<>(ElementChecker.hasIssues(edge));

    if (edge.getTargetVertex() == null) {
      issues.add("Edge must have a target vertex.");
    }

    if (edge.hasName() && CharMatcher.whitespace().matchesAnyOf(edge.getName())) {
      issues.add("Name of edge cannot have any white spaces.");
    }

    if (edge.getWeight() < 0 || edge.getWeight() > 1) {
      issues.add("The weight must be a value between 0 and 1.");
    }

    return issues;
  }
}
