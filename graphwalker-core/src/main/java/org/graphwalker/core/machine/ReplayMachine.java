package org.graphwalker.core.machine;

/*-
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import org.graphwalker.core.statistics.Execution;
import org.graphwalker.core.statistics.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class ReplayMachine extends SimpleMachine {

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
