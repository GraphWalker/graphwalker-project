package org.graphwalker.io.factory.gw3;

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
import org.graphwalker.core.model.Element;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Kristian Karl
 */
public class GW3ContextFactoryTest {
  private static final Logger logger = LoggerFactory.getLogger(GW3ContextFactoryTest.class);

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void PetClinic() {
    List<Context> contexts = new GW3ContextFactory().createMultiple(Paths.get("gw3/petClinic.gw3"));
    Assert.assertNotNull(contexts);
    Assert.assertThat(contexts.size(), is(5));
  }

  @Test
  public void PetClinicWithSimpleMachine() {
    SimpleMachine machine = new SimpleMachine(new GW3ContextFactory().createMultiple(Paths.get("gw3/petClinic.gw3")));
    while (machine.hasNextStep()) {
      Element e = machine.getNextStep().getCurrentElement();
      logger.debug(e.getName());
    }
  }
}
