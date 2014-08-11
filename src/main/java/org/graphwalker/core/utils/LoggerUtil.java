package org.graphwalker.core.utils;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public final class LoggerUtil {

    public enum Level {
        OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
    }

    public static void setLogLevel(Level level) {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.valueOf(level.name()));
    }

    public static Level getLogLevel() {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        return Level.valueOf(root.getLevel().levelStr);
    }
}
