package org.graphwalker.io.factory.json;

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
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.io.factory.dot.DotContextFactory;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;

/**
 * @author Kristian Karl
 */
public class JsonContextFactoryTest {

    @Test
    public void SmallModel() {
        Context context = new JsonContextFactory().create(Paths.get("json/SmallModel.json"));
        Assert.assertThat(context.getModel().getVertices().size(), is(2));
        Assert.assertThat(context.getModel().getEdges().size(), is(4));

        Assert.assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getName(), is("v_VerifySomeAction"));
        Assert.assertThat(context.getModel().findVertices("v_VerifySomeAction").get(0).getId(), is("n0"));

        Assert.assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getName(), is("v_VerifySomeOtherAction"));
        Assert.assertThat(context.getModel().findVertices("v_VerifySomeOtherAction").get(0).getId(), is("n1"));
    }

    @Test
    public void SmallModelSimpleMachine() {
        SimpleMachine machine = new SimpleMachine(new JsonContextFactory().create(Paths.get("json/SmallModel.json")));
        while (machine.hasNextStep()) {
            System.out.println(machine.getNextStep().getCurrentElement().getName());
        }
    }
}
