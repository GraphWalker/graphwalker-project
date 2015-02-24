package org.graphwalker.core.machine;

import org.graphwalker.core.model.Element;
import org.graphwalker.core.statistics.Profiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
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
        for (Context context: profiler.getContexts()) {
            try {
                Context newContext = context.getClass().newInstance();
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
