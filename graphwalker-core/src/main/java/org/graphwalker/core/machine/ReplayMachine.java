package org.graphwalker.core.machine;

import static org.graphwalker.core.common.Objects.isNotNull;

import java.util.*;
import java.util.stream.Collectors;

import org.graphwalker.core.model.Element;
import org.graphwalker.core.statistics.Execution;
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

  private static final Logger LOG = LoggerFactory.getLogger(ReplayMachine.class);

  private final Profiler profiler;
  private final Iterator<Execution> iterator;
  private final Map<Context, Context> contexts = new HashMap<>();

  public ReplayMachine(Profiler profiler) {
    super();
    this.profiler = profiler;
    this.iterator = profiler.getExecutionPath().iterator();
    createContexts(profiler);
    Execution execution = profiler.getExecutionPath().get(0);
    Context context = contexts.get(execution.getContext());
    context.setNextElement(execution.getElement());
    setCurrentContext(context);
  }

  @Override
  public List<Context> getContexts() {
    return contexts.values().stream().collect(Collectors.toList());
  }

  private void createContexts(Profiler profiler) {
    for (Context context : profiler.getContexts()) {
      try {
        Context newContext = context.getClass().newInstance();
        newContext.setModel(context.getModel());
        contexts.put(context, newContext);
      } catch (InstantiationException | IllegalAccessException e) {
        LOG.error(e.getMessage());
        throw new MachineException(context, e);
      }
    }
  }

  protected Context getNextStep(Context context) {
    Execution execution = iterator.next();
    setCurrentContext(contexts.get(execution.getContext()));
    getCurrentContext().setCurrentElement(execution.getElement());
    return getCurrentContext();
  }

  @Override
  public boolean hasNextStep() {
    return iterator.hasNext();
  }
}
