package org.graphwalker.core.generator;

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.ExecutionContext;

/**
 * @author Nils Olsson
 */
public final class ShortestAllPaths implements PathGenerator {

    private final StopCondition stopCondition;

    public ShortestAllPaths(StopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    @Override
    public StopCondition getStopCondition() {
        return stopCondition;
    }

    @Override
    public ExecutionContext getNextStep(ExecutionContext context) {

        return null;
    }

    @Override
    public boolean hasNextStep(ExecutionContext context) {
        return !getStopCondition().isFulfilled(context);
    }
}
