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
// This file is part of the GraphWalker java package
// The MIT License
//
// Copyright (c) 2010 graphwalker.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.graphwalker.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CLITestRoot {

    StringBuffer stdOutput;
    StringBuffer errOutput;
    String outMsg;
    String errMsg;

    static Logger logger = Logger.getAnonymousLogger();
    private CLI commandLineInterface;

    private OutputStream redirectOut() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                stdOutput.append(Character.toString((char) b));
            }
        };
    }

    private OutputStream redirectErr() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                errOutput.append(Character.toString((char) b));
            }
        };
    }

    private InputStream redirectIn() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    logger.log(Level.ALL, "Unit testing was interrupted", e.getStackTrace());
                }
                return '0';
            }
        };
    }

    protected void runCommand(String args[]) {
        stdOutput = new StringBuffer();
        errOutput = new StringBuffer();

        PrintStream outStream = new PrintStream(redirectOut());
        PrintStream oldOutStream = System.out; // backup
        PrintStream errStream = new PrintStream(redirectErr());
        PrintStream oldErrStream = System.err; // backup

        System.setOut(outStream);
        System.setErr(errStream);

        commandLineInterface = new CLI();
        commandLineInterface.main(args);

        System.setOut(oldOutStream);
        System.setErr(oldErrStream);

        outMsg = stdOutput.toString();
        errMsg = errOutput.toString();
        logger.log(Level.FINER, "stdout: " + outMsg);
        logger.log(Level.FINER, "stderr: " + errMsg);
    }
}
