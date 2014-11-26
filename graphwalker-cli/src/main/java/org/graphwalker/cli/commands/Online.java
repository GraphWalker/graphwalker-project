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
package org.graphwalker.cli.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Starts GraphWalker as a WebSocket server.")
public class Online {

    public static final String SERVICE_RESTFUL = "RESTFUL";
    public static final String SERVICE_WEBSOCKET = "WEBSOCKET";

    @Parameter(names = {"--verbose", "-o"}, required = false,
        description = "Will print more details")
    public boolean verbose = false;

    @Parameter(names = {"--unvisited", "-u"}, required = false,
        description = "Will also print the remaining unvisited elements in the model.")
    public boolean unvisited = false;

    @Parameter(names = {"--model", "-m"}, required = false, arity = 2,
        description = "The model, as a graphml file followed by generator with stop condition. " +
            "The format is GENERATOR(STOP_CONDITION) See HTML DOC")
    public List<String> model = new ArrayList<String>();

    @Parameter(names = {"--service", "-s"}, required = false, arity = 1,
        description = "Selects which kind of service to start. Either websocket [defualt], or restful")
    public String service = SERVICE_WEBSOCKET;

    @Parameter(names = {"--json", "-j"}, required = false,
        description = "Returns data formatted as json")
    public boolean json = false;

    @Parameter(names = {"--port", "-p"}, description = "Sets the port of the service")
    public int port = 8887;
}
