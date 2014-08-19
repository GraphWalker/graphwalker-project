package org.graphwalker.core.statistics;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public final class ProfileUnit {

    private final List<Execution> executions = new LinkedList<>();

    public ProfileUnit(Execution execution) {
        addExecution(execution);
    }

    public void addExecution(Execution executionTime) {
        executions.add(executionTime);
    }

    public long getExecutionCount() {
        return executions.size();
    }

    public long getMinExecutionTime() {
        return getMinExecutionTime(TimeUnit.NANOSECONDS);
    }

    public long getMinExecutionTime(TimeUnit unit) {
        long duration = Long.MAX_VALUE;
        for (Execution execution: executions) {
            if (execution.getDuration() < duration) {
                duration = execution.getDuration();
            }
        }
        return unit.convert(duration, TimeUnit.NANOSECONDS);
    }

    public long getAverageExecutionTime() {
        return getAverageExecutionTime(TimeUnit.NANOSECONDS);
    }

    public long getAverageExecutionTime(TimeUnit unit) {
        long duration = getTotalExecutionTime()/(executions.isEmpty()?1:executions.size());
        return unit.convert(duration, TimeUnit.NANOSECONDS);
    }

    public long getMaxExecutionTime() {
        return getMaxExecutionTime(TimeUnit.NANOSECONDS);
    }

    public long getMaxExecutionTime(TimeUnit unit) {
        long duration = Long.MIN_VALUE;
        for (Execution execution: executions) {
            if (execution.getDuration() > duration) {
                duration = execution.getDuration();
            }
        }
        return unit.convert(duration, TimeUnit.NANOSECONDS);
    }

    public long getTotalExecutionTime() {
        return getTotalExecutionTime(TimeUnit.NANOSECONDS);
    }

    public long getTotalExecutionTime(TimeUnit unit) {
        long duration = 0;
        for (Execution execution: executions) {
            duration += execution.getDuration();
        }
        return unit.convert(duration, TimeUnit.NANOSECONDS);
    }

    public long getFirstExecutionTime(TimeUnit unit) {
        return unit.convert(executions.get(0).getTime(), TimeUnit.NANOSECONDS);
    }

    public long getLastExecutionTime(TimeUnit unit) {
        return unit.convert(executions.get(executions.size()-1).getTime(), TimeUnit.NANOSECONDS);
    }
}
