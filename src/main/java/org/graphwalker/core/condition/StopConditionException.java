package org.graphwalker.core.condition;

/**
 * @author Nils Olsson
 */
public class StopConditionException extends RuntimeException {

    public StopConditionException(String message) {
        super(message);
    }

    public StopConditionException(Throwable throwable) {
        super(throwable);
    }
}
