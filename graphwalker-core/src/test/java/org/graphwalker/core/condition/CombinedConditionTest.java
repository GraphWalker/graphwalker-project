package org.graphwalker.core.condition;

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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class CombinedConditionTest {

  @Test
  public void testConstructor() throws Exception {
    CombinedCondition condition = new CombinedCondition();
    assertNotNull(condition);
    assertNotNull(condition.getStopConditions());
    assertThat(condition.getStopConditions().size(), is(0));
  }

  @Test
  public void testFulfilment() throws Exception {
    CombinedCondition condition = new CombinedCondition();
    condition.addStopCondition(new Never());
    assertThat("Should be zero", condition.getFulfilment(), is(0.0));
    assertThat(condition.toString(), is("Never()"));
  }

  @Test
  public void testIsFulfilled() throws Exception {
    CombinedCondition condition = new CombinedCondition();
    condition.addStopCondition(new Never());
    assertThat("Should be false", condition.isFulfilled(), is(false));
    condition.addStopCondition(new Never());
    assertThat("Should be false", condition.isFulfilled(), is(false));
    condition.addStopCondition(new Never());
    assertThat("Should be false", condition.isFulfilled(), is(false));
    assertThat(condition.toString(), is("Never() AND Never() AND Never()"));
  }
}
