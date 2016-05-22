package org.graphwalker.java.test;

import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.hamcrest.core.StringStartsWith;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;


/**
 * Created by krikar on 2016-05-13.
 */
public class ResultTest {

  /**
   * Verifies statistics with multiple models execution.
   */
  @Test
  public void PetClinic() {
    List<Context> contexts = new JsonContextFactory().createMultiple(Paths.get("org/graphwalker/java/test/PetClinic.json"));
    Executor executor = new TestExecutor(contexts);
    JSONObject results = executor.execute(true).getResults();

    Assert.assertThat("totalNumberOfModels", results.getInt("totalNumberOfModels"), is(5));
    Assert.assertThat("totalCompletedNumberOfModels", results.getInt("totalCompletedNumberOfModels"), is(5));
    Assert.assertThat("totalIncompleteNumberOfModels", results.getInt("totalIncompleteNumberOfModels"), is(0));
    Assert.assertThat("totalFailedNumberOfModels", results.getInt("totalFailedNumberOfModels"), is(0));
    Assert.assertThat("totalNotExecutedNumberOfModels", results.getInt("totalNotExecutedNumberOfModels"), is(0));

    Assert.assertThat("vertexCoverage", results.getInt("vertexCoverage"), is(100));
    Assert.assertThat("totalNumberOfEdges", results.getInt("totalNumberOfEdges"), is(25));
    Assert.assertThat("totalNumberOfVisitedVertices", results.getInt("totalNumberOfVisitedVertices"), is(16));
    Assert.assertThat("totalNumberOfUnvisitedVertices", results.getInt("totalNumberOfUnvisitedVertices"), is(0));
    Assert.assertThat("totalNumberOfVisitedEdges", results.getInt("totalNumberOfVisitedEdges"), is(25));
    Assert.assertThat("edgeCoverage", results.getInt("edgeCoverage"), is(100));
    Assert.assertThat("totalNumberOfVertices", results.getInt("totalNumberOfVertices"), is(16));
    Assert.assertThat("totalNumberOfUnvisitedEdges", results.getInt("totalNumberOfUnvisitedEdges"), is(0));
  }

  /**
   * Verifies statistics model with requirements.
   */
  @Test
  public void UC01() {
    List<Context> contexts = new JsonContextFactory().createMultiple(Paths.get("org/graphwalker/java/test/UC01.json"));
    Executor executor = new TestExecutor(contexts);
    JSONObject results = executor.execute(true).getResults();

    Assert.assertThat("totalNumberOfModels", results.getInt("totalNumberOfModels"), is(1));
    Assert.assertThat("totalCompletedNumberOfModels", results.getInt("totalCompletedNumberOfModels"), is(1));
    Assert.assertThat("totalIncompleteNumberOfModels", results.getInt("totalIncompleteNumberOfModels"), is(0));
    Assert.assertThat("totalFailedNumberOfModels", results.getInt("totalFailedNumberOfModels"), is(0));
    Assert.assertThat("totalNotExecutedNumberOfModels", results.getInt("totalNotExecutedNumberOfModels"), is(0));

    Assert.assertThat("vertexCoverage", results.getInt("vertexCoverage"), is(100));
    Assert.assertThat("totalNumberOfEdges", results.getInt("totalNumberOfEdges"), is(12));
    Assert.assertThat("totalNumberOfVisitedVertices", results.getInt("totalNumberOfVisitedVertices"), is(7));
    Assert.assertThat("totalNumberOfUnvisitedVertices", results.getInt("totalNumberOfUnvisitedVertices"), is(0));
    Assert.assertThat("totalNumberOfVisitedEdges", results.getInt("totalNumberOfVisitedEdges"), is(12));
    Assert.assertThat("edgeCoverage", results.getInt("edgeCoverage"), is(100));
    Assert.assertThat("totalNumberOfVertices", results.getInt("totalNumberOfVertices"), is(7));
    Assert.assertThat("totalNumberOfUnvisitedEdges", results.getInt("totalNumberOfUnvisitedEdges"), is(0));


    // The requirement part
    Assert.assertThat("totalNumberOfRequirement", results.getInt("totalNumberOfRequirement"), is(4));
    Assert.assertThat("totalNumberOfUncoveredRequirement", results.getInt("totalNumberOfUncoveredRequirement"), is(0));
    Assert.assertThat("totalNumberOfPassedRequirement", results.getInt("totalNumberOfPassedRequirement"), is(4));

    Assert.assertThat("requirementsPassed", results.getJSONArray("requirementsPassed").length(), is(4));
    List<String> requirements = new ArrayList<>();
    for (int index = 0; index <  results.getJSONArray("requirementsPassed").length(); index++ ) {
      requirements.add(results.getJSONArray("requirementsPassed").getJSONObject(index).getString("requirementKey"));
    }
    List<String> expectedRequirements = Arrays.asList("UC01 2.2.1", "UC01 2.2.2", "UC01 2.2.3", "UC01 2.3");
    Collections.sort(requirements);
    Assert.assertThat("requirementsPassed list", requirements.toArray(), is(expectedRequirements.toArray()));

    Assert.assertThat("requirementsFailed", results.getJSONArray("requirementsFailed").length(), is(0));
  }

