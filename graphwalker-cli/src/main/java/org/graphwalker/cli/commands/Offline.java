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
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Generate a test sequence offline. The sequence is printed to the standard output. See http://graphwalker.org/docs/command_line_syntax")
public class Offline {

  @Parameter(names = {"--verbose", "-o"}, required = false,
    description = "Will print more details to stdout")
  public boolean verbose = false;

  @Parameter(names = {"--unvisited", "-u"}, required = false,
    description = "Will also print the remaining unvisited elements in the model.")
  public boolean unvisited = false;

  @Parameter(names = {"--model", "-m"}, required = false, arity = 2,
    description = "The model, as a graphml file followed by generator with stop condition. " +
                  "The format is GENERATOR(STOP_CONDITION) See http://graphwalker.org/docs/path_generators_and_stop_conditions")
  public List<String> model = new ArrayList<>();

  @Parameter(names = {"--gw3", "-g"}, required = false, arity = 1,
    description = "The model, as a single gw3 file")
  public String gw3 = "";

  @Parameter(names = {"--start-element", "-e"}, required = false,
    description = "Sets the starting element in the [first] model.")
  public String startElement = "";

  @Parameter(names = {"--blocked",
                      "-b"}, arity = 1, description = "This option enables or disables the BLOCKED feature. When \"-b true\" GraphWalker will filter out elements in models with the keyword BLOCKED. When \"-b false\" GraphWalker will not filter out any elements in models with the keyword BLOCKED.")
  public boolean blocked = true;

  @Parameter(names = {"--seed", "-d"}, required = false,
    description = "Seed the random generator using the provided number.")
  public long seed = 0;
}
