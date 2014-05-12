package org.graphwalker.core.condition;

import org.graphwalker.core.machine.ExecutionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Nils Olsson
 */
public class AlternativeCondition implements StopCondition {

    private List<StopCondition> conditions = new ArrayList<>();

    public void add(StopCondition condition) {
        this.conditions.add(condition);
    }

    public List<StopCondition> getStopConditions() {
        return conditions;
    }

    @Override
    public boolean isFulfilled(ExecutionContext context) {
        for (StopCondition condition : conditions) {
            if (condition.isFulfilled(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double getFulfilment(ExecutionContext context) {
        double fulfilment = 0;
        for (StopCondition condition : conditions) {
            double newFulfilment = condition.getFulfilment(context);
            if (newFulfilment > fulfilment) {
                fulfilment = newFulfilment;
            }
        }
        return fulfilment;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("(");
        for (Iterator<StopCondition> i = conditions.iterator(); i.hasNext();) {
            stringBuilder.append(i.next().toString());
            if (i.hasNext()) {
                stringBuilder.append(" OR ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
