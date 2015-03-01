package org.graphwalker.core.machine;

import org.graphwalker.core.model.Element;
import org.graphwalker.core.statistics.Profiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <h1>ReplayMachine</h1>
 * ReplayMachine can re-run an previous executed path..
 * <p/>
 * The ReplayMachine takes a Profiler from a previous execution and will re-run that execution
 * in the same order. The edges and vertices will called exactly the same order.
 * <p/>
 * A typical use case could be a test that encountered a failure, and now you
 * want to execute that test and see iff it can reproduce that failure.
 * <p/>
 *
 * @author Nils Olsson
 */
public final class ReplayMachine extends SimpleMachine {

    private final Profiler profiler;
    private final Iterator<Element> iterator;

    public ReplayMachine(Profiler profiler) {
        super();
        this.profiler = profiler;
        this.iterator = profiler.getPath().descendingIterator();
        getContexts().addAll(createContexts(profiler));
        setCurrentContext(chooseStartContext(getContexts()));
    }

    private Collection<Context> createContexts(Profiler profiler) {
        List<Context> contexts = new ArrayList<>();
        for (Context context : profiler.getContexts()) {
            try {
                Context newContext = context.getClass().newInstance();
                newContext.setModel(context.getModel());
                newContext.setNextElement(profiler.getPath().getFirst());
                contexts.add(newContext);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new MachineException(context, e);
            }
        }
        return contexts;
    }

    protected Context getNextStep(Context context) {
        Element element = iterator.next();
        setCurrentContext(profiler.getContext(element));
        getCurrentContext().setCurrentElement(element);
        return getCurrentContext();
    }

    @Override
    public boolean hasNextStep() {
        return iterator.hasNext();
    }

    private Context chooseStartContext(Collection<Context> contexts) {
        for (Context context : contexts) {
            if (null != context.getCurrentElement() || null != context.getNextElement()) {
                return context;
            }
        }
        throw new MachineException("No start context found");
    }
}
