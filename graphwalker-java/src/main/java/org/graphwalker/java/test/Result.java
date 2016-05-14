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

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.RequirementStatus;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Nils Olsson
 */
public final class Result {

  private List<String> errors = new ArrayList<>();
  JSONObject result;

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

  public JSONObject getResults() {
    return result;
  }

  public void updateResults(Machine machine, Map<Context, MachineException> failures) {
    machine.getProfiler().updateResult();

    result = new JSONObject();
    result.put("totalNumberOfModels", machine.getProfiler().getModelCount());
    result.put("totalCompletedNumberOfModels", machine.getProfiler().getCompletedModelCount());
    result.put("totalIncompleteNumberOfModels", machine.getProfiler().getIncompleteModelCount());
    result.put("totalFailedNumberOfModels", machine.getProfiler().getFailedModelCount());
    result.put("totalNotExecutedNumberOfModels", machine.getProfiler().getNotExecutedModelCount());
    result.put("totalNumberOfEdges", machine.getProfiler().getTotalNumberOfEdges());
    result.put("totalNumberOfUnvisitedEdges", machine.getProfiler().getTotalNumberOfUnvisitedEdges());
    result.put("totalNumberOfVisitedEdges", machine.getProfiler().getVisitedEdges().size());
    result.put("edgeCoverage", 100 * (machine.getProfiler().getTotalNumberOfEdges() - machine.getProfiler().getTotalNumberOfUnvisitedEdges()) / machine.getProfiler().getTotalNumberOfEdges());
    result.put("totalNumberOfVertices", machine.getProfiler().getTotalNumberOfVertices());
    result.put("totalNumberOfUnvisitedVertices", machine.getProfiler().getTotalNumberOfUnvisitedVertices());
    result.put("totalNumberOfVisitedVertices", machine.getProfiler().getVisitedVertices().size());
    result.put("vertexCoverage", 100 * (machine.getProfiler().getTotalNumberOfVertices() - machine.getProfiler().getTotalNumberOfUnvisitedVertices()) / machine.getProfiler().getTotalNumberOfVertices());

    if (machine.getProfiler().getTotalNumberOfUnvisitedEdges() > 0) {
      JSONArray jsonElements = new JSONArray();
      for (Element edge : machine.getProfiler().getUnvisitedEdges()) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("edgeName", edge.getName());
        jsonElement.put("edgeId", edge.getId());
        jsonElements.put(jsonElement);
      }
      result.put("edgesNotVisited", jsonElements);
    }

    if (machine.getProfiler().getTotalNumberOfUnvisitedVertices() > 0) {
      JSONArray jsonElements = new JSONArray();
      for (Element vertex : machine.getProfiler().getUnvisitedVertices()) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("vertexName", vertex.getName());
        jsonElement.put("vertexId", vertex.getId());
        jsonElements.put(jsonElement);
      }
      result.put("verticesNotVisited", jsonElements);
    }

    if (machine.getProfiler().getTotalNumberOfRequirements() > 0) {
      result.put("totalNumberOfRequirement", machine.getProfiler().getTotalNumberOfRequirements());
      result.put("totalNumberOfUncoveredRequirement", machine.getProfiler().getTotalNumberOfRequirementsNotCovered());
      result.put("totalNumberOfPassedRequirement", machine.getProfiler().getTotalNumberOfRequirementsPassed());
      result.put("totalNumberOfFailedRequirement", machine.getProfiler().getTotalNumberOfRequirementsFailed());
      result.put("requirementCoverage", 100 * (machine.getProfiler().getTotalNumberOfRequirements() - machine.getProfiler().getTotalNumberOfRequirementsNotCovered()) / machine.getProfiler().getTotalNumberOfRequirements());

      JSONArray jsonElements = new JSONArray();
      for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.NOT_COVERED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("requirementKey", r.getKey());
        jsonElements.put(jsonElement);
      }
      result.put("requirementsNotCovered", jsonElements);

      jsonElements = new JSONArray();
      for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.PASSED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("requirementKey", r.getKey());
        jsonElements.put(jsonElement);
      }
      result.put("requirementsPassed", jsonElements);

      jsonElements = new JSONArray();
      for (Requirement r : machine.getCurrentContext().getRequirements(RequirementStatus.FAILED)) {
        JSONObject jsonElement = new JSONObject();
        jsonElement.put("RequirementKey", r.getKey());
        jsonElements.put(jsonElement);
      }
      result.put("requirementsFailed", jsonElements);
    }

    if (failures.size() > 0) {
      JSONArray jsonFailures = new JSONArray();
      for (MachineException exception : failures.values()) {
        JSONObject jsonFailure = new JSONObject();
        jsonFailure.put("failure", getStackTrace(exception.getCause()));
        jsonFailures.put(jsonFailure);
      }
      result.put("failures", jsonFailures);
    }
  }

  private String getStackTrace(Throwable throwable) {
    StringWriter writer = new StringWriter();
    throwable.printStackTrace(new PrintWriter(writer, true));
    return writer.getBuffer().toString();
  }
}
