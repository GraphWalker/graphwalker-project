package org.graphwalker.restful;

/*
 * #%L
 * GraphWalker As A Service
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

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.java.annotation.AfterExecution;
import org.graphwalker.java.annotation.BeforeExecution;
import org.graphwalker.java.annotation.GraphWalker;
import org.graphwalker.java.test.TestExecutionException;
import org.graphwalker.java.test.TestExecutor;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.ArraySizeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by krikar on 10/10/14.
 */
@GraphWalker(value = "random(edge_coverage(100))", start = "v_EmptyMachine")
public class RestTest extends ExecutionContext implements RestFlow {

  private static final Logger logger = LoggerFactory.getLogger(RestTest.class);
  HttpServer server;
  Restful rest;
  HttpResponse response;

  @BeforeExecution
  public void startServer() throws Exception {
    ResourceConfig rc = new DefaultResourceConfig();
    rest = new Restful(null, true, true, true);
    rc.getSingletons().add(rest);

    String url = "http://0.0.0.0:" + 9191;

    server = GrizzlyServerFactory.createHttpServer(url, rc);
    logger.debug("Starting RestFul service");
    server.start();
  }

  @AfterExecution
  public void stopServer() {
    logger.debug("Stopping RestFul service");
    server.stop();
  }

  @Test
  public void testRun() throws IOException {
    TestExecutor testExecutor = new TestExecutor(getClass());
    try {
      testExecutor.execute(false);
    } catch (TestExecutionException e) {
      if (e.hasErrors()) {
        for (String error : e.getResult().getErrors()) {
          System.err.println(error);
        }
        Assert.fail("Did not expect any errors");
      }
    }
  }

  private CloseableHttpResponse httpExecute(HttpRequestBase request) {
    try {
      return HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return null;
  }

  @Override
  public void e_GetData() {
    response = httpExecute(new HttpGet("http://localhost:9191/graphwalker/getData"));
  }

  @Override
  public void e_SetData() {
    response = httpExecute(new HttpPut("http://localhost:9191/graphwalker/setData/MAX_BOOKS=6;"));
  }

  @Override
  public void v_EmptyMachine() {
    Assert.assertNull(rest.getContexts());
    Assert.assertNull(rest.getMachine());
  }

  @Override
  public void e_GetStatistics() {
    response = httpExecute(new HttpGet("http://localhost:9191/graphwalker/getStatistics"));
  }

  @Override
  public void e_GetNext() {
    response = httpExecute(new HttpGet("http://localhost:9191/graphwalker/getNext"));
  }

  @Override
  public void e_Restart() {
    response = httpExecute(new HttpPut("http://localhost:9191/graphwalker/restart"));
  }

  @Override
  public void v_RestRunning() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, is("{\"result\":\"ok\"}"));
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());

    response = httpExecute(new HttpGet("http://localhost:9191/graphwalker/getStatistics"));
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    body = getResonseBody();
    logger.debug(body);
    JSONObject responseJSON = new JSONObject(body);
    JSONAssert.assertEquals("Wrong number of edge coverage", "{edgeCoverage:0}", responseJSON, false);
    JSONAssert.assertEquals("Result should be ok", "{result:\"ok\"}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of total visited edges", "{totalNumberOfVisitedEdges:0}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of total number of visited vertices", "{totalNumberOfVisitedVertices:0}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of vertices", "{totalNumberOfVertices:7}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of edges", "{totalNumberOfEdges:12}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of unvisited vertices", "{totalNumberOfUnvisitedVertices:7}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of vertex coverage", "{vertexCoverage:0}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of unvisited edges", "{totalNumberOfUnvisitedEdges:12}", responseJSON, false);
  }

  @Override
  public void e_Load() {
    HttpPost request = new HttpPost("http://localhost:9191/graphwalker/load");
    FileEntity fileEntity = new FileEntity(ResourceUtils.getResourceAsFile("gw3/UC01.json"), ContentType.TEXT_PLAIN);
    request.setEntity(fileEntity);
    response = httpExecute(request);
  }

  @Override
  public void e_HasNext() {
    HttpGet request = new HttpGet("http://localhost:9191/graphwalker/hasNext");
    response = httpExecute(request);
  }

  @Override
  public void v_GetData() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    JSONObject responseJSON = new JSONObject(body);
    JSONAssert.assertEquals("Result should be ok", "{result:\"ok\"}", responseJSON, false);
    JSONAssert.assertEquals("Wrong value of num_of_books", "{data:{num_of_books:\"0\"}}", responseJSON, false);
    JSONAssert.assertEquals("Wrong value of MAX_BOOKS", "{data:{MAX_BOOKS:\"5\"}}", responseJSON, false);
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  private String getResonseBody() {
    try {
      return new BasicResponseHandler().handleResponse(response);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
    return null;
  }

  @Override
  public void v_GetNext() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    JSONObject responseJSON = new JSONObject(body);
    JSONAssert.assertEquals("Wrong number of elements", "{numberOfElements:19}", responseJSON, false);
    JSONAssert.assertEquals("Result should be ok", "{result:\"ok\"}", responseJSON, false);
    JSONAssert.assertEquals("Wrong model name", "{modelName:\"UC01\"}", responseJSON, false);
    JSONAssert.assertEquals("Wrong current element id", "{currentElementID:\"e0\"}", responseJSON, false);
    JSONAssert.assertEquals("Wrong current element name", "{currentElementName:\"e_init\"}", responseJSON, false);
    //TODO: Fix assert below.
    //      see https://www.baeldung.com/jsonassert#advanced-comparison-example
    //JSONAssert.assertEquals("Wrong data", "{data:[{num_of_books:\"0\"},{MAX_BOOKS:\"5\"}]}", responseJSON, new ArraySizeComparator(JSONCompareMode.LENIENT));
    JSONAssert.assertEquals("Wrong number of unvisited elements", "{numberOfUnvisitedElements:18}", responseJSON, false);

    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  @Override
  public void v_GetStatistics() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    JSONObject responseJSON = new JSONObject(body);
    JSONAssert.assertEquals("Wrong number of edge coverage", "{edgeCoverage:8}", responseJSON, false);
    JSONAssert.assertEquals("Result should be ok", "{result:\"ok\"}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of total visited edges", "{totalNumberOfVisitedEdges:1}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of total number of visited vertices", "{totalNumberOfVisitedVertices:0}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of vertices", "{totalNumberOfVertices:7}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of edges", "{totalNumberOfEdges:12}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of unvisited vertices", "{totalNumberOfUnvisitedVertices:7}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of vertex coverage", "{vertexCoverage:0}", responseJSON, false);
    JSONAssert.assertEquals("Wrong number of unvisited edges", "{totalNumberOfUnvisitedEdges:11}", responseJSON, false);
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  @Override
  public void v_HasNext() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, is("{\"result\":\"ok\",\"hasNext\":\"true\"}"));
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  @Override
  public void v_SetData() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, is("{\"result\":\"ok\"}"));
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }
}
