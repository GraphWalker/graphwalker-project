package org.graphwalker.restful;

/*
 * #%L
 * GraphWalker Command Line Interface
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.graalvm.polyglot.Value;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.RuntimeBase;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper class to create output in different formats, like JSON or plain text.
 * Created by krikar on 9/13/14.
 */
public abstract class Util {

  /**
   * Will create a JSON formatted string representing the current step. The step
   * is the current element, which can be either a vertex orn an edge.
   *
   * @param verbose       Print more details if true
   * @param showUnvisited Print all unvisited elements if true
   * @return The JSON string representing the current step.
   */
  public static JSONObject getStepAsJSON(Machine machine, boolean verbose, boolean showUnvisited) {
    JSONObject object = new JSONObject();
    if (verbose) {
      object.put("modelName", FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName()));
    }
    if (machine.getCurrentContext().getCurrentElement().hasName()) {
      object.put("currentElementName", machine.getCurrentContext().getCurrentElement().getName());
    } else {
      object.put("currentElementName", "");
    }
    if (verbose) {
      object.put("currentElementID", machine.getCurrentContext().getCurrentElement().getId());

      Value bindings = machine.getCurrentContext().getExecutionEnvironment().getBindings("js");
      JSONArray jsonKeys = new JSONArray();
      for (String key : bindings. getMemberKeys() ) {
        JSONObject jsonKey = new JSONObject();
        jsonKey.put(key, bindings.getMember(key));
        jsonKeys.put(jsonKey);
      }
      object.put("data", jsonKeys);

      JSONArray jsonProperties = new JSONArray();
      RuntimeBase runtimeBase = (RuntimeBase) machine.getCurrentContext().getCurrentElement();
      for (Map.Entry<String, Object> key : runtimeBase.getProperties().entrySet()) {
        JSONObject jsonKey = new JSONObject();
        jsonKey.put(key.getKey(), key.getValue());
        jsonProperties.put(jsonKey);
      }
      object.put("properties", jsonProperties);

      JSONArray jsonActions = new JSONArray();
      if (runtimeBase.hasActions()) {
        for (Action action : runtimeBase.getActions()) {
          JSONObject jsonAction = new JSONObject();
          jsonAction.put("Action", action.getScript());
          jsonActions.put(jsonAction);
        }
        object.put("actions", jsonActions);
      }
    }
    if (showUnvisited) {
      Context context = machine.getCurrentContext();
      object.put("numberOfElements", context.getModel().getElements().size());
      object.put("numberOfUnvisitedElements", context.getProfiler().getUnvisitedElements(context).size());

      JSONArray jsonElements = new JSONArray();
      for (Element element : context.getProfiler().getUnvisitedElements(context)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("elementName", element.getName());
        if (verbose) {
          jsonElement.put("elementId", element.getId());
        }
        jsonElements.put(jsonElement);
      }
      object.put("unvisitedElements", jsonElements);
    }
    return object;
  }
}
