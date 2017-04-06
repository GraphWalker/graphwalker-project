package org.graphwalker.modelchecker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

/**
 * Created by krikar on 2015-11-08.
 */
public class ContextsChecker {

  /**
   * Checks the context for problems or any possible errors.
   * Any findings will be added to a list of strings.
   * <p/>
   * TODO: Implement a rule framework so that organisations and projects can create their own rule set (think model based code convention)
   *
   * @return A list of issues found in the context
   */
  static public List<String> hasIssues(List<Context> contexts) {
    List<String> issues = new ArrayList<>();

    // Check that individual contexts are valid
    for (Context context : contexts) {
      issues.addAll(ContextChecker.hasIssues(context));
    }

    // Check that ids are unique
    Set<String> ids = new HashSet<>();
    for (Context context : contexts) {
      if (!ids.add(context.getModel().getId())) {
        issues.add("Id of the model is not unique: " + context.getModel().getId());
      }
    }

    // Check that all internal ids are unique
    Set<Element> elements = new HashSet<>();
    for (Context context : contexts) {
      if (!elements.add(context.getModel())) {
        issues.add("Internal id of the model is not unique: " + context);
      }
      for (Element element : context.getModel().getElements()) {
        if (!elements.add(element)) {
          issues.add("Internal id of the element is not unique: " + element);
        }
      }
    }

    return issues;
  }
}
