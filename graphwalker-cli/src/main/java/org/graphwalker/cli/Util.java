package org.graphwalker.cli;

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
import org.json.JSONArray;
import org.json.JSONObject;

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
     * Will create a plain text formatted string representing the current step. The step
     * is the current element, which can be either a vertex orn an edge.
     * @param machine
     * @param verbose Print more details if true
     * @param showUnvisited Print all unvisited elements if true
     * @return The plain text string representing the current step.
     */
    public static String getStepAsString(Machine machine, boolean verbose, boolean showUnvisited) {
        StringBuilder builder = new StringBuilder();
        if (verbose) {
            builder.append(FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName())).append(" : ");
        }
        if (machine.getCurrentContext().getCurrentElement().hasName()) {
            builder.append(machine.getCurrentContext().getCurrentElement().getName());
            if (verbose) {
                builder.append("(").append(machine.getCurrentContext().getCurrentElement().getId()).append(")");
                builder.append(":").append(machine.getCurrentContext().getKeys());
            }
        }

        if (showUnvisited) {
            Context context = machine.getCurrentContext();
            builder.append(" | ").append(context.getProfiler().getUnvisitedElements(context).size())
                    .append("(").append(context.getModel().getElements().size()).append(") : ");
            for (Element e : context.getProfiler().getUnvisitedElements(context)) {
                builder.append(e.getName());
                if (verbose) {
                    builder.append("(").append(e.getId()).append(")");
                }
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    /**
     * Will create a JSON formatted string representing the current step. The step
     * is the current element, which can be either a vertex orn an edge.
     * @param machine
     * @param verbose Print more details if true
     * @param showUnvisited Print all unvisited elements if true
     * @return The JSON string representing the current step.
     */
    public static JSONObject getStepAsJSON(Machine machine, boolean verbose, boolean showUnvisited) {
        JSONObject object = new JSONObject();
        if (verbose) {
            object.put("ModelName", FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName()));
        }
        if (machine.getCurrentContext().getCurrentElement().hasName()) {
            object.put("CurrentElementName", machine.getCurrentContext().getCurrentElement().getName());
            if (verbose) {
                object.put("CurrentElementID", machine.getCurrentContext().getCurrentElement().getId());

                JSONArray jsonKeys = new JSONArray();
                for (Map.Entry<String, String> key : machine.getCurrentContext().getKeys().entrySet()) {
                    JSONObject jsonKey = new JSONObject();
                    jsonKey.put(key.getKey(), key.getValue());
                    jsonKeys.put(jsonKey);
                }
                object.put("Data", jsonKeys);
            }
        }
        if (showUnvisited) {
            Context context = machine.getCurrentContext();
            object.put("NumberOfElements", context.getModel().getElements().size());
            object.put("NumberOfUnvisitedElements", context.getProfiler().getUnvisitedElements(context).size());

            JSONArray jsonElements = new JSONArray();
            for (Element element : context.getProfiler().getUnvisitedElements(context)) {
                JSONObject jsonElement = new JSONObject();
                jsonElement.put("ElementName", element.getName());
                if (verbose) {
                    jsonElement.put("ElementId", element.getId());
                }
                jsonElements.put(jsonElement);
            }
            object.put("UnvisitedElements", jsonElements);
        }
        return object;
    }

    /**
     * Will create a plain text string representing the statistics of the current
     * execution of the machine.
     * @param machine
     * @return The execution statistics in plain text.
     */
    public static String getStatisticsAsString(Machine machine) {
        HashMap<Statistics, Integer> map = getStatistics(machine.getCurrentContext());

        StringBuilder builder = new StringBuilder();
        builder.append("Coverage Edges: ")
                .append(map.get(Statistics.TOTAL_NUMBER_OF_EDGES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES))
                .append("/")
                .append(map.get(Statistics.TOTAL_NUMBER_OF_EDGES))
                .append(" => ")
                .append(100 * (map.get(Statistics.TOTAL_NUMBER_OF_EDGES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES)) / map.get(Statistics.TOTAL_NUMBER_OF_EDGES))
                .append("%")
                .append(System.lineSeparator());
        builder.append("Coverage Vertices: ")
                .append(map.get(Statistics.TOTAL_NUMBER_OF_VERTICES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES))
                .append("/")
                .append(map.get(Statistics.TOTAL_NUMBER_OF_VERTICES))
                .append(" => ")
                .append(100 * (map.get(Statistics.TOTAL_NUMBER_OF_VERTICES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES)) / map.get(Statistics.TOTAL_NUMBER_OF_VERTICES))
                .append("%")
                .append(System.lineSeparator());
        if (map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) > 0) {
            builder.append("Coverage Requirements: ")
                    .append(map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) - map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED))
                    .append("/")
                    .append(map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS))
                    .append(" => ")
                    .append(100 * (map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) - map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED)) / map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS))
                    .append("%")
                    .append(System.lineSeparator());

            builder.append("Requirements not covered:").append(System.lineSeparator());
            for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.NOT_COVERED)) {
                builder.append("  ")
                        .append(r.getKey())
                        .append(System.lineSeparator());
            }
            builder.append("Requirements passed:").append(System.lineSeparator());
            for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.PASSED)) {
                builder.append("  ")
                        .append(r.getKey())
                        .append(System.lineSeparator());
            }
            builder.append("Requirements failed:").append(System.lineSeparator());
            for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.FAILED)) {
                builder.append("  ")
                        .append(r.getKey())
                        .append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    /**
     * Will create a JSON formatted string representing the statistics of the current
     * execution of the machine.
     * @param machine
     * @return The execution statistics in JSON format.
     */
    public static JSONObject getStatisticsAsJSON(Machine machine) {
        HashMap<Statistics, Integer> map = getStatistics(machine.getCurrentContext());
        JSONObject object = new JSONObject();
        object.put("TotalNumberOfEdges", map.get(Statistics.TOTAL_NUMBER_OF_EDGES));
        object.put("TotalNumberOfUnvisitedEdges", map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES));
        object.put("TotalNumberOfVisitedEdges", map.get(Statistics.TOTAL_NUMBER_OF_EDGES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES));
        object.put("EdgeCoverage", 100 * (map.get(Statistics.TOTAL_NUMBER_OF_EDGES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_EDGES)) / map.get(Statistics.TOTAL_NUMBER_OF_EDGES));
        object.put("TotalNumberOfVertices", map.get(Statistics.TOTAL_NUMBER_OF_VERTICES));
        object.put("TotalNumberOfUnvisitedVertices", map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES));
        object.put("TotalNumberOfVisitedVertices", map.get(Statistics.TOTAL_NUMBER_OF_VERTICES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES));
        object.put("VertexCoverage", 100 * (map.get(Statistics.TOTAL_NUMBER_OF_VERTICES) - map.get(Statistics.TOTAL_NUMBER_OF_UNVISITED_VERTICES)) / map.get(Statistics.TOTAL_NUMBER_OF_VERTICES));

        if (map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) > 0) {
            object.put("TotalNumberOfRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS));
            object.put("TotalNumberOfUncoveredRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED));
            object.put("TotalNumberOfPassedRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_PASSED));
            object.put("TotalNumberOfFailedRequirement", map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_FAILED));
            object.put("RequirementCoverage", 100 * (map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS) - map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS_NOT_COVERED)) / map.get(Statistics.TOTAL_NUMBER_OF_REQUIREMENTS));

            JSONArray jsonElements = new JSONArray();
            for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.NOT_COVERED)) {
                JSONObject jsonElement = new JSONObject();
                jsonElement.put("RequirementKey", r.getKey());
                jsonElements.put(jsonElement);
            }
            object.put("RequirementsNotCovered", jsonElements);

            jsonElements = new JSONArray();
            for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.PASSED)) {
                JSONObject jsonElement = new JSONObject();
                jsonElement.put("RequirementKey", r.getKey());
                jsonElements.put(jsonElement);
            }
            object.put("RequirementsPassed", jsonElements);

            jsonElements = new JSONArray();
            for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.FAILED)) {
                JSONObject jsonElement = new JSONObject();
                jsonElement.put("RequirementKey", r.getKey());
                jsonElements.put(jsonElement);
            }
            object.put("RequirementsFailed", jsonElements);
        }
        return object;
    }

    private static HashMap<Statistics, Integer> getStatistics(Context context) {
        HashMap<Statistics, Integer> map = new HashMap<>();
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
