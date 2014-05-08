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

import org.graphwalker.core.statistics.ProfileUnit;

import javax.script.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nils Olsson
 */
public final class SimpleMachine implements Machine {

    private final static String DEFAULT_SCRIPT_LANGUAGE = "JavaScript";
    private final List<ExecutionContext> contexts = new ArrayList<>();
    private final ScriptEngine scriptEngine;

    private ExecutionContext currentContext;

    public SimpleMachine(ExecutionContext context) {
        this(Arrays.asList(context));
    }

    public SimpleMachine(List<ExecutionContext> contexts) {
        this.contexts.addAll(contexts);
        this.scriptEngine = new ScriptEngineManager().getEngineByName(DEFAULT_SCRIPT_LANGUAGE);
    }

    @Override
    public Context getNextStep() {
        if (null != contexts.get(0).getCurrentElement()) { // End
            contexts.get(0).getProfile().put(contexts.get(0).getCurrentElement(), new ProfileUnit());
        }
        ExecutionContext context = contexts.get(0).getPathGenerator().getNextStep(contexts.get(0));
        if (null != context.getCurrentElement() && !context.getProfile().containsKey(context.getCurrentElement())) {  // Start
            contexts.get(0).getProfile().put(contexts.get(0).getCurrentElement(), new ProfileUnit());
        }
        String name = context.getCurrentElement().getName();
        if (null != name && !"".equals(name)) {
            try {
                scriptEngine.setContext(context);
                Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("impl", context);
                scriptEngine.eval( "impl." + name + "()");
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        return context;
    }

    @Override
    public boolean hasNextStep() {
        return !contexts.get(0).getPathGenerator().getStopCondition().isFulfilled(contexts.get(0));
    }


}
