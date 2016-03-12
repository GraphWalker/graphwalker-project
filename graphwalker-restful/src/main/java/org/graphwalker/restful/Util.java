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

import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.RequirementStatus;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.RuntimeBase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to create output in different formats, like JSON or plain text.
 * Created by krikar on 9/13/14.
 */
public abstract class Util {

  public enum Statistics {
    TOTAL_NUMBER_OF_VERTICES,
    TOTAL_NUMBER_OF_UNVISITED_VERTICES,
    TOTAL_NUMBER_OF_EDGES,
    TOTAL_NUMBER_OF_UNVISITED_EDGES,
    TOTAL_NUMBER_OF_REQUIREMENTS,
    TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED,
    TOTAL_NUMBER_OF_REQUIREMENTS_PASSED,
    TOTAL_NUMBER_OF_REQUIREMENTS_FAILED
  }

  /**
   * Will create a JSON formatted string representing the current step. The step
   * is the current element, which can be either a vertex orn an edge.
   *
   * @param machine
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
      if (verbose) {
        object.put("currentElementID", machine.getCurrentContext().getCurrentElement().getId());

        JSONArray jsonKeys = new JSONArray();
        for (Map.Entry<String, String> key : machine.getCurrentContext().getKeys().entrySet()) {
          JSONObject jsonKey = new JSONObject();
          jsonKey.put(key.getKey(), key.getValue());
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

  /**
   * Will create a JSON formatted string representing the statistics of the current
   * execution of the machine.
   *
   * @param machine
   * @return The execution statistics in JSON format.
   */
  public static JSONObject getStatisticsAsJSON(Machine machine) {
    EnumMap<Statistics, Integer> map = getStatistics(machine.getCurrentContext());
    JSONObject object = new JSONObject();
    object.put("totalNumberOfEdges", map.get(Statistics.TOTAL_NUMBER_OF_EDGES));
    object.put("totalNumberOfUnvisitedEdges", map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES));
    object.put("totalNumberOfVisitedEdges", map.get(Statistics.TOTAL_NUMBER_OF_EDGES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES));
    object.put("edgeCoverage", 100 * (map.get(Statistics.TOTAL_NUMBER_OF_EDGES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES)) / map.get(Statistics.TOTAL_NUMBER_OF_EDGES));
    object.put("totalNumberOfVertices", map.get(Statistics.TOTAL_NUMBER_OF_VERTICES));
    object.put("totalNumberOfUnvisitedVertices", map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES));
    object.put("totalNumberOfVisitedVertices", map.get(Statistics.TOTAL_NUMBER_OF_VERTICES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES));
    object.put("vertexCoverage", 100 * (map.get(Statistics.TOTAL_NUMBER_OF_VERTICES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES)) / map.get(Statistics.TOTAL_NUMBER_OF_VERTICES));

    if (map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) > 0) {
      object.put("totalNumberOfRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS));
      object.put("totalNumberOfUncoveredRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED));
      object.put("totalNumberOfPassedRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_PASSED));
      object.put("totalNumberOfFailedRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_FAILED));
      object.put("requirementCoverage", 100 * (map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) - map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED)) / map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS));

      JSONArray jsonElements = new JSONArray();
      for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.NOT_COVERED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("requirementKey", r.getKey());
        jsonElements.put(jsonElement);
      }
      object.put("requirementsNotCovered", jsonElements);

      jsonElements = new JSONArray();
      for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.PASSED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("requirementKey", r.getKey());
        jsonElements.put(jsonElement);
      }
      object.put("requirementsPassed", jsonElements);

      jsonElements = new JSONArray();
      for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.FAILED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("RequirementKey", r.getKey());
        jsonElements.put(jsonElement);
      }
      object.put("requirementsFailed", jsonElements);
    }
    return object;
  }

  private static EnumMap<Statistics, Integer> getStatistics(Context context) {
    EnumMap<Statistics, Integer> map = new EnumMap<>(Statistics.class);
    map.put(Statistics.TOTAL_NUMBER_OF_VERTICES, context.getModel().getVertices().size());
    map.put(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES, context.getProfiler().getUnvisitedVertices(context).size());
    map.put(Statistics.TOTAL_NUMBER_OF_EDGES, context.getModel().getEdges().size());
    map.put(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES, context.getProfiler().getUnvisitedEdges(context).size());
    map.put(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS, context.getRequirements().size());
    map.put(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED, context.getRequirements(RequirementStatus.NOT_COVERED).size());
    map.put(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_PASSED, context.getRequirements(RequirementStatus.PASSED).size());
    map.put(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_FAILED, context.getRequirements(RequirementStatus.FAILED).size());
    return map;
  }
}
