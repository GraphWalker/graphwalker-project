package org.graphwalker.core.statistics;

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

import java.util.Arrays;
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
}
