package org.graphwalker.core.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class LoggerUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtilTest.class);

    @Test
    public void changeLevel() {
        LoggerUtil.setLogLevel(LoggerUtil.Level.OFF);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.OFF));
        logger.debug("This won't be seen 1");
        LoggerUtil.setLogLevel(LoggerUtil.Level.ERROR);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.ERROR));
        logger.debug("This won't be seen 2");
        LoggerUtil.setLogLevel(LoggerUtil.Level.WARN);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.WARN));
        logger.warn("Oops");
        LoggerUtil.setLogLevel(LoggerUtil.Level.INFO);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.INFO));
        logger.warn("Order restored");
        LoggerUtil.setLogLevel(LoggerUtil.Level.DEBUG);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.DEBUG));
        logger.warn("Warning due to a faulty line");
        LoggerUtil.setLogLevel(LoggerUtil.Level.TRACE);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.TRACE));
        LoggerUtil.setLogLevel(LoggerUtil.Level.ALL);
        Assert.assertThat(LoggerUtil.getLogLevel(), is(LoggerUtil.Level.ALL));
    }
}
