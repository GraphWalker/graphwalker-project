package org.graphwalker.core.generator;

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.ExecutionContext;

/**
 * @author Nils Olsson
 */
public final class ShortestAllPaths implements PathGenerator {

    @Override
    public StopCondition getStopCondition() {
        return null;
    }

    @Override
    public ExecutionContext getNextStep(ExecutionContext context) {
        return null;
    }

    @Override
    public boolean hasNextStep(ExecutionContext context) {
        return false;
    }
}
