package org.graphwalker.cli.commands;

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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.graphwalker.io.factory.ContextFactoryScanner;

@Parameters(commandDescription = "Convert a graph in file format, to some other format. See http://graphwalker.org/docs/command_line_syntax")
public class Convert {

  @Parameter(names = {"--input", "-i"}, required = true, arity = 1,
    description = "This command requires an input file." +
                  "See http://graphwalker.org/docs/command_line_syntax")
  public String input = "";

  @Parameter(names = {"--format", "-f"}, required = false, arity = 1,
    description = "Which format to convert into. Valid key words are: JSON [default], GRAPHML, DOT or JAVA")
  public String format = ContextFactoryScanner.JSON;

  @Parameter(names = {"--blocked",
                      "-b"}, arity = 1, description = "This option enables or disables the BLOCKED feature. When \"-b true\" GraphWalker will filter out elements in models with the keyword BLOCKED. When \"-b false\" GraphWalker will not filter out any elements in models with the keyword BLOCKED.")
  public boolean blocked = true;
}
