package org.graphwalker.cli.service;

/*
 * #%L
 * GraphWalker Command Line Interface
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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by krikar on 5/30/14.
 */
@Path("graphwalker")
public class Restful {
  private Machine machine;
  private ExecutionContext context;

  public Restful(Machine machine, ExecutionContext context) {
    this.machine = machine;
    this.context = context;
  }

  @GET
  @Produces("text/plain")
  @Path("hasNext")
  public String hasNext() {
    if (machine.hasNextStep()) {
      return "true";
    } else {
      return "false";
    }
  }

  @GET
  @Produces("text/plain")
  @Path("getNext")
  public String getNext() {
    if (machine.hasNextStep()) {
      try {
        machine.getNextStep();
      } catch (MachineException e) {
        ;
      } finally {
        if (context.getCurrentElement().hasName()) {
          return context.getCurrentElement().getName();
        } else {
          return "";
        }
      }
    } else {
      return "";
    }
  }
}
