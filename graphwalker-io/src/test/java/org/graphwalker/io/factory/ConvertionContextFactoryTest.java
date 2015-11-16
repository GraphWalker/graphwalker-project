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
import org.graphwalker.io.factory.gw3.GW3ContextFactory;
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
    public void PetClinicSharedStateGraphml2Json() throws IOException {
        Context yedContext = new YEdContextFactory().create(Paths.get("graphml/PetClinicSharedState.graphml"));
        Assert.assertNotNull(yedContext);

        File testFile = testFolder.newFile("PetClinicSharedState.json");
        Context jsonWriteContext = new JsonContextFactory().write(yedContext, testFile.toPath());
        Assert.assertNotNull(jsonWriteContext);

        Context jsonReadContext = new JsonContextFactory().create(testFile.toPath());
        Assert.assertNotNull(jsonReadContext);

        Assert.assertThat(yedContext.getNextElement().getId(), is(jsonWriteContext.getNextElement().getId()));
        Assert.assertThat(yedContext.getModel().getEdges().size(), is(jsonWriteContext.getModel().getEdges().size()));
        Assert.assertThat(yedContext.getModel().getVertices().size(), is(jsonWriteContext.getModel().getVertices().size()));
        Assert.assertThat(yedContext.getNextElement().getId(), is(jsonReadContext.getNextElement().getId()));
        Assert.assertThat(yedContext.getModel().getEdges().size(), is(jsonReadContext.getModel().getEdges().size()));
        Assert.assertThat(yedContext.getModel().getVertices().size(), is(jsonReadContext.getModel().getVertices().size()));
    }

    @Test
    public void LoginGraphml2Gw3() throws IOException {
        Context yedContext = new YEdContextFactory().create(Paths.get("graphml/Login.graphml"));
        Assert.assertNotNull(yedContext);

        File testFile = testFolder.newFile("Login.gw3");
        Context gw3WriteContext = new GW3ContextFactory().write(yedContext, testFile.toPath());
        Assert.assertNotNull(gw3WriteContext);

        Context gw3ReadContext = new GW3ContextFactory().create(testFile.toPath());
        Assert.assertNotNull(gw3ReadContext);

        Assert.assertThat(yedContext.getNextElement().getId(), is(gw3WriteContext.getNextElement().getId()));
        Assert.assertThat(yedContext.getModel().getEdges().size(), is(gw3WriteContext.getModel().getEdges().size()));
        Assert.assertThat(yedContext.getModel().getVertices().size(), is(gw3WriteContext.getModel().getVertices().size()));
        Assert.assertThat(yedContext.getNextElement().getId(), is(gw3ReadContext.getNextElement().getId()));
        Assert.assertThat(yedContext.getModel().getEdges().size(), is(gw3ReadContext.getModel().getEdges().size()));
        Assert.assertThat(yedContext.getModel().getVertices().size(), is(gw3ReadContext.getModel().getVertices().size()));
    }

}
