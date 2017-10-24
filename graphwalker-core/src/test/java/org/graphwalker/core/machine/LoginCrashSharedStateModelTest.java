package org.graphwalker.core.machine;

/*
 * #%L
 * GraphWalker Core
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

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.graphwalker.core.condition.AlternativeCondition;
import org.graphwalker.core.condition.CombinedCondition;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.ReachedEdge;
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * This is a programatic implementaion of the models:
 * https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/src/test/resources/graphml/shared_state/Login.graphml
 * https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/src/test/resources/graphml/shared_state/Crash.graphml
 * <p/>
 * Created by krikar on 8/20/14.
 */
public class LoginCrashSharedStateModelTest {

  /**
   * The login Model
   */
  Vertex v_Browse = new Vertex().setName("v_Browse").setSharedState("LOGGED_IN");
  Vertex v_ClientNotRunning = new Vertex().setName("v_ClientNotRunning").setSharedState("CLIENT_NOT_RUNNING");
  Vertex v_LoginPrompted = new Vertex().setName("v_LoginPrompted");

  Edge e_Close = new Edge().setName("e_Close").setSourceVertex(v_LoginPrompted).setTargetVertex(v_ClientNotRunning);
  Edge e_Exit = new Edge().setName("e_Exit").setSourceVertex(v_Browse).setTargetVertex(v_ClientNotRunning);
  Edge
      e_InvalidCredentials =
      new Edge().setName("e_InvalidCredentials").setSourceVertex(v_LoginPrompted).setTargetVertex(v_LoginPrompted).addAction(new Action("validLogin=false"));
  Edge e_Logout = new Edge().setName("e_Logout").setSourceVertex(v_Browse).setTargetVertex(v_LoginPrompted);
  Edge
      e_StartClient_1 =
      new Edge().setName("e_StartClient").setSourceVertex(v_ClientNotRunning).setTargetVertex(v_LoginPrompted).setGuard(new Guard("!rememberMe||!validLogin"));
  Edge
      e_StartClient_2 =
      new Edge().setName("e_StartClient").setSourceVertex(v_ClientNotRunning).setTargetVertex(v_Browse).setGuard(new Guard("rememberMe&&validLogin"));
  Edge
      e_ToggleRememberMe =
      new Edge().setName("e_ToggleRememberMe").setSourceVertex(v_LoginPrompted).setTargetVertex(v_LoginPrompted).addAction(new Action("rememberMe=true"));
  Edge
      e_ValidPremiumCredentials =
      new Edge().setName("e_ValidPremiumCredentials").setSourceVertex(v_LoginPrompted).setTargetVertex(v_Browse).addAction(new Action("validLogin=true"));

  Model loginModel = new Model().addEdge(e_Close)
      .addEdge(e_Exit)
      .addEdge(e_InvalidCredentials)
      .addEdge(e_Logout)
      .addEdge(e_StartClient_1)
      .addEdge(e_StartClient_2)
      .addEdge(e_ToggleRememberMe)
      .addEdge(e_ValidPremiumCredentials)
      //.addAction(new Action("e_Init()"))
      .addAction(new Action("validLogin=false"))
      .addAction(new Action("rememberMe=false"));

  /**
   * The crash Model
   */
  Vertex firstVertex = new Vertex().setSharedState("LOGGED_IN");
  Vertex v_CrashDumpFilesGenerated = new Vertex().setName("v_CrashDumpFilesGenerated");
  Vertex lastVertex = new Vertex().setSharedState("CLIENT_NOT_RUNNING");

  Edge e_CrashSpotify = new Edge().setName("e_CrashSpotify").setSourceVertex(firstVertex).setTargetVertex(v_CrashDumpFilesGenerated);
  Edge lastEdge = new Edge().setName("e_Exit").setSourceVertex(v_CrashDumpFilesGenerated).setTargetVertex(lastVertex);

  Model crashModel = new Model()
      .addEdge(e_CrashSpotify)
      .addEdge(lastEdge);


  //Test
  public void shortestAllPathEdgeCoverage() throws Exception {
    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new ShortestAllPaths(new EdgeCoverage(100))).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new ShortestAllPaths(new EdgeCoverage(100))));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  //Test
  public void shortestAllPathEdgeAndVertexCoverage() throws Exception {
    CombinedCondition combinedCondition = new CombinedCondition();
    combinedCondition.addStopCondition(new EdgeCoverage(100));
    combinedCondition.addStopCondition(new VertexCoverage(100));

    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new ShortestAllPaths(combinedCondition)).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new ShortestAllPaths(combinedCondition)));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  //Test
  public void shortestAllPathEdgeOrVertexCoverage() throws Exception {
    AlternativeCondition alternativeCondition = new AlternativeCondition();
    alternativeCondition.addStopCondition(new EdgeCoverage(100));
    alternativeCondition.addStopCondition(new VertexCoverage(100));

    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new ShortestAllPaths(alternativeCondition)).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new ShortestAllPaths(alternativeCondition)));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  //Test
  public void aStarPathReachedEdgeAndReachedVertex() throws Exception {
    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new AStarPath(new ReachedEdge("e_RememberMe"))).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new AStarPath(new ReachedVertex("v_CrashDumpFilesGenerated"))));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }

    List<Element> expectedPath = Arrays.asList(
        v_ClientNotRunning.build(),
        e_StartClient_1.build(),
        v_LoginPrompted.build(),
        e_ValidPremiumCredentials.build(),
        v_Browse.build(),
        e_Exit.build());
    Collections.reverse(expectedPath);
    assertArrayEquals(expectedPath.toArray(), machine.getCurrentContext().getProfiler().getExecutionPath().toArray());
  }

  /**
   * Should not throw any exceptions or end up in some infinite loop
   */
  @Test
  public void randomPathEdgeCoverage() throws Exception {
    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new RandomPath(new EdgeCoverage(100))).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new RandomPath(new EdgeCoverage(100))));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  /**
   * Should not throw any exceptions or end up in some infinite loop
   */
  @Test
  public void randomPathVertexCoverage() throws Exception {
    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new RandomPath(new VertexCoverage(100))).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new RandomPath(new VertexCoverage(100))));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  /**
   * Should not throw any exceptions or end up in some infinite loop
   */
  @Test
  public void randomPathEdgeAndVertexCoverage() throws Exception {
    CombinedCondition combinedCondition = new CombinedCondition();
    combinedCondition.addStopCondition(new EdgeCoverage(100));
    combinedCondition.addStopCondition(new VertexCoverage(100));

    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new RandomPath(combinedCondition)).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new RandomPath(combinedCondition)));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }

  /**
   * Should not throw any exceptions or end up in some infinite loop
   */
  @Test
  public void randomPathEdgeOrVertexCoverage() throws Exception {
    AlternativeCondition alternativeCondition = new AlternativeCondition();
    alternativeCondition.addStopCondition(new EdgeCoverage(100));
    alternativeCondition.addStopCondition(new VertexCoverage(100));

    List<Context> contexts = new ArrayList<>();
    contexts.add(new TestExecutionContext(loginModel, new RandomPath(alternativeCondition)).setNextElement(v_ClientNotRunning));
    contexts.add(new TestExecutionContext(crashModel, new RandomPath(alternativeCondition)));
    SimpleMachine machine = new SimpleMachine(contexts);

    while (machine.hasNextStep()) {
      machine.getNextStep();
    }
  }
}
