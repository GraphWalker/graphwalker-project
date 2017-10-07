package org.graphwalker.core.machine;

import static org.graphwalker.core.common.Objects.isNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.statistics.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>ReplayMachine</h1>
 * ReplayMachine can re-run an previous executed path..
 * </p>
 * The ReplayMachine takes a Profiler from a previous execution and will re-run that execution
 * in the same order. The edges and vertices will called exactly the same order.
 * </p>
 * If the last element of the execution is an edge, the ReplayMachine will execute the target
 * vertex as well.
 * </p>
 * A typical use case could be a test that encountered a failure, and now you
 * want to execute that test and see iff it can reproduce that failure.
 *
 * @author Nils Olsson
 */
public final class ReplayMachine extends SimpleMachine {
/*
  private static final Logger LOG = LoggerFactory.getLogger(ReplayMachine.class);

  private final Profiler profiler;
  private final Iterator<Element> iterator;

  public ReplayMachine(Profiler profiler) {
    super();
    this.profiler = profiler;
    this.iterator = profiler.getExecutionPath().descendingIterator();
    getContexts().addAll(createContexts(profiler));
    setCurrentContext(chooseStartContext(getContexts()));
  }

  private Collection<Context> createContexts(Profiler profiler) {
    List<Context> contexts = new ArrayList<>();
    for (Context context : profiler.getContexts()) {
      try {
        Context newContext = context.getClass().newInstance();
        newContext.setModel(context.getModel());
        newContext.setNextElement(profiler.getExecutionPath().getFirst());
        contexts.add(newContext);
      } catch (InstantiationException | IllegalAccessException e) {
        LOG.error(e.getMessage());
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
      if (isNotNull(context.getCurrentElement()) || isNotNull(context.getNextElement())) {
        return context;
      }
    }
    throw new MachineException("No start context found");
  }
*/
}
