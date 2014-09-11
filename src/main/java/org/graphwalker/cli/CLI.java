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
package org.graphwalker.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.graphwalker.cli.antlr.GeneratorFactory;
import org.graphwalker.cli.antlr.GeneratorFactoryException;
import org.graphwalker.cli.commands.Methods;
import org.graphwalker.cli.commands.Offline;
import org.graphwalker.cli.commands.Online;
import org.graphwalker.cli.commands.Requirements;
import org.graphwalker.cli.service.Restful;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.graphwalker.core.utils.LoggerUtil;
import org.graphwalker.io.factory.yed.YEdContextFactory;
import org.graphwalker.io.factory.yed.YEdContextFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

public class CLI {
    private static final Logger logger = LoggerFactory.getLogger(CLI.class);
    JCommander jc;
    Options options;
    Offline offline;
    Online online;
    Methods methods;
    Requirements requirements;

    public static void main(String[] args) {
        CLI cli = new CLI();
        try {
            cli.run(args);
        } catch (Exception e) {
            // We should have caught all exceptions up until here, but there
            // might have been problems with the command parser for instance...
            System.err.println(e);
            logger.error("An error occurred when running command: " + StringUtils.join(args, " "), e);
        }
    }

    /**
     * Parses the command line.
     *
     * @param args
     */
    private void run(String[] args) {
        options = new Options();

        jc = new JCommander(options);
        jc.setProgramName("java -jar graphwalker.jar");

        offline = new Offline();
        jc.addCommand("offline", offline);

        online = new Online();
        jc.addCommand("online", online);

        methods = new Methods();
        jc.addCommand("methods", methods);

        requirements = new Requirements();
        jc.addCommand("requirements", requirements);

        try {
            jc.parse(args);
            setLogLevel(options);


            // Parse for commands
            if (jc.getParsedCommand() != null) {
                if (jc.getParsedCommand().equalsIgnoreCase("offline")) {
                    RunCommandOffline();
                } else if (jc.getParsedCommand().equalsIgnoreCase("online")) {
                    RunCommandOnline();
                } else if (jc.getParsedCommand().equalsIgnoreCase("methods")) {
                    RunCommandMethods();
                } else if (jc.getParsedCommand().equalsIgnoreCase("requirements")) {
                    RunCommandRequirements();
                }
            }

            // Parse for arguments
            else if (options.version) {
                System.out.println(printVersionInformation());
            }

            // No commands or options were found
            else {
                jc.usage();
            }

        } catch (MissingCommandException e) {
            System.err.println("I did not see a valid command.");
            System.err.println("");
            jc.usage();
        } catch (ParameterException e) {
            System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
            System.err.println(e.getMessage());
            jc.usage(jc.getParsedCommand());
        } catch (YEdContextFactoryException e) {
            System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
            System.err.println(e.getMessage());
        } catch (GeneratorFactoryException e) {
            System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
            System.err.println(e.getMessage());
            logger.error("An error occurred when running command: " + StringUtils.join(args, " "), e);
        }
    }

