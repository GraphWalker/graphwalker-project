package org.graphwalker.core.generator;

/*
* #%L
 * * GraphWalker Core
 * *
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
 * *
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
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Kristian Karl
 *         <p/>
 *         Generates a random path, but tries to reach unvisited elements first.
 *         This is quite effective for FSM, but for a EFSM this may not work since edges
 *         can be inaccessable.
 */
public final class QuickRandomPath extends PathGeneratorBase {

    private final Random random = new Random(System.nanoTime());
    private final StopCondition stopCondition;
    private final List<Element> unvisitedElements = new ArrayList<>();
    private Element target = null;

    public QuickRandomPath(StopCondition stopCondition) {
        this.stopCondition = stopCondition;
    }

    public StopCondition getStopCondition() {
        return stopCondition;
    }

    @Override
    public Context getNextStep() {
        Context context = getContext();
        if (unvisitedElements.isEmpty()) {
            unvisitedElements.addAll(context.getModel().getElements());
            unvisitedElements.remove(context.getCurrentElement());
        }
        if (null == target || target.equals(context.getCurrentElement())) {
            if (unvisitedElements.isEmpty()) {
                throw new NoPathFoundException();
            } else {
                target = unvisitedElements.get(random.nextInt(unvisitedElements.size()));
            }
        }
        Element nextElement = context.getAlgorithm(AStar.class).getNextElement(context.getCurrentElement(), target);
        unvisitedElements.remove(nextElement);
        return context.setCurrentElement(nextElement);
    }

    @Override
    public boolean hasNextStep() {
        return !getStopCondition().isFulfilled();
    }
}

