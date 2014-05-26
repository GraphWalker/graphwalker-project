package org.graphwalker.core.machine;

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

import org.graphwalker.core.generator.NoPathFoundException;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public final class SimpleMachine implements Machine {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMachine.class);

    private final List<ExecutionContext> contexts = new ArrayList<>();

    private ExecutionContext currentContext;

    public SimpleMachine(ExecutionContext context) {
        this(Arrays.asList(context));
    }

    public SimpleMachine(List<ExecutionContext> contexts) {
        this.contexts.addAll(contexts);
        this.currentContext = contexts.get(0);
    }

    @Override
    public Context getNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        currentContext.getProfiler().stop();
        walk(currentContext);
        currentContext.getProfiler().start();
        execute(currentContext.getCurrentElement());
        return currentContext;
    }

    private void walk(ExecutionContext context) {
        if (null == context.getCurrentElement()) {
            if (null == context.getNextElement()) {
                throw new NoPathFoundException("No Start element defined");
            }
            context.setCurrentElement(context.getNextElement());
        } else {
            context.getPathGenerator().getNextStep(context);
        }
    }

    @Override
    public boolean hasNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        if (currentContext.getPathGenerator().hasNextStep(currentContext)) {
            return true;
        }
        // Find another context to execute!?
        // ...
        return false;
    }

    private void execute(Element element) {
        if (element instanceof RuntimeVertex) {
            execute((RuntimeVertex)element);
        } else if (element instanceof RuntimeEdge) {
            execute((RuntimeEdge)element);
        }
    }

    private void execute(RuntimeEdge edge) {
        logger.info("Execute {}", currentContext.getCurrentElement());
        execute(edge.getActions());
        if (edge.hasName()) {
            currentContext.execute(edge.getName());
        }
    }

    private void execute(List<Action> actions) {
        for (Action action: actions) {
            logger.info("Execute {}", action);
            currentContext.execute(action);
        }
    }

    private void execute(RuntimeVertex vertex) {
        logger.info("Execute {}", currentContext.getCurrentElement());
        if (vertex.hasName()) {
            currentContext.execute(vertex.getName());
        }
    }

}
