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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.script.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Nils Olsson
 */
public final class SimpleMachine implements Machine {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMachine.class);

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
        this.currentContext = contexts.get(0);
    }

    @Override
    public Context getNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        currentContext.getProfiler().stop();
        currentContext.getPathGenerator().getNextStep(currentContext);
        currentContext.getProfiler().start();
        execute(currentContext.getCurrentElement().getName());
        return currentContext;
    }

    @Override
    public boolean hasNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        if (!currentContext.getPathGenerator().hasNextStep(currentContext)) {
            return true;
        }
        // Find another context to execute!?
        // ...
        return false;
    }

    private void execute(String name) {
        logger.info("Execute {}", currentContext.getCurrentElement().getName());
        if (null != name && !"".equals(name)) {
            try {
                scriptEngine.setContext(currentContext);
                Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("impl", currentContext);
                scriptEngine.eval( "impl." + name + "()");
            } catch (ScriptException e) {
                throw new MachineException(e);
            }
        }
    }
}
