package org.graphwalker.io.factory;

/*
 * #%L
 * GraphWalker Input/Output
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
import org.graphwalker.io.factory.dot.DotContextFactory;
import org.graphwalker.io.factory.java.JavaContextFactory;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.io.factory.yed.YEdContextFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Kristian Karl
 */
public class ConvertionContextFactoryTest {
  private static final Logger logger = LoggerFactory.getLogger(ConvertionContextFactoryTest.class);

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void convertJsonToJson() throws IOException {
    List<Context> jsonContexts = new JsonContextFactory().create(Paths.get("json/example.json"));
    Assert.assertNotNull(jsonContexts);
    Assert.assertThat(jsonContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new JsonContextFactory().write(jsonContexts, tmpFolder);

    List<Context> jsonReadContexts = new JsonContextFactory().create(tmpFolder);
    Assert.assertNotNull(jsonReadContexts);
    Assert.assertThat(jsonReadContexts.size(), is(1));
    Context jsonReadContext = jsonReadContexts.get(0);

    Assert.assertThat(jsonContexts.get(0).getModel().getEdges().size(), is(jsonReadContext.getModel().getEdges().size()));
    Assert.assertThat(jsonContexts.get(0).getModel().getVertices().size(), is(jsonReadContext.getModel().getVertices().size()));
  }

  @Test
  public void convertJsonToGraphml() throws IOException {
    List<Context> jsonContexts = new JsonContextFactory().create(Paths.get("json/example.json"));
    Assert.assertNotNull(jsonContexts);
    Assert.assertThat(jsonContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new YEdContextFactory().write(jsonContexts, tmpFolder);

    List<Context> yedReadContexts = new YEdContextFactory().create(tmpFolder);
    Assert.assertNotNull(yedReadContexts);
    Assert.assertThat(yedReadContexts.size(), is(1));
    Context yedReadContext = yedReadContexts.get(0);

    // Since edges without source vertex is not allowed in yed/graphml, the yedReadContext will be one edge short
    Assert.assertThat(jsonContexts.get(0).getModel().getEdges().size(), is(yedReadContext.getModel().getEdges().size() + 1));
    Assert.assertThat(jsonContexts.get(0).getModel().getVertices().size(), is(yedReadContext.getModel().getVertices().size()));
  }

  @Test
  public void convertJsonToJava() throws IOException {
    List<Context> jsonContexts = new JsonContextFactory().create(Paths.get("json/example.json"));
    Assert.assertNotNull(jsonContexts);
    Assert.assertThat(jsonContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new JavaContextFactory().write(jsonContexts, tmpFolder);
  }

  @Test
  public void convertJsonToDot() throws IOException {
    List<Context> jsonContexts = new JsonContextFactory().create(Paths.get("json/example.json"));
    Assert.assertNotNull(jsonContexts);
    Assert.assertThat(jsonContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new DotContextFactory().write(jsonContexts, tmpFolder);

    List<Context> dotReadContexts = new DotContextFactory().create(tmpFolder);
    Assert.assertNotNull(dotReadContexts);

    Assert.assertThat(jsonContexts.get(0).getModel().getEdges().size(), is(dotReadContexts.get(0).getModel().getEdges().size()));
    Assert.assertThat(jsonContexts.get(0).getModel().getVertices().size(), is(dotReadContexts.get(0).getModel().getVertices().size()));
  }

  @Test
  public void convertGraphmlToGraphml() throws IOException {
    List<Context> yEdContexts = new YEdContextFactory().create(Paths.get("graphml/UC01_GW2.graphml"));
    Assert.assertNotNull(yEdContexts);
    Assert.assertThat(yEdContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new YEdContextFactory().write(yEdContexts, tmpFolder);

    List<Context> yedReadContexts = new YEdContextFactory().create(tmpFolder);
    Assert.assertNotNull(yedReadContexts);
    Assert.assertThat(yEdContexts.size(), is(1));

    // Since edges without source vertex is not allowed in yed/graphml, the yedReadContext will be one edge short
    Assert.assertThat(yEdContexts.get(0).getModel().getEdges().size(), is(yedReadContexts.get(0).getModel().getEdges().size() + 1));
    Assert.assertThat(yEdContexts.get(0).getModel().getVertices().size(), is(yedReadContexts.get(0).getModel().getVertices().size()));
  }


  @Test
  public void convertGraphmlToDot() throws IOException {
    List<Context> yEdContexts = new YEdContextFactory().create(Paths.get("graphml/UC01_GW2.graphml"));
    Assert.assertNotNull(yEdContexts);
    Assert.assertThat(yEdContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new DotContextFactory().write(yEdContexts, tmpFolder);

    List<Context> dotReadContexts = new DotContextFactory().create(tmpFolder);
    Assert.assertNotNull(dotReadContexts);
    Assert.assertThat(dotReadContexts.size(), is(1));

    Assert.assertThat(yEdContexts.get(0).getModel().getEdges().size(), is(dotReadContexts.get(0).getModel().getEdges().size()));
    Assert.assertThat(yEdContexts.get(0).getModel().getVertices().size(), is(dotReadContexts.get(0).getModel().getVertices().size()));
  }

  @Test
  public void convertGraphmlToJava() throws IOException {
    List<Context> yEdContexts = new YEdContextFactory().create(Paths.get("graphml/UC01_GW2.graphml"));
    Assert.assertNotNull(yEdContexts);
    Assert.assertThat(yEdContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new JavaContextFactory().write(yEdContexts, tmpFolder);
  }

  @Test
  public void graphml2Json() throws IOException {
    List<Context> yEdContexts = new YEdContextFactory().create(Paths.get("graphml/PetClinicSharedState.graphml"));
    Assert.assertNotNull(yEdContexts);
    Assert.assertThat(yEdContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new JsonContextFactory().write(yEdContexts, tmpFolder);

    List<Context> jsonReadContexts = new JsonContextFactory().create(tmpFolder);
    Assert.assertNotNull(jsonReadContexts);
    Assert.assertThat(jsonReadContexts.size(), is(1));

    Assert.assertThat(yEdContexts.get(0).getModel().getEdges().size(), is(jsonReadContexts.get(0).getModel().getEdges().size()));
    Assert.assertThat(yEdContexts.get(0).getModel().getVertices().size(), is(jsonReadContexts.get(0).getModel().getVertices().size()));
  }

  @Test
  public void convertDotToDot() throws IOException {
    List<Context> dotContexts = new DotContextFactory().create(Paths.get("dot/Login.dot"));
    Assert.assertNotNull(dotContexts);
    Assert.assertThat(dotContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new DotContextFactory().write(dotContexts, tmpFolder);

    List<Context> dotReadContexts = new DotContextFactory().create(tmpFolder);
    Assert.assertNotNull(dotReadContexts);
    Assert.assertThat(dotReadContexts.size(), is(1));

    Assert.assertThat(dotContexts.get(0).getModel().getEdges().size(), is(dotReadContexts.get(0).getModel().getEdges().size()));
    Assert.assertThat(dotContexts.get(0).getModel().getVertices().size(), is(dotReadContexts.get(0).getModel().getVertices().size()));
  }

  @Test
  public void convertDotToJava() throws IOException {
    List<Context> dotContexts = new DotContextFactory().create(Paths.get("dot/Login.dot"));
    Assert.assertNotNull(dotContexts);
    Assert.assertThat(dotContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new JavaContextFactory().write(dotContexts, tmpFolder);
  }

  @Test
  public void convertDotToGraphml() throws IOException {
    List<Context> dotContexts = new DotContextFactory().create(Paths.get("dot/Login.dot"));
    Assert.assertNotNull(dotContexts);
    Assert.assertThat(dotContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new YEdContextFactory().write(dotContexts, tmpFolder);

    List<Context> yedReadContexts = new YEdContextFactory().create(tmpFolder);
    Assert.assertNotNull(yedReadContexts);
    Assert.assertThat(dotContexts.size(), is(1));

    // Since edges without source vertex is not allowed in yed/graphml, the yedReadContext will be one edge short
    Assert.assertThat(dotContexts.get(0).getModel().getEdges().size(), is(yedReadContexts.get(0).getModel().getEdges().size() + 1));
    Assert.assertThat(dotContexts.get(0).getModel().getVertices().size(), is(yedReadContexts.get(0).getModel().getVertices().size()));
  }

  @Test
  public void convertDotToJson() throws IOException {
    List<Context> dotContexts = new DotContextFactory().create(Paths.get("dot/Login.dot"));
    Assert.assertNotNull(dotContexts);
    Assert.assertThat(dotContexts.size(), is(1));

    Path tmpFolder = testFolder.getRoot().toPath();
    new JsonContextFactory().write(dotContexts, tmpFolder);

    List<Context> jsonReadContexts = new JsonContextFactory().create(tmpFolder);
    Assert.assertNotNull(jsonReadContexts);
    Assert.assertThat(jsonReadContexts.size(), is(1));
    Context jsonReadContext = jsonReadContexts.get(0);

    Assert.assertThat(dotContexts.get(0).getModel().getEdges().size(), is(jsonReadContext.getModel().getEdges().size()));
    Assert.assertThat(dotContexts.get(0).getModel().getVertices().size(), is(jsonReadContext.getModel().getVertices().size()));
  }
}
