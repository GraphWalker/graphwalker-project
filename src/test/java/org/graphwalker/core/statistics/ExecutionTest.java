package org.graphwalker.core.statistics;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class ExecutionTest {

    @Test
    public void create() {
        Execution execution = new Execution(100000000L, 50000L);
        Assert.assertNotNull(execution);
        Assert.assertThat(execution.getTimestamp(), is(100000000L));
        Assert.assertThat(execution.getDuration(), is(50000L));
        Assert.assertThat(execution.getTimestamp(TimeUnit.MILLISECONDS), is(100L));
        Assert.assertThat(execution.getDuration(TimeUnit.MICROSECONDS), is(50L));
        Assert.assertThat(execution.getTimestamp(TimeUnit.SECONDS), is(0L));
        Assert.assertThat(execution.getDuration(TimeUnit.DAYS), is(0L));
    }
}
