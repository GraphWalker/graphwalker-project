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

import org.graphwalker.core.algorithm.AStar;
import org.graphwalker.core.algorithm.FloydWarshall;
import org.graphwalker.core.condition.NamedStopCondition;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Path;

import java.util.List;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public final class AStarPath implements PathGenerator {

    private final NamedStopCondition stopCondition;

    public AStarPath(NamedStopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    @Override
    public StopCondition getStopCondition() {
        return stopCondition;
    }

    @Override
    public ExecutionContext getNextStep(ExecutionContext context) {
        List<Element> elements = context.filter(context.getModel().getElements(context.getCurrentElement()));
        if (elements.isEmpty()) {
            throw new NoPathFoundException();
        }
        Element target = null;
        RuntimeModel model = context.getModel();
        int distance = Integer.MAX_VALUE;
        FloydWarshall floydWarshall = context.getAlgorithm(FloydWarshall.class);
        for (Element element: context.filter(model.findElements(stopCondition.getName()))) {
            int edgeDistance = floydWarshall.getShortestDistance(context.getCurrentElement(), element);
            if (edgeDistance < distance) {
                distance = edgeDistance;
                target = element;
            }
        }
        AStar astar = context.getAlgorithm(AStar.class);
        Path<Element> path = astar.getShortestPath(context.getCurrentElement(), target);
        path.pollFirst();
        return context.setCurrentElement(path.getFirst());
    }

    @Override
    public boolean hasNextStep(ExecutionContext context) {
        return !getStopCondition().isFulfilled(context);
    }
}
