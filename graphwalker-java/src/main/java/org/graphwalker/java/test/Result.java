package org.graphwalker.java.test;

/*
 * #%L
 * GraphWalker Java
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

import static org.graphwalker.core.common.Objects.isNotNullOrEmpty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.RequirementStatus;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Nils Olsson
 */
public final class Result {

  private List<String> errors = new ArrayList<>();
  private JSONObject results;

  public Result() {
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public void addError(String error) {
    this.errors.add(error);
  }

  public boolean hasErrors() {
    return !getErrors().isEmpty();
  }

  public void setResults(String results) {
    this.results = new JSONObject(results.toString());
  }

  public JSONObject getResults() {
    return results;
  }

  public String getResultsAsString() {
    return results.toString(2);
  }

  public void updateResults(Machine machine, Map<Context, MachineException> failures) {
    int modelCount = 0;
    int completedModelCount = 0;
    int incompleteModelCount = 0;
    int failedModelCount = 0;
    int notExecutedModelCount = 0;
    int totalNumberOfEdges = 0;
    int totalNumberOfVertices = 0;
    int totalNumberOfUnvisitedVertices = 0;
    int totalNumberOfUnvisitedEdges = 0;
    int totalNumberOfRequirements = 0;
    int totalNumberOfRequirementsNotCovered = 0;
    int totalNumberOfRequirementsPassed = 0;
    int totalNumberOfRequirementsFailed = 0;

    JSONArray edgesNotVisitedJson = new JSONArray();
    JSONArray verticesNotVisitedJson = new JSONArray();
    JSONArray requirementsNotCoveredJson = new JSONArray();
    JSONArray requirementsPassedJson = new JSONArray();
    JSONArray requirementsFailedJson = new JSONArray();

    for (Context context : machine.getContexts()) {
      switch (context.getExecutionStatus()) {
        case COMPLETED: {
          completedModelCount++;
        }
        break;
        case FAILED: {
          failedModelCount++;
        }
        break;
        case NOT_EXECUTED: {
          notExecutedModelCount++;
        }
        break;
        case EXECUTING: {
          incompleteModelCount++;
        }
      }

      for (Requirement r : context.getRequirements(RequirementStatus.NOT_COVERED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("requirementKey", r.getKey());
        jsonElement.put("modelName", context.getModel().getName());
        requirementsNotCoveredJson.put(jsonElement);
      }

      for (Requirement r : context.getRequirements(RequirementStatus.PASSED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("requirementKey", r.getKey());
        jsonElement.put("modelName", context.getModel().getName());
        requirementsPassedJson.put(jsonElement);
      }

      for (Requirement r : context.getRequirements(RequirementStatus.FAILED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("RequirementKey", r.getKey());
        jsonElement.put("modelName", context.getModel().getName());
        requirementsFailedJson.put(jsonElement);
      }

      for (Element edge : context.getProfiler().getUnvisitedEdges(context)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("edgeName", edge.getName());
        jsonElement.put("edgeId", edge.getId());
        jsonElement.put("modelName", context.getModel().getName());
        edgesNotVisitedJson.put(jsonElement);
      }

      for (Element vertex : context.getProfiler().getUnvisitedVertices(context)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("vertexName", vertex.getName());
        jsonElement.put("vertexId", vertex.getId());
        jsonElement.put("modelName", context.getModel().getName());
        verticesNotVisitedJson.put(jsonElement);
      }

      modelCount++;
      totalNumberOfEdges += context.getModel().getEdges().size();
      totalNumberOfVertices += context.getModel().getVertices().size();
      totalNumberOfUnvisitedVertices += context.getProfiler().getUnvisitedVertices(context).size();
      totalNumberOfUnvisitedEdges += context.getProfiler().getUnvisitedEdges(context).size();
      totalNumberOfRequirements += context.getRequirements().size();
      totalNumberOfRequirementsNotCovered += context.getRequirements(RequirementStatus.NOT_COVERED).size();
      totalNumberOfRequirementsPassed += context.getRequirements(RequirementStatus.PASSED).size();
      totalNumberOfRequirementsFailed += context.getRequirements(RequirementStatus.FAILED).size();
    }

    results = new JSONObject();
    results.put("totalNumberOfModels", modelCount);
    results.put("totalCompletedNumberOfModels", completedModelCount);
    results.put("totalIncompleteNumberOfModels", incompleteModelCount);
    results.put("totalFailedNumberOfModels", failedModelCount);
    results.put("totalNotExecutedNumberOfModels", notExecutedModelCount);
    results.put("totalNumberOfEdges", totalNumberOfEdges);
    results.put("totalNumberOfUnvisitedEdges", totalNumberOfUnvisitedEdges);
    results.put("totalNumberOfVisitedEdges", machine.getProfiler().getVisitedEdges().size());
    if (totalNumberOfEdges > 0) {
      results.put("edgeCoverage",
                  100 * (totalNumberOfEdges - totalNumberOfUnvisitedEdges) / totalNumberOfEdges);
    }
    results.put("totalNumberOfVertices", totalNumberOfVertices);
    results.put("totalNumberOfUnvisitedVertices", totalNumberOfUnvisitedVertices);
    results.put("totalNumberOfVisitedVertices", machine.getProfiler().getVisitedVertices().size());
    if (totalNumberOfVertices > 0) {
      results.put("vertexCoverage", 100 * (totalNumberOfVertices - totalNumberOfUnvisitedVertices)
                                    / totalNumberOfVertices);
    }

    results.put("edgesNotVisited", edgesNotVisitedJson);
    results.put("verticesNotVisited", verticesNotVisitedJson);

    if (totalNumberOfRequirements > 0) {
      results.put("totalNumberOfRequirement", totalNumberOfRequirements);
      results.put("totalNumberOfUncoveredRequirement", totalNumberOfRequirementsNotCovered);
      results.put("totalNumberOfPassedRequirement", totalNumberOfRequirementsPassed);
      results.put("totalNumberOfFailedRequirement", totalNumberOfRequirementsFailed);
      results.put("requirementCoverage", 100 * (totalNumberOfRequirements - totalNumberOfRequirementsNotCovered) / totalNumberOfRequirements);

      results.put("requirementsNotCovered", requirementsNotCoveredJson);
      results.put("requirementsPassed", requirementsPassedJson);
      results.put("requirementsFailed", requirementsFailedJson);
    }

    if (isNotNullOrEmpty(failures)) {
      JSONArray jsonFailures = new JSONArray();
      for (MachineException exception : failures.values()) {
        addError(getStackTrace(exception.getCause()));
        JSONObject jsonFailure = new JSONObject();
        jsonFailure.put("failure", getStackTrace(exception.getCause()));
        jsonFailures.put(jsonFailure);
      }
      results.put("failures", jsonFailures);
    }
  }

  private String getStackTrace(Throwable throwable) {
    StringWriter writer = new StringWriter();
    throwable.printStackTrace(new PrintWriter(writer, true));
    return writer.getBuffer().toString();
  }
}
