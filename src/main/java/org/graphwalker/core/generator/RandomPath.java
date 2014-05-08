package org.graphwalker.core.generator;

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

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.EFSM;
import org.graphwalker.core.model.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.graphwalker.core.model.efsm.Edge.ImmutableEdge;
import static org.graphwalker.core.model.efsm.Vertex.ImmutableVertex;

/**
 * @author Nils Olsson
 */
public final class RandomPath implements PathGenerator {

    private final Random random = new Random(System.nanoTime());
    private final StopCondition stopCondition;

    public RandomPath(StopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    public StopCondition getStopCondition() {
        return stopCondition;
    }

    @Override
    public ExecutionContext getNextStep(ExecutionContext context) {
        Element element = context.getCurrentElement();
        if (null == element) {
            context.setCurrentElement(context.getStartElement());
        } else if (element instanceof ImmutableVertex) {
            ImmutableVertex vertex = (ImmutableVertex)element;
            // TODO: ugly lookup, fix it
            List<ImmutableEdge> edges = new ArrayList<>();
            for (ImmutableEdge edge: ((EFSM.ImmutableEFSM)context.getModel()).getEdges()) {
                if (edge.getSourceVertex() == vertex) {
                    edges.add(edge);
                }
            }
            if (0 == edges.size()) {
                throw new NoPathFoundException();
            }
            context.setCurrentElement(edges.get(random.nextInt(edges.size())));
        } else if (element instanceof ImmutableEdge) {
            ImmutableEdge edge = (ImmutableEdge)element;
            context.setCurrentElement(edge.getTargetVertex());
        }
        return context;
    }

    @Override
    public boolean hasNextStep(ExecutionContext context) {
        return getStopCondition().isFulfilled(context);
    }


}
