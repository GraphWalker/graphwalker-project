package org.graphwalker.core.condition;

import org.graphwalker.core.machine.ExecutionContext;

import javax.script.ScriptException;

/**
 * @author Nils Olsson
 */
public class InternalState implements StopCondition {

    private final String script;

    public InternalState(String script) {
        this.script = script;
    }

    @Override
    public boolean isFulfilled(ExecutionContext context) {
        try {
            Object value = context.getScriptEngine().eval(script);
            if (value instanceof Boolean) {
                return (Boolean)value;
            } else {
                throw new StopConditionException("Wrong type of expression");
            }
        } catch (ScriptException e) {
            throw new StopConditionException(e);
        }
    }

    @Override
    public double getFulfilment(ExecutionContext context) {
        return isFulfilled(context)?1.0:0.0;
    }
}
