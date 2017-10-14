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

import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;

import static org.graphwalker.core.model.Edge.RuntimeEdge;

/**
 * <h1>FailFastStrategy</h1>
 * The FailFastStrategy handles the way GraphWalker handles failures during test execution.
 * </p>
 * The default way of handling a failure when executing a test, is to stop the test run and bail out.
 * That is what this class does.
 * </p>
 *
 * @author Nils Olsson
 */
public class FailFastStrategy implements ExceptionStrategy {

  @Override
  public void handle(Machine machine, MachineException exception) {
    Context context = exception.getContext();
    fail(context, context.getCurrentElement());
    if (context.getLastElement() instanceof RuntimeEdge) {
      fail(context, context.getLastElement());
    }
    context.setExecutionStatus(ExecutionStatus.FAILED);
    throw exception;
  }

  private void fail(Context context, Element element) {
    if (element.hasRequirements()) {
      for (Requirement requirement : element.getRequirements()) {
        context.setRequirementStatus(requirement, RequirementStatus.FAILED);
      }
    }
  }
}
