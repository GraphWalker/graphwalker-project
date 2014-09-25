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

/**
 * @author Nils Olsson
 */
public abstract class PathGeneratorBase implements PathGenerator {

    private Context context;
    private StopCondition stopCondition;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        if (null != getStopCondition()) {
            getStopCondition().setContext(getContext());
        }
    }

    public StopCondition getStopCondition() {
        return stopCondition;
    }

    public void setStopCondition(StopCondition stopCondition) {
        this.stopCondition = stopCondition;
        if (null != getContext()) {
            this.stopCondition.setContext(getContext());
        }
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder builder) {
        builder.append(getClass().getSimpleName()).append("(");
        getStopCondition().toString(builder).append(")");
        return builder;
    }
}
