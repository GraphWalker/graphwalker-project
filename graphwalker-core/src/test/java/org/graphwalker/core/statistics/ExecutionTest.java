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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Vertex;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class ExecutionTest {

  private static Vertex.RuntimeVertex vertex = new Vertex().build();
  private static ExecutionContext context = new TestExecutionContext();

  @Test
  public void create() throws Exception {
    Execution execution = new Execution(context, vertex, 100000000L, 50000L);
    assertNotNull(execution);
    assertThat(execution.getContext(), CoreMatchers.is(context));
    assertThat(execution.getElement(), CoreMatchers.is(vertex));
    assertThat(execution.getTime(), is(100000000L));
    assertThat(execution.getDuration(), is(50000L));
    assertThat(execution.getTime(TimeUnit.MILLISECONDS), is(100L));
    assertThat(execution.getDuration(TimeUnit.MICROSECONDS), is(50L));
    assertThat(execution.getTime(TimeUnit.SECONDS), is(0L));
    assertThat(execution.getDuration(TimeUnit.DAYS), is(0L));
  }
}
