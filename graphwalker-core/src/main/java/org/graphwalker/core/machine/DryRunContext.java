package org.graphwalker.core.machine;

/**
 * @author Nils Olsson
 */
public final class DryRunContext extends ExecutionContext {

    public DryRunContext(Context context) {
        setModel(context.getModel());
        setPathGenerator(context.getPathGenerator());
        setNextElement(context.getNextElement());
    }
}
