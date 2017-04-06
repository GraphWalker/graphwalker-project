package org.graphwalker.cli;

/*
 * #%L
 * GraphWalker Command Line Interface
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CLITestRoot {

  private static Logger logger = LoggerFactory.getLogger(CLITestRoot.class);

  protected Result runCommand(String args[]) {
    RedirectStream stdOutput = new RedirectStream();
    RedirectStream errOutput = new RedirectStream();

    PrintStream outStream = new PrintStream(stdOutput);
    PrintStream oldOutStream = System.out; // backup
    PrintStream errStream = new PrintStream(errOutput);
    PrintStream oldErrStream = System.err; // backup

    System.setOut(outStream);
    System.setErr(errStream);

    try {
      CLI.main(args);
    } finally {
      System.setOut(oldOutStream);
      System.setErr(oldErrStream);

      String outMsg = stdOutput.toString();
      String errMsg = errOutput.toString();
      logger.info("stdout: " + outMsg);
      logger.info("stderr: " + errMsg);
      return new Result(outMsg, errMsg);
    }
  }

  public class RedirectStream extends OutputStream {

    private final StringBuffer buffer = new StringBuffer();

    @Override
    public void write(int b) throws IOException {
      buffer.append(Character.toString((char) b));
    }

    @Override
    public String toString() {
      return buffer.toString();
    }
  }

  public class Result {

    private final String output;
    private final String error;

    public Result(String output, String error) {
      this.output = output;
      this.error = error;
    }

    public String getOutput() {
      return output;
    }

    public String getError() {
      return error;
    }
  }
}
