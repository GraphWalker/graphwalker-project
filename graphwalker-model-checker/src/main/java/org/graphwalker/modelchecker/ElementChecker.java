package org.graphwalker.modelchecker;

import java.util.ArrayList;
import java.util.List;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;

/**
 * Created by krikar on 2015-11-08.
 */
public class ElementChecker {

  private ElementChecker() {
  }

  /**
   * Checks for problems or any possible errors.
   * Any findings will be added to a list of strings.
   * <p/>
   * TODO: Implement a rule framework so that organisations and projects can create their own rule set (think model based code convention)
   *
   * @return A list of issues found
   */
  static public List<String> hasIssues(Element element) {
    List<String> issues = new ArrayList<>();

    if (element.getId() == null) {
      issues.add("Id cannot be null");
    }
    if (element.hasRequirements()) {
      for (Requirement requirement : element.getRequirements()) {
        if (requirement.getKey().isEmpty()) {
          issues.add("Requirement cannot be an empty string");
        }
      }
    }
    if (element.hasActions()) {
      for (Action action : element.getActions()) {
        if (action.getScript().isEmpty()) {
          issues.add("Script statement cannot be an empty string");
        }
      }
    }

    return issues;
  }
}
