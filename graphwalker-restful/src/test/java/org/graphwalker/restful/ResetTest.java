package org.graphwalker.restful;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.graphwalker.io.common.ResourceUtils;
import org.json.JSONObject;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

public class ResetTest {

  private static HttpServer server;

  @Before
  public void startServer() throws Exception {
    ResourceConfig resourceConfig = new DefaultResourceConfig();
    Restful restful = new Restful(null, true, true, true);
    resourceConfig.getSingletons().add(restful);
    server = GrizzlyServerFactory.createHttpServer("http://0.0.0.0:9192", resourceConfig);
    server.start();
  }

  @After
  public void stopServer() {
    server.stop();
  }

  @Test
  public void resetEmptyMachine() throws IOException {
    try (CloseableHttpClient client = HttpClientBuilder.create() .build()) {
      CloseableHttpResponse response = client.execute(new HttpPut("http://localhost:9192/graphwalker/restart"));
      JSONObject responseJSON = new JSONObject(EntityUtils.toString(response.getEntity()));
      JSONAssert.assertEquals("Result should be nok", "{result:\"nok\",\"error\":\"No model(s) are loaded.\"}", responseJSON, false);
    }
  }

  @Test
  public void resetNotYetStartedMachine() throws IOException {
    try (CloseableHttpClient client = HttpClientBuilder.create() .build()) {
      // Load model
      HttpPost load = new HttpPost("http://localhost:9192/graphwalker/load");
      load.setEntity(new FileEntity(ResourceUtils.getResourceAsFile("gw3/UC01.json"), ContentType.TEXT_PLAIN));
      client.execute(load);
      // Reset model without taking any steps
      CloseableHttpResponse response = client.execute(new HttpPut("http://localhost:9192/graphwalker/restart"));
      JSONObject responseJSON = new JSONObject(EntityUtils.toString(response.getEntity()));
      JSONAssert.assertEquals("Result should be ok", "{result:\"ok\"}", responseJSON, false);
    }
  }
}
