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
import org.graphwalker.io.factory.gw3.GW3ContextFactory;
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
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;

/**
 * @author Kristian Karl
 */
public class ConvertionContextFactoryTest {
    private static final Logger logger = LoggerFactory.getLogger(ConvertionContextFactoryTest.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void graphml2Gw3() throws IOException {
        Context yedContext = new YEdContextFactory().create(Paths.get("graphml/Login.graphml"));
        Assert.assertNotNull(yedContext);

        File tempFile = testFolder.newFile("test.gw3");
        Context gw3WriteContext = new GW3ContextFactory().write(yedContext, tempFile.toPath());
        Assert.assertNotNull(gw3WriteContext);

        Context gw3ReadContext = new GW3ContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(gw3ReadContext);

        Assert.assertThat(yedContext.getModel().getEdges().size(), is(gw3WriteContext.getModel().getEdges().size()));
        Assert.assertThat(yedContext.getModel().getVertices().size(), is(gw3WriteContext.getModel().getVertices().size()));
    }

    @Test
    public void convertJsonToJson() throws IOException {
        Context jsonContext = new JsonContextFactory().create(Paths.get("json/example.json"));
        Assert.assertNotNull(jsonContext);

        File tempFile = testFolder.newFile("test.json");
        Context jsonWriteContext = new JsonContextFactory().write(jsonContext, tempFile.toPath());
        Assert.assertNotNull(jsonWriteContext);

        Context jsonReadContext = new JsonContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(jsonReadContext);

        Assert.assertThat(jsonContext.getModel().getEdges().size(), is(jsonReadContext.getModel().getEdges().size()));
        Assert.assertThat(jsonContext.getModel().getVertices().size(), is(jsonReadContext.getModel().getVertices().size()));
    }

    @Test
    public void convertJsonToGraphml() throws IOException {
        Context jsonContext = new JsonContextFactory().create(Paths.get("json/example.json"));
        Assert.assertNotNull(jsonContext);

        File tempFile = testFolder.newFile("test.graphml");
        Context yedWriteContext = new YEdContextFactory().write(jsonContext, tempFile.toPath());
        Assert.assertNotNull(yedWriteContext);

        Context yedReadContext = new YEdContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(yedReadContext);

        // Since edges without source vertex is not allowed in yed/graphml, the yedReadContext will be one edge short
        Assert.assertThat(jsonContext.getModel().getEdges().size(), is(yedReadContext.getModel().getEdges().size() + 1));
        Assert.assertThat(jsonContext.getModel().getVertices().size(), is(yedReadContext.getModel().getVertices().size()));
    }

    @Test
    public void convertJsonToJava() throws IOException {
        Context jsonContext = new JsonContextFactory().create(Paths.get("json/example.json"));
        Assert.assertNotNull(jsonContext);

        File tempFile = testFolder.newFile("test.java");
        Context javaWriteContext = new JavaContextFactory().write(jsonContext, tempFile.toPath());
        Assert.assertNotNull(javaWriteContext);

        Assert.assertThat(jsonContext.getModel().getEdges().size(), is(javaWriteContext.getModel().getEdges().size()));
        Assert.assertThat(jsonContext.getModel().getVertices().size(), is(javaWriteContext.getModel().getVertices().size()));
    }

    @Test
    public void convertJsonToDot() throws IOException {
        Context jsonContext = new JsonContextFactory().create(Paths.get("json/example.json"));
        Assert.assertNotNull(jsonContext);

        File tempFile = testFolder.newFile("test.dot");
        Context dotWriteContext = new DotContextFactory().write(jsonContext, tempFile.toPath());
        Assert.assertNotNull(dotWriteContext);

        Context dotReadContext = new DotContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(dotReadContext);

        Assert.assertThat(jsonContext.getModel().getEdges().size(), is(dotReadContext.getModel().getEdges().size()));
        Assert.assertThat(jsonContext.getModel().getVertices().size(), is(dotReadContext.getModel().getVertices().size()));
    }

    @Test
    public void convertGraphmlToGraphml() throws IOException {
        Context yEdContext = new YEdContextFactory().create(Paths.get("graphml/UC01_GW2.graphml"));
        Assert.assertNotNull(yEdContext);

        File tempFile = testFolder.newFile("test.graphml");
        Context yedWriteContext = new YEdContextFactory().write(yEdContext, tempFile.toPath());
        Assert.assertNotNull(yedWriteContext);

        Context yedReadContext = new YEdContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(yedReadContext);

        // Since edges without source vertex is not allowed in yed/graphml, the yedReadContext will be one edge short
        Assert.assertThat(yEdContext.getModel().getEdges().size(), is(yedReadContext.getModel().getEdges().size() + 1));
        Assert.assertThat(yEdContext.getModel().getVertices().size(), is(yedReadContext.getModel().getVertices().size()));
    }


    @Test
    public void convertGraphmlToDot() throws IOException {
        Context yEdContext = new YEdContextFactory().create(Paths.get("graphml/UC01_GW2.graphml"));
        Assert.assertNotNull(yEdContext);

        File tempFile = testFolder.newFile("test.dot");
        Context dotWriteContext = new DotContextFactory().write(yEdContext, tempFile.toPath());
        Assert.assertNotNull(dotWriteContext);

        Context dotReadContext = new DotContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(dotReadContext);

        Assert.assertThat(yEdContext.getModel().getEdges().size(), is(dotReadContext.getModel().getEdges().size()));
        Assert.assertThat(yEdContext.getModel().getVertices().size(), is(dotReadContext.getModel().getVertices().size()));
    }

    @Test
    public void convertGraphmlToJava() throws IOException {
        Context yEdContext = new YEdContextFactory().create(Paths.get("graphml/UC01_GW2.graphml"));
        Assert.assertNotNull(yEdContext);

        File tempFile = testFolder.newFile("test.java");
        Context javaWriteContext = new JavaContextFactory().write(yEdContext, tempFile.toPath());
        Assert.assertNotNull(javaWriteContext);

        Assert.assertThat(yEdContext.getModel().getEdges().size(), is(javaWriteContext.getModel().getEdges().size()));
        Assert.assertThat(yEdContext.getModel().getVertices().size(), is(javaWriteContext.getModel().getVertices().size()));
    }

    @Test
    public void graphml2Json() throws IOException {
        Context yedContext = new YEdContextFactory().create(Paths.get("graphml/PetClinicSharedState.graphml"));
        Assert.assertNotNull(yedContext);

        File testFile = testFolder.newFile("test.json");
        Context jsonWriteContext = new JsonContextFactory().write(yedContext, testFile.toPath());
        Assert.assertNotNull(jsonWriteContext);

        Context jsonReadContext = new JsonContextFactory().create(testFile.toPath());
        Assert.assertNotNull(jsonReadContext);

        Assert.assertThat(yedContext.getModel().getEdges().size(), is(jsonWriteContext.getModel().getEdges().size()));
        Assert.assertThat(yedContext.getModel().getVertices().size(), is(jsonWriteContext.getModel().getVertices().size()));
    }

    @Test
    public void convertDotToDot() throws IOException {
        Context dotContext = new DotContextFactory().create(Paths.get("dot/Login.dot"));
        Assert.assertNotNull(dotContext);

        File tempFile = testFolder.newFile("test.dot");
        Context dotWriteContext = new DotContextFactory().write(dotContext, tempFile.toPath());
        Assert.assertNotNull(dotWriteContext);

        Context dotReadContext = new DotContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(dotReadContext);

        Assert.assertThat(dotContext.getModel().getEdges().size(), is(dotReadContext.getModel().getEdges().size()));
        Assert.assertThat(dotContext.getModel().getVertices().size(), is(dotReadContext.getModel().getVertices().size()));
    }

    @Test
    public void convertDotToJava() throws IOException {
        Context dotContext = new DotContextFactory().create(Paths.get("dot/Login.dot"));
        Assert.assertNotNull(dotContext);

        File tempFile = testFolder.newFile("test.java");
        Context javaWriteContext = new JavaContextFactory().write(dotContext, tempFile.toPath());
        Assert.assertNotNull(javaWriteContext);

        Assert.assertThat(dotContext.getModel().getEdges().size(), is(javaWriteContext.getModel().getEdges().size()));
        Assert.assertThat(dotContext.getModel().getVertices().size(), is(javaWriteContext.getModel().getVertices().size()));
    }

    @Test
    public void convertDotToGraphml() throws IOException {
        Context dotContext = new DotContextFactory().create(Paths.get("dot/Login.dot"));
        Assert.assertNotNull(dotContext);

        File tempFile = testFolder.newFile("test.graphml");
        Context yedWriteContext = new YEdContextFactory().write(dotContext, tempFile.toPath());
        Assert.assertNotNull(yedWriteContext);

        Context yedReadContext = new YEdContextFactory().create(tempFile.toPath());
        Assert.assertNotNull(yedReadContext);

        // Since edges without source vertex is not allowed in yed/graphml, the yedReadContext will be one edge short
        Assert.assertThat(dotContext.getModel().getEdges().size(), is(yedReadContext.getModel().getEdges().size() + 1));
        Assert.assertThat(dotContext.getModel().getVertices().size(), is(yedReadContext.getModel().getVertices().size()));
    }

    @Test
    public void convertDotToJson() throws IOException {
        Context dotContext = new DotContextFactory().create(Paths.get("dot/Login.dot"));
        Assert.assertNotNull(dotContext);

        File testFile = testFolder.newFile("test.json");
        Context jsonWriteContext = new JsonContextFactory().write(dotContext, testFile.toPath());
        Assert.assertNotNull(jsonWriteContext);

        Context jsonReadContext = new JsonContextFactory().create(testFile.toPath());
        Assert.assertNotNull(jsonReadContext);

        Assert.assertThat(dotContext.getModel().getEdges().size(), is(jsonWriteContext.getModel().getEdges().size()));
        Assert.assertThat(dotContext.getModel().getVertices().size(), is(jsonWriteContext.getModel().getVertices().size()));
    }
}
