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

import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.java.annotation.GraphWalker;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

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
    urls.addAll(Arrays.asList(((URLClassLoader) getClass().getClassLoader()).getURLs()));
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
  public void ThrowExceptionExecutor() throws IOException {
    Executor executor = new TestExecutor(ThrowExceptionTest.class);
    executor.execute();
  }
}
