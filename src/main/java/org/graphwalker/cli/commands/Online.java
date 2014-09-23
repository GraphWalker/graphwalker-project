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
package org.graphwalker.cli.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Generate a test sequence offline. The sequence is fetched using different techniques, like RestAPI")
public class Online {

    @Parameter(names = {"--verbose", "-o"}, required = false, arity = 1,
        description = "Will print more details")
    public boolean verbose = false;

    @Parameter(names = {"--unvisited", "-u"}, required = false, arity = 1,
        description = "Will also print the remaining unvisited elements in the model.")
    public boolean unvisited = false;

    @Parameter(names = {"--model", "-m"}, required = true, arity = 2,
        description = "The model, as a graphml file followed by generator with stop condition. " +
            "The format is GENERATOR(STOP_CONDITION) See HTML DOC")
    public List<String> model = new ArrayList<String>();

    @Parameter(names = {"--restful", "-r"}, required = false, arity = 1,
        description = "Starts as a Restful API service.")
    public boolean restful = true;

    @Parameter(names = {"--json", "-j"}, required = false, arity = 1,
        description = "Returns data formatted as json")
    public boolean json = true;
}