    private void setLogLevel(Options options) {
        // OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
        if (options.debug.equalsIgnoreCase("OFF")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.OFF);
        } else if (options.debug.equalsIgnoreCase("ERROR")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.ERROR);
        } else if (options.debug.equalsIgnoreCase("WARN")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.WARN);
        } else if (options.debug.equalsIgnoreCase("INFO")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.INFO);
        } else if (options.debug.equalsIgnoreCase("DEBUG")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.DEBUG);
        } else if (options.debug.equalsIgnoreCase("TRACE")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.TRACE);
        } else if (options.debug.equalsIgnoreCase("ALL")) {
            LoggerUtil.setLogLevel(LoggerUtil.Level.ALL);
        }
    }

    private void RunCommandRequirements() throws Exception {
        YEdContextFactory factory = new YEdContextFactory();
        Context context = null;

        String modelFileName = requirements.model;
        try {
            context = factory.create(Paths.get(modelFileName));
        } catch (YEdContextFactoryException e) {
            throw new YEdContextFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?");
        }

        SortedSet<Requirement> reqs = new TreeSet<>();
        for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
            for (Requirement req : vertex.getRequirements()) {
                reqs.add(req);
            }
        }

        for (Requirement req : reqs) {
            System.out.println(req.getKey());
        }
    }

    private void RunCommandMethods() throws Exception {
        YEdContextFactory factory = new YEdContextFactory();
        Context context = null;

        String modelFileName = methods.model;
        try {
            context = factory.create(Paths.get(modelFileName));
        } catch (YEdContextFactoryException e) {
            throw new YEdContextFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?");
        }

        SortedSet<String> names = new TreeSet<>();
        for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
            if (null != vertex.getName()) {
                names.add(vertex.getName());
            }
        }
        for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
            names.add(edge.getName());
        }

        for (String name : names) {
            System.out.println(name);
        }
    }

    private void RunCommandOnline() throws Exception {
        YEdContextFactory factory = new YEdContextFactory();
        Context context = null;

        ArrayList<Context> executionContexts = new ArrayList<>();
        Iterator itr = online.model.iterator();
        while (itr.hasNext()) {
            String modelFileName = (String) itr.next();
            try {
                context = factory.create(Paths.get(modelFileName));
            } catch (YEdContextFactoryException e) {
                throw new YEdContextFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?");
            }
            context.setPathGenerator(GeneratorFactory.parse((String) itr.next()));
            executionContexts.add(context);
        }

        if (online.restful) {
            // Todo: We cannot iterate of multiple models and
            // instantiating the HttpServer every time.
            // The creation of HttpServer needs to be done outside the while-loop
            ResourceConfig rc = new DefaultResourceConfig();
            rc.getSingletons().add(new Restful(new SimpleMachine((org.graphwalker.core.machine.Context) executionContexts)));
            HttpServer server = GrizzlyServerFactory.createHttpServer("http://0.0.0.0:9999", rc);
            System.out.println("Press Control+C to end...");
            try {
                server.start();
                Thread.currentThread().join();
            } catch (Exception e) {
                logger.error("An error occurred when running command online: ", e);
            } finally {
                server.stop();
            }
        }
    }

    private void RunCommandOffline() throws Exception {
        YEdContextFactory factory = new YEdContextFactory();
        Context context = null;

        ArrayList<Context> executionContexts = new ArrayList<>();
        Iterator itr = offline.model.iterator();
        while (itr.hasNext()) {
            String modelFileName = (String) itr.next();
            context = factory.create(Paths.get(modelFileName));
            context.setPathGenerator(GeneratorFactory.parse((String) itr.next()));
            executionContexts.add(context);
            verifyModel(context.getModel());
        }

        SimpleMachine machine = new SimpleMachine(executionContexts);
        while (machine.hasNextStep()) {
            try {
                machine.getNextStep();
                if (offline.verbose) {
                    System.out.print(FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName()) + " : ");
                }
                if (machine.getCurrentContext().getCurrentElement().hasName()) {
                    System.out.print(machine.getCurrentContext().getCurrentElement().getName());
                    if (offline.verbose) {
                        System.out.print("(" + machine.getCurrentContext().getCurrentElement().getId() + ")");
                        System.out.print(":" + machine.getCurrentContext().getKeys());
                    }
                }

                if (offline.unvisited) {
                    System.out.print(" | " + machine.getCurrentContext().getProfiler().getUnvisitedElements().size() +
                        "(" + machine.getCurrentContext().getModel().getElements().size() + ") : ");

                    for (Element e : machine.getCurrentContext().getProfiler().getUnvisitedElements()) {
                        System.out.print(e.getName());
                        if (offline.verbose) {
                            System.out.print("(" + e.getId() + ")");
                        }
                        System.out.print(" ");
                    }
                }

                System.out.println();
            } catch (MachineException e) {
                throw e;
            }
        }
    }

    private void verifyModel(Model.RuntimeModel model) {
        // Verify that the model has more than 1 vertex
        if (model.getAllVertices().size() < 1) {
            throw new RuntimeException("Model has less than 1 vertices. [Excluding the Start vertex]");
        }
        // Verify that the model has more than 0 edges
        if (model.getEdges().size() < 1) {
            throw new RuntimeException("Model has less than 1 edge.");
        }
    }

    private String printVersionInformation() {
        String version = "org.graphwalker version: " + getVersionString() + System.getProperty("line.separator");
        version += System.getProperty("line.separator");

        version += "org.graphwalker is open source software licensed under MIT license" + System.getProperty("line.separator");
        version += "The software (and it's source) can be downloaded from http://graphwalker.org" + System.getProperty("line.separator");
        version += "For a complete list of this package software dependencies, see TO BE DEFINED" + System.getProperty("line.separator");

        return version;
    }

    private String getVersionString() {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("/version.properties");
        if (null != inputStream) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                logger.error("An error occurred when trying to get the version string", e);
                return "unknown";
            }
        }
        return properties.getProperty("graphwalker.version");
    }

    private String generateListOfValidGenerators() {
        return "";
    }

    private String generateListOfValidStopConditions() {
        return "";
    }

    private boolean helpNeeded(String module, boolean condition, String message) {
        if (condition) {
            System.out.println(message);
            System.out.println("Type 'java -jar graphwalker.jar help " + module + "' for help.");
        }
        return condition;
    }
}
