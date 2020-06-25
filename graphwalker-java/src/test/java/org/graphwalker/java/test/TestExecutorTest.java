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
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.statistics.Execution;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.java.annotation.GraphWalker;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import io.github.classgraph.ClassGraph;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class TestExecutorTest {

  @GraphWalker(start = "myStartElement")
  public static class MultipleStartElements extends ExecutionContext {

    public MultipleStartElements() {
      Vertex vertex = new Vertex();
      Model model = new Model()
        .addEdge(new Edge().setName("myStartElement").setSourceVertex(vertex).setTargetVertex(vertex))
        .addEdge(new Edge().setName("myStartElement").setSourceVertex(vertex).setTargetVertex(vertex));
      setModel(model.build());

    }
  }

  @Test(expected = TestExecutionException.class)
  public void multipleStartElements() throws IOException {
    Executor executor = new TestExecutor(MultipleStartElements.class);
    executor.execute();
  }

  @GraphWalker(start = "myOnlyStartElement")
  public static class SingleStartElements extends ExecutionContext {

    public SingleStartElements() {
      Vertex vertex = new Vertex().setName("myOnlyStartElement");
      Model model = new Model()
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex))
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
      setModel(model.build());
    }
  }

  @Test
  public void singleStartElements() throws IOException {
    Executor executor = new TestExecutor(SingleStartElements.class);
    executor.execute();
  }

  @GraphWalker(start = "nonExistingStartElement")
  public static class NonExistingStartElement extends ExecutionContext {

    public NonExistingStartElement() {
      Vertex vertex = new Vertex();
      Model model = new Model()
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex))
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
      setModel(model.build());
    }
  }

  @Test(expected = TestExecutionException.class)
  public void nonExistingStartElement() throws IOException {
    Executor executor = new TestExecutor(NonExistingStartElement.class);
    executor.execute();
  }

  @org.graphwalker.java.annotation.Model(file = "org/graphwalker/java/test/SimpleModel.json")
  public static class NonExistingGraphwalkerAnnotation extends ExecutionContext {
    @org.graphwalker.java.annotation.Vertex()
    void v_Vertex_1() {}

    @org.graphwalker.java.annotation.Vertex()
    void v_Vertex_2() {}

    @org.graphwalker.java.annotation.Edge()
    void e_Edge_1() {}
  }

  @Test()
  public void nonExistingGraphwalkerAnnotation() throws IOException {
    Executor executor = new TestExecutor(NonExistingGraphwalkerAnnotation.class);
    executor.execute();
  }

  @GraphWalker(value = "random(vertex_coverage(100))", start = "myStartElement")
  public static class DSLConfiguredTest extends ExecutionContext {

    public DSLConfiguredTest() {
      Vertex vertex = new Vertex().setName("myStartElement");
      Model model = new Model()
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex))
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
      setModel(model.build());
    }
  }

  @Test
  public void dslTest() throws IOException {
    new TestExecutor(DSLConfiguredTest.class).execute();
  }

  @Test
  public void isolation() throws MalformedURLException {
    List<URL> urls = new ArrayList<>();
    urls.add(new File(new File("."), "target/test-classes").toURI().toURL());
    urls.add(new File(new File("."), "target/classes").toURI().toURL());
    urls.addAll(new ClassGraph().getClasspathURLs());
    Configuration configuration = new Configuration();
    configuration.addInclude("*MyOtherTest*");
    Reflector reflector = new Reflector(configuration, new IsolatedClassLoader(urls.toArray(new URL[urls.size()])));
    MachineConfiguration mc = reflector.getMachineConfiguration();
    Result result = reflector.execute();
    JSONObject results = result.getResults();
    Assert.assertThat(result.getErrors().size(), is(0));
    Assert.assertThat(results.getInt("totalFailedNumberOfModels"), is(0));
    Assert.assertThat(results.getInt("totalNotExecutedNumberOfModels"), is(0));
    Assert.assertThat(results.getInt("totalNumberOfUnvisitedVertices"), is(0));
    Assert.assertThat(results.getInt("totalNumberOfModels"), is(1));
    Assert.assertThat(results.getInt("totalCompletedNumberOfModels"), is(1));
    Assert.assertThat(results.getInt("totalNumberOfVisitedEdges"), is(1));
    Assert.assertThat(results.getInt("totalIncompleteNumberOfModels"), is(0));
    Assert.assertThat(results.getInt("totalNumberOfVisitedVertices"), is(2));
    Assert.assertThat(results.getInt("edgeCoverage"), is(100));
    Assert.assertThat(results.getInt("vertexCoverage"), is(100));
    Assert.assertThat(results.getInt("totalNumberOfUnvisitedEdges"), is(0));
  }

  @GraphWalker(start = "throwException")
  public static class ThrowExceptionTest extends ExecutionContext {

    public ThrowExceptionTest() {
      Vertex vertex = new Vertex().setName("throwException");
      Model model = new Model()
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex))
        .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
      setModel(model.build());
    }

    public void throwException() {
      throw new RuntimeException();
    }
  }

  @Test(expected = TestExecutionException.class)
  public void throwExceptionExecutor() throws IOException {
    Executor executor = new TestExecutor(ThrowExceptionTest.class);
    executor.execute();
  }

  @Test
  public void multilpeContexts() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("org/graphwalker/java/test/PetClinic.json"));
    Executor executor = new TestExecutor(
      contexts.get(0),
      contexts.get(1),
      contexts.get(2),
      contexts.get(3),
      contexts.get(4)
    );
    Assert.assertThat(executor.getMachine().getContexts().size(), is(5));
  }

  @Test
  public void multilpeContextsWithSeed() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("org/graphwalker/java/test/PetClinicWithSeed.json"));
    Executor executor = new TestExecutor(
      contexts.get(0),
      contexts.get(1),
      contexts.get(2),
      contexts.get(3),
      contexts.get(4)
    );
    executor.execute();

    List<String> actualPath = new ArrayList<String>();
    for (Execution execution: executor.getMachine().getProfiler().getExecutionPath()) {
      actualPath.add(execution.getElement().getId());
    }

    Assert.assertArrayEquals(new ArrayList<>(Arrays.asList(
      "b53810a0-468c-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971ee142-468d-11e7-a919-92ebcb67fe33",
      "dcb0f8a5-468c-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "971eea2a-468d-11e7-a919-92ebcb67fe33",
      "dcb0f124-468c-11e7-a919-92ebcb67fe33",
      "971eeba6-468d-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "971eea2a-468d-11e7-a919-92ebcb67fe33",
      "dcb0f124-468c-11e7-a919-92ebcb67fe33",
      "971eeba6-468d-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "971eea2a-468d-11e7-a919-92ebcb67fe33",
      "dcb0f124-468c-11e7-a919-92ebcb67fe33",
      "971eeba6-468d-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "dcb0f8a5-468c-11e7-a919-92ebcb67fe33",
      "971ee2b4-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971ee142-468d-11e7-a919-92ebcb67fe33",
      "dcb0f8a5-468c-11e7-a919-92ebcb67fe33",
      "971ee732-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ee5c0-468d-11e7-a919-92ebcb67fe33",
      "dcb0f8a5-468c-11e7-a919-92ebcb67fe33",
      "971ee2b4-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0d798-468c-11e7-a919-92ebcb67fe33",
      "dcb104e8-468c-11e7-a919-92ebcb67fe33",
      "dcb0eab2-468c-11e7-a919-92ebcb67fe33",
      "dcb10736-468c-11e7-a919-92ebcb67fe33",
      "dcb0d798-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971ee142-468d-11e7-a919-92ebcb67fe33",
      "dcb0f8a5-468c-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "971eea2a-468d-11e7-a919-92ebcb67fe33",
      "dcb0f124-468c-11e7-a919-92ebcb67fe33",
      "971eeba6-468d-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "971eea2a-468d-11e7-a919-92ebcb67fe33",
      "dcb0f124-468c-11e7-a919-92ebcb67fe33",
      "971eeba6-468d-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "971eea2a-468d-11e7-a919-92ebcb67fe33",
      "dcb0f124-468c-11e7-a919-92ebcb67fe33",
      "971eeba6-468d-11e7-a919-92ebcb67fe33",
      "dcb0defa-468c-11e7-a919-92ebcb67fe33",
      "dcb0f8a5-468c-11e7-a919-92ebcb67fe33",
      "971ee2b4-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0d798-468c-11e7-a919-92ebcb67fe33",
      "dcb10812-468c-11e7-a919-92ebcb67fe33",
      "dcb0f3c2-468c-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "971ecaa4-468d-11e7-a919-92ebcb67fe33",
      "dcb0ebb6-468c-11e7-a919-92ebcb67fe33",
      "971ec838-468d-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "971ecca2-468d-11e7-a919-92ebcb67fe33",
      "dcb0f8a4-468c-11e7-a919-92ebcb67fe33",
      "971ece78-468d-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "dcb0f3c2-468c-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "971ed3c8-468d-11e7-a919-92ebcb67fe33",
      "971ec0b8-468d-11e7-a919-92ebcb67fe33",
      "971ed53a-468d-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "971edad0-468d-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fe62-468c-11e7-a919-92ebcb67fe33",
      "dcb0f200-468c-11e7-a919-92ebcb67fe33",
      "dcb0ff34-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0fd5e-468c-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ede36-468d-11e7-a919-92ebcb67fe33",
      "dcb0dde2-468c-11e7-a919-92ebcb67fe33",
      "971edcce-468d-11e7-a919-92ebcb67fe33",
      "dcb0ef4e-468c-11e7-a919-92ebcb67fe33",
      "971ec57c-468d-11e7-a919-92ebcb67fe33",
      "b53814ec-468c-11e7-a919-92ebcb67fe33",
      "dcb0fb88-468c-11e7-a919-92ebcb67fe33",
      "dcb0e896-468c-11e7-a919-92ebcb67fe33",
      "dcb0d798-468c-11e7-a919-92ebcb67fe33",
      "dcb10812-468c-11e7-a919-92ebcb67fe33",
      "dcb0f3c2-468c-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "971ecaa4-468d-11e7-a919-92ebcb67fe33",
      "dcb0ebb6-468c-11e7-a919-92ebcb67fe33",
      "971ed0b2-468d-11e7-a919-92ebcb67fe33",
      "dcb0ebb6-468c-11e7-a919-92ebcb67fe33",
      "971ec838-468d-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "dcb0f3c2-468c-11e7-a919-92ebcb67fe33",
      "dcb0dba8-468c-11e7-a919-92ebcb67fe33",
      "971ed3c8-468d-11e7-a919-92ebcb67fe33",
      "971ec0b8-468d-11e7-a919-92ebcb67fe33",
      "971ed738-468d-11e7-a919-92ebcb67fe33",
      "971ec0b8-468d-11e7-a919-92ebcb67fe33"
    )).toArray(), actualPath.toArray());
  }
}
