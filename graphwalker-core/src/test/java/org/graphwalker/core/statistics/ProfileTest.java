package org.graphwalker.core.statistics;

/*-
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProfileTest {

  private static Vertex.RuntimeVertex vertex = new Vertex().build();
  private static ExecutionContext context = new TestExecutionContext();

  @Test
  public void singleExecutionProfile() throws Exception {
    Profile profile = new Profile(context, vertex, Arrays.asList(
      new Execution(context, vertex, 100, 1000)
    ));
    assertThat(profile.getContext(), is(context));
    assertThat(profile.getElement(), is(vertex));
    assertThat(profile.getAverageExecutionTime(), is(1000L));
    assertThat(profile.getMinExecutionTime(), is(1000L));
    assertThat(profile.getMaxExecutionTime(), is(1000L));
    assertThat(profile.getFirstExecutionTime(), is(1000L));
    assertThat(profile.getLastExecutionTime(), is(1000L));
    assertThat(profile.getTotalExecutionTime(), is(1000L));
    assertThat(profile.getExecutionCount(), is(1L));
    assertThat(profile.getAverageExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getMinExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getMaxExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getFirstExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getLastExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getTotalExecutionTime(TimeUnit.MICROSECONDS), is(1L));
  }

  @Test
  public void multipleExecutionsProfile() throws Exception {
    Profile profile = new Profile(context, vertex, Arrays.asList(
      new Execution(context, vertex, 100, 1000),
      new Execution(context, vertex, 100, 3000),
      new Execution(context, vertex, 100, 2000)
    ));
    assertThat(profile.getContext(), is(context));
    assertThat(profile.getElement(), is(vertex));
    assertThat(profile.getAverageExecutionTime(), is(2000L));
    assertThat(profile.getMinExecutionTime(), is(1000L));
    assertThat(profile.getMaxExecutionTime(), is(3000L));
    assertThat(profile.getFirstExecutionTime(), is(1000L));
    assertThat(profile.getLastExecutionTime(), is(2000L));
    assertThat(profile.getTotalExecutionTime(), is(6000L));
    assertThat(profile.getExecutionCount(), is(3L));
    assertThat(profile.getAverageExecutionTime(TimeUnit.MICROSECONDS), is(2L));
    assertThat(profile.getMinExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getMaxExecutionTime(TimeUnit.MICROSECONDS), is(3L));
    assertThat(profile.getFirstExecutionTime(TimeUnit.MICROSECONDS), is(1L));
    assertThat(profile.getLastExecutionTime(TimeUnit.MICROSECONDS), is(2L));
    assertThat(profile.getTotalExecutionTime(TimeUnit.MICROSECONDS), is(6L));
  }

  @Test
  public void emptyProfile() throws Exception {
    Profile profile = new Profile(context, vertex, Collections.emptyList());
    assertThat(profile.getContext(), is(context));
    assertThat(profile.getElement(), is(vertex));
    assertThat(profile.getExecutionCount(), is(0L));
    assertThat(profile.getTotalExecutionTime(), is(0L));
  }

  @Test(expected = MissingExecutionException.class)
  public void noAverageExecutionTime() throws Exception {
    new Profile(context, vertex, Collections.emptyList()).getAverageExecutionTime();
  }

  @Test(expected = MissingExecutionException.class)
  public void noMinExecutionTime() throws Exception {
    new Profile(context, vertex, Collections.emptyList()).getMinExecutionTime();
  }

  @Test(expected = MissingExecutionException.class)
  public void noMaxExecutionTime() throws Exception {
    new Profile(context, vertex, Collections.emptyList()).getMaxExecutionTime();
  }

  @Test(expected = MissingExecutionException.class)
  public void noFirstExecutionTime() throws Exception {
    new Profile(context, vertex, Collections.emptyList()).getFirstExecutionTime();
  }

  @Test(expected = MissingExecutionException.class)
  public void noLastExecutionTime() throws Exception {
    new Profile(context, vertex, Collections.emptyList()).getLastExecutionTime();
  }
}
