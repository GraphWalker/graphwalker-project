package org.graphwalker.core.statistics;

/*
 * #%L
 * GraphWalker Core
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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public final class ProfileUnitTest {

  @Test
  public void measure() {
    ProfileUnit profileUnit = new ProfileUnit(new Execution(0L, 350L));
    profileUnit.addExecution(new Execution(350L, 150L));
    Assert.assertNotNull(profileUnit);
    Assert.assertThat(profileUnit.getExecutionCount(), is(2L));
    Assert.assertThat(profileUnit.getAverageExecutionTime(), is(250L));
    Assert.assertThat(profileUnit.getMinExecutionTime(), is(150L));
    Assert.assertThat(profileUnit.getMaxExecutionTime(), is(350L));
    Assert.assertThat(profileUnit.getTotalExecutionTime(), is(500L));
  }
}
