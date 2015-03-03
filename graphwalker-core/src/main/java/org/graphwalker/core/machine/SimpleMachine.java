package org.graphwalker.core.machine;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
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

import org.graphwalker.core.event.EventType;
import org.graphwalker.core.generator.NoPathFoundException;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * <h1>SimpleMachine</h1>
 * SimpleMachine is working implementation of the MachineBase.
 * </p>
 * The SimpleMachine is the Finite State Machine which executes one or more contexts. Running the machine
 * is done by calling {@link SimpleMachine#getNextStep()}. Before calling
 * getNextStep check {@link SimpleMachine#hasNextStep()} if the machine has more steps to be executed or not.
 * </p>
 *
 * @author Nils Olsson
 */
public class SimpleMachine extends MachineBase {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMachine.class);

    private Element lastElement;

    public SimpleMachine() {
    }

    public SimpleMachine(Context... contexts) {
        this(Arrays.asList(contexts));
    }

    public SimpleMachine(Collection<Context> contexts) {
        this.getContexts().addAll(contexts);
        executeInitActions(contexts);
        setCurrentContext(chooseStartContext(contexts));
    }

    private void executeInitActions(Collection<Context> contexts) {
        for (Context context : contexts) {
            setCurrentContext(context);
            getCurrentContext().setProfiler(getProfiler());
            if (null == context.getModel()) {
                throw new MachineException("A context must be associated with a model");
            }
            execute(context.getModel().getActions());
        }
    }

    private Context chooseStartContext(Collection<Context> contexts) {
        for (Context context : contexts) {
            if (null != context.getCurrentElement() || null != context.getNextElement()) {
                return context;
            }
        }
        throw new MachineException("No start context found");
    }

    @Override
    public Context getNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        walk(getCurrentContext());
        notifyObservers(getCurrentContext().getCurrentElement(), EventType.BEFORE_ELEMENT);
        getProfiler().start(getCurrentContext());
        execute(getCurrentContext().getCurrentElement());
        getProfiler().stop(getCurrentContext());
        if (getCurrentContext().getLastElement() instanceof RuntimeEdge) {
            updateRequirements(getCurrentContext(), getCurrentContext().getLastElement());
        }
        if (getCurrentContext().getCurrentElement() instanceof RuntimeVertex) {
            updateRequirements(getCurrentContext(), getCurrentContext().getCurrentElement());
        }
        notifyObservers(getCurrentContext().getCurrentElement(), EventType.AFTER_ELEMENT);
        return getCurrentContext();
    }

    private void updateRequirements(Context context, Element element) {
        if (element.hasRequirements()) {
            for (Requirement requirement : element.getRequirements()) {
                context.setRequirementStatus(requirement, RequirementStatus.PASSED);
            }
        }
    }

    protected Context getNextStep(Context context) {
        if (null != context.getNextElement()) {
            context.setCurrentElement(context.getNextElement());
        } else {
            context.getPathGenerator().getNextStep();
        }
        return context;
    }

    private void walk(Context context) {
        try {
            if (null == context.getCurrentElement()) {
                context = takeFirstStep(context);
            } else {
                context = takeNextStep(context);
            }
            if (ExecutionStatus.NOT_EXECUTED.equals(context.getExecutionStatus())) {
                context.setExecutionStatus(ExecutionStatus.EXECUTING);
            }
        } catch (Throwable t) {
            getExceptionStrategy().handle(this, new MachineException(context, t));
        }
    }

    private Context takeFirstStep(Context context) {
        if (null != context.getNextElement()) {
            context = getNextStep(context);
        } else {
            context = switchContext(findStartContext());
        }
        return context;
    }

    private Context findStartContext() {
        Context startContext = null;
        for (Context context : getContexts()) {
            if (hasNextStep(context) && (null != context.getCurrentElement() || null != context.getNextElement())) {
                startContext = context;
                break;
            }
        }
        if (null == startContext) {
            throw new NoPathFoundException("No start element defined");
        }
        return startContext;
    }

    private Context switchContext(Context context) {
        hasNextStep(getCurrentContext());
        lastElement = getCurrentContext().getCurrentElement();
        getCurrentContext().setCurrentElement(null);
        setCurrentContext(context);
        return getCurrentContext();
    }

    private Context takeNextStep(Context context) {
        if (isVertex(context.getCurrentElement())) {
            RuntimeVertex vertex = (RuntimeVertex) context.getCurrentElement();
            if (vertex.hasSharedState() && hasPossibleSharedStates(vertex)) {
                context = chooseSharedContext(context, vertex);
            }
        }
        return getNextStep(context);
    }

    private Context chooseSharedContext(Context context, RuntimeVertex vertex) {
        List<SharedStateTuple> candidates = getPossibleSharedStates(vertex.getSharedState());
        Random random = new Random(System.nanoTime());
        SharedStateTuple candidate = candidates.get(random.nextInt(candidates.size()));
        if (!candidate.getVertex().equals(context.getCurrentElement())) {
            candidate.context.setNextElement(candidate.getVertex());
            context = switchContext(candidate.context);
        } else {
            lastElement = null;
        }
        return context;
    }

    private boolean isVertex(Element element) {
        return element instanceof RuntimeVertex;
    }

    private boolean hasPossibleSharedStates(RuntimeVertex vertex) {
        return null != vertex.getSharedState() && 0 < getPossibleSharedStates(vertex.getSharedState()).size();
    }

    private List<SharedStateTuple> getPossibleSharedStates(String sharedState) {
        List<SharedStateTuple> sharedStates = new ArrayList<>();
        for (Context context : getContexts()) {
            if (getCurrentContext().equals(context) && hasOutEdges(context)) {
                sharedStates.add(new SharedStateTuple(getCurrentContext(), (RuntimeVertex) getCurrentContext().getCurrentElement()));
            } else if (!getCurrentContext().equals(context) && context.getModel().hasSharedState(sharedState)) {
                for (RuntimeVertex vertex : context.getModel().getSharedStates(sharedState)) {
                    if ((!vertex.equals(lastElement) || getCurrentContext().getModel().getOutEdges((RuntimeVertex) getCurrentContext().getCurrentElement()).isEmpty()) && (vertex.hasName() || !context.getModel().getOutEdges(vertex).isEmpty())) {
                        sharedStates.add(new SharedStateTuple(context, vertex));
                    }
                }
            }
        }
        return sharedStates;
    }

    private boolean hasOutEdges(Context context) {
        return null != context.getCurrentElement()
                && context.getCurrentElement() instanceof RuntimeVertex
                && !context.getModel().getOutEdges((RuntimeVertex) context.getCurrentElement()).isEmpty();
    }

    @Override
    public boolean hasNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        for (Context context : getContexts()) {
            if (hasNextStep(context)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNextStep(Context context) {
        ExecutionStatus status = context.getExecutionStatus();
        if (ExecutionStatus.COMPLETED.equals(status) || ExecutionStatus.FAILED.equals(status)) {
            return false;
        }
        if (null == context.getPathGenerator()) {
            throw new MachineException("No path generator is defined");
        }
        boolean hasMoreSteps = context.getPathGenerator().hasNextStep();
        if (!hasMoreSteps) {
            context.setExecutionStatus(ExecutionStatus.COMPLETED);
            updateRequirements(context, context.getModel());
        }
        return hasMoreSteps;
    }

    private void execute(Element element) {
        try {
            if (element instanceof RuntimeVertex) {
                execute((RuntimeVertex) element);
            } else if (element instanceof RuntimeEdge) {
                execute((RuntimeEdge) element);
            }
        } catch (MachineException e) {
            getExceptionStrategy().handle(this, e);
        }
    }

    private void execute(RuntimeEdge edge) {
        execute(edge.getActions());
        if (edge.hasName()) {
            getCurrentContext().execute(edge.getName());
        }
    }

    private void execute(List<Action> actions) {
        for (Action action : actions) {
            getCurrentContext().execute(action);
        }
    }

    private void execute(RuntimeVertex vertex) {
        if (vertex.hasName()) {
            getCurrentContext().execute(vertex.getName());
        }
    }

    private static class SharedStateTuple {

        private final Context context;
        private final RuntimeVertex vertex;

        private SharedStateTuple(Context context, RuntimeVertex vertex) {
            this.context = context;
            this.vertex = vertex;
        }

        public Context getContext() {
            return context;
        }

        public RuntimeVertex getVertex() {
            return vertex;
        }
    }

}
