package org.graphwalker.modelchecker;

import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Vertex;

/**
 * Created by krikar on 2015-11-08.
 */
public class ContextChecker {

  private ContextChecker() {
  }

  /**
   * Checks the context for problems or any possible errors.
   * Any findings will be added to a list of strings.
   * <p/>
   * TODO: Implement a rule framework so that organisations and projects can create their own rule set (think model based code convention)
   *
   * @return A list of issues found in the context
   */
  static public List<String> hasIssues(Context context) {
    List<String> issues = new ArrayList<>();

    if (context.getModel() == null) {
      issues.add("No model found in context");
      return issues;
    }

    // Check the model
    issues.addAll(ModelChecker.hasIssues(context.getModel()));

    // Check for start element (or shared state)
    if (context.getNextElement() == null && !context.getModel().hasSharedStates()) {
      issues.add("The model has neither a start element or a defined shared state.");
      return issues;
    }

    // Check for a non-strongly connected graph and in combination with
    // random generator with full edge coverage.
    if (context.getPathGenerator() instanceof RandomPath) {
      if (context.getPathGenerator().getStopCondition() instanceof EdgeCoverage) {
        EdgeCoverage edgeCoverage = (EdgeCoverage) context.getPathGenerator().getStopCondition();
        if (edgeCoverage.getPercent() == 100) {
          int countNumOfCulDeSac = 0;
          for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
            if (context.getModel().getOutEdges(vertex).size() == 0) {

              // Check if the vertex with no out-edges is a shared vertex.
              // if so, it shall not to be calculated as a cul-de-sac
              if (!vertex.hasSharedState()) {
                countNumOfCulDeSac++;
              }
            }
          }
          if (countNumOfCulDeSac > 1) {
            issues.add("The model has multiple cul-de-sacs, and is requested to run using a random " +
                       "path generator and 100% edge coverage. That will not work.");
          } else if (countNumOfCulDeSac == 1) {
            issues.add("The model has one cul-de-sacs, and is requested to run using a random " +
                       "path generator and 100% edge coverage. That might not work.");
          }
        }
      }
    }

    return issues;
  }
}
