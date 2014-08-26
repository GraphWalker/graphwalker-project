package org.graphwalker.core.condition;

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

import org.graphwalker.core.algorithm.FloydWarshall;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.NamedElement;

import static org.graphwalker.core.model.Edge.RuntimeEdge;

/**
 * @author Nils Olsson
 */
public final class ReachedEdge implements NamedStopCondition {

    private final String name;

    public ReachedEdge(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isFulfilled(Context context) {
        return getFulfilment(context) >= FULFILLMENT_LEVEL;
    }

    @Override
    public double getFulfilment(Context context) {
        if (context.getCurrentElement() instanceof NamedElement) {
            NamedElement element = (NamedElement)context.getCurrentElement();
            if (element.hasName() && element.getName().equals(name)){
                return 1;
            } else {
                double maxFulfilment = 0;
                FloydWarshall floydWarshall = context.getAlgorithm(FloydWarshall.class);
                for (RuntimeEdge edge : context.getModel().findEdges(name)) {
                    int distance = floydWarshall.getShortestDistance(context.getCurrentElement(), edge);
                    int max = floydWarshall.getMaximumDistance(edge);
                    double fulfilment = 1 - (double) distance / max;
                    if (maxFulfilment < fulfilment) {
                        maxFulfilment = fulfilment;
                    }
                }
                return maxFulfilment;
            }
        }
        return 0;
    }
}
