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
import org.graphwalker.core.machine.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Nils Olsson
 */
public final class CombinedPath extends PathGeneratorBase {

    private final List<PathGenerator> generators = new ArrayList<>();
    private int index = 0;

    public void addPathGenerator(PathGenerator pathGenerator) {
        generators.add(pathGenerator);
    }

    public List<PathGenerator> getPathGenerators() {
        return generators;
    }

    private PathGenerator getActivePathGenerator() {
        return generators.get(index);
    }

    @Override
    public StopCondition getStopCondition() {
        return getActivePathGenerator().getStopCondition();
    }

    @Override
    public Context getNextStep(Context context) {
        if (hasNextStep(context)) {
            return getActivePathGenerator().getNextStep(context);
        }
        throw new NoPathFoundException();
    }

    @Override
    public boolean hasNextStep(Context context) {
        for (; index < generators.size(); index++) {
            if (getActivePathGenerator().hasNextStep(context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StringBuilder toString(StringBuilder builder) {
        Iterator<PathGenerator> iterator = generators.iterator();
        while (iterator.hasNext()) {
            iterator.next().toString(builder);
            if (iterator.hasNext()) {
                builder.append(" AND ");
            }
        }
        return builder;
    }
}
