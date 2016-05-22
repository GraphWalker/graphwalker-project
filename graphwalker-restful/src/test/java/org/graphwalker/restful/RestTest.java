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

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import org.graphwalker.java.test.Result;
import org.graphwalker.java.test.TestExecutor;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.hamcrest.core.Is.is;

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
  public void StartServer() throws Exception {
    ResourceConfig rc = new DefaultResourceConfig();
    rest = new Restful(null, true, true);
    rc.getSingletons().add(rest);

    String url = "http://0.0.0.0:" + 9191;

    server = GrizzlyServerFactory.createHttpServer(url, rc);
    logger.debug("Starting RestFul service");
    server.start();
  }

  @AfterExecution
  public void StopServer() {
    logger.debug("Stopping RestFul service");
    server.stop();
  }

  @Test
  public void TestRun() {
    Result result = new TestExecutor(getClass()).execute(true);
    if (result.hasErrors()) {
      for (String error : result.getErrors()) {
        System.out.println(error);
      }
      Assert.fail("Test run failed.");
    }
  }

  @Override
  public void e_GetData() {
    HttpGet request = new HttpGet("http://localhost:9191/graphwalker/getData");
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void e_SetData() {
    HttpPut request = new HttpPut("http://localhost:9191/graphwalker/setData/MAX_BOOKS=6;");
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void v_EmptyMachine() {
    Assert.assertNull(rest.getContexts());
    Assert.assertNull(rest.getMachine());
  }

  @Override
  public void e_GetStatistics() {
    HttpGet request = new HttpGet("http://localhost:9191/graphwalker/getStatistics");
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void e_GetNext() {
    HttpGet request = new HttpGet("http://localhost:9191/graphwalker/getNext");
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void e_Restart() {
    HttpPut request = new HttpPut("http://localhost:9191/graphwalker/restart");
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void v_RestRunning() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, is("{\"result\":\"ok\"}"));
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  @Override
  public void e_Load() {
    HttpPost request = new HttpPost("http://localhost:9191/graphwalker/load");
    FileEntity fileEntity = new FileEntity(ResourceUtils.getResourceAsFile("gw3/UC01.json"), ContentType.TEXT_PLAIN);
    request.setEntity(fileEntity);
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void e_HasNext() {
    HttpGet request = new HttpGet("http://localhost:9191/graphwalker/hasNext");
    try {
      response = HttpClientBuilder.create().build().execute(request);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void v_GetData() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, new StringContains("\"result\":\"ok\""));
    Assert.assertThat(body, new StringContains("\"num_of_books\":\"0\""));
    Assert.assertThat(body, new StringContains("\"MAX_BOOKS\":\"5\""));
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  private String getResonseBody() {
    ResponseHandler<String> handler = new BasicResponseHandler();
    String body = null;
    try {
      body = handler.handleResponse(response);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return body;
  }

  @Override
  public void v_GetNext() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, new StringContains("\"numberOfElements\":19"));
    Assert.assertThat(body, new StringContains("\"result\":\"ok\""));
    Assert.assertThat(body, new StringContains("\"modelName\":\"UC01_GW2\""));
    Assert.assertThat(body, new StringContains("\"currentElementID\":\"e0\""));
    Assert.assertThat(body, new StringContains("\"currentElementName\":\"e_init\""));
    Assert.assertThat(body, new StringContains("\"data\":\\[\\{\"num_of_books\":\"0\"\\},\\{\"MAX_BOOKS\":\"5\"\\}\\]"));
    Assert.assertThat(body, new StringContains("\"numberOfUnvisitedElements\":18"));
    Assert.assertNotNull(rest.getContexts());
    Assert.assertNotNull(rest.getMachine());
  }

  @Override
  public void v_GetStatistics() {
    Assert.assertThat(response.getStatusLine().getStatusCode(), is(200));
    String body = getResonseBody();
    logger.debug(body);
    Assert.assertThat(body, new StringContains("\"edgeCoverage\":8"));
    Assert.assertThat(body, new StringContains("\"result\":\"ok\""));
    Assert.assertThat(body, new StringContains("\"totalNumberOfVisitedEdges\":1"));
    Assert.assertThat(body, new StringContains("\"totalNumberOfVisitedVertices\":0"));
    Assert.assertThat(body, new StringContains("\"totalNumberOfVertices\":7"));
    Assert.assertThat(body, new StringContains("\"totalNumberOfEdges\":12"));
    Assert.assertThat(body, new StringContains("\"totalNumberOfUnvisitedVertices\":7"));
    Assert.assertThat(body, new StringContains("\"vertexCoverage\":0"));
    Assert.assertThat(body, new StringContains("\"totalNumberOfUnvisitedEdges\":11"));
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