  /**
   * Verifies statistics multiple with model that has run failures.
   */
  @Test
  public void DualPathModel() {
    List<Context> contexts = new JsonContextFactory().createMultiple(Paths.get("org/graphwalker/java/test/DualPathModel.json"));
    Executor executor = new TestExecutor(contexts);
    JSONObject results = executor.execute(true).getResults();

    Assert.assertThat("totalNumberOfModels", results.getInt("totalNumberOfModels"), is(1));
    Assert.assertThat("totalCompletedNumberOfModels", results.getInt("totalCompletedNumberOfModels"), is(0));
    Assert.assertThat("totalIncompleteNumberOfModels", results.getInt("totalIncompleteNumberOfModels"), is(0));
    Assert.assertThat("totalFailedNumberOfModels", results.getInt("totalFailedNumberOfModels"), is(1));
    Assert.assertThat("totalNotExecutedNumberOfModels", results.getInt("totalNotExecutedNumberOfModels"), is(0));

    Assert.assertThat("vertexCoverage", results.getInt("vertexCoverage"), is(75));
    Assert.assertThat("totalNumberOfEdges", results.getInt("totalNumberOfEdges"), is(5));
    Assert.assertThat("totalNumberOfVisitedVertices", results.getInt("totalNumberOfVisitedVertices"), is(3));
    Assert.assertThat("totalNumberOfUnvisitedVertices", results.getInt("totalNumberOfUnvisitedVertices"), is(1));
    Assert.assertThat("totalNumberOfVisitedEdges", results.getInt("totalNumberOfVisitedEdges"), is(3));
    Assert.assertThat("edgeCoverage", results.getInt("edgeCoverage"), is(60));
    Assert.assertThat("totalNumberOfVertices", results.getInt("totalNumberOfVertices"), is(4));
    Assert.assertThat("totalNumberOfUnvisitedEdges", results.getInt("totalNumberOfUnvisitedEdges"), is(2));


    // Verify the list of unvisited vertices
    Assert.assertThat("verticesNotVisited", results.getJSONArray("verticesNotVisited").length(), is(1));
    List<String> unvisitedVertices = new ArrayList<>();
    for (int index = 0; index <  results.getJSONArray("verticesNotVisited").length(); index++ ) {
      unvisitedVertices.add(results.getJSONArray("verticesNotVisited").getJSONObject(index).getString("vertexName"));
      unvisitedVertices.add(results.getJSONArray("verticesNotVisited").getJSONObject(index).getString("vertexId"));
    }
    List<String> expectedUnvisitedVertices = Arrays.asList("v3", "n3");
    Assert.assertThat("verticesNotVisited list", unvisitedVertices.toArray(), is(expectedUnvisitedVertices.toArray()));


    // Verify the list of unvisited edges
    Assert.assertThat("edgesNotVisited", results.getJSONArray("edgesNotVisited").length(), is(2));
    List<String> unvisitedEdges = new ArrayList<>();
    for (int index = 0; index <  results.getJSONArray("edgesNotVisited").length(); index++ ) {
      unvisitedEdges.add(results.getJSONArray("edgesNotVisited").getJSONObject(index).getString("edgeName"));
      unvisitedEdges.add(results.getJSONArray("edgesNotVisited").getJSONObject(index).getString("edgeId"));
    }
    List<String> expectedUnvisitedEdges = Arrays.asList("e3", "e2", "e5", "e4");
    Assert.assertThat("edgesNotVisited list", unvisitedEdges.toArray(), is(expectedUnvisitedEdges.toArray()));

    Assert.assertThat("failures", results.getJSONArray("failures").length(), is(1));
    Assert.assertThat("failure text", results.getJSONArray("failures").getJSONObject(0).getString("failure"), new StringStartsWith("org.graphwalker.core.generator.NoPathFoundException: Could not find a valid path from element: v2"));
  }
}
