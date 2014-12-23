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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.graphwalker.cli.commands.Methods;
import org.graphwalker.cli.commands.Offline;
import org.graphwalker.cli.commands.Online;
import org.graphwalker.cli.commands.Requirements;
import org.graphwalker.cli.service.WebSocketServer;
import org.graphwalker.cli.service.Restful;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.cli.util.LoggerUtil;
import org.graphwalker.dsl.antlr.DslException;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static org.graphwalker.core.model.Model.RuntimeModel;

public class CLI {
    private static final Logger logger = LoggerFactory.getLogger(CLI.class);
    JCommander jc;
    Options options;
    Offline offline;
    Online online;
    Methods methods;
    Requirements requirements;
    enum Command {NONE, OFFLINE, ONLINE, METHODS, REQUIREMENTS};
    Command command = Command.NONE;

    public static void main(String[] args) {
        CLI cli = new CLI();
        try {
            cli.run(args);
        } catch (Exception e) {
            // We should have caught all exceptions up until here, but there
            // might have been problems with the command parser for instance...
            System.err.println(e + System.lineSeparator());
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
        try {
            jc.parseWithoutValidation(args);
        } catch (Exception e) {
            ;
        }

        try {
            setLogLevel(options);

            if (options.help) {
                options = new Options();
                jc = new JCommander(options);
                offline = new Offline();
                jc.addCommand("offline", offline);

                online = new Online();
                jc.addCommand("online", online);

                methods = new Methods();
                jc.addCommand("methods", methods);

                requirements = new Requirements();
                jc.addCommand("requirements", requirements);

                jc.parse(args);
                jc.usage();
                return;
            } else if (options.version) {
                System.out.println(printVersionInformation());
                return;
            }


            // Need to instantiate options again to avoid
            // ParameterException "Can only specify option --debug once."
            options = new Options();
            jc = new JCommander(options);
            offline = new Offline();
            jc.addCommand("offline", offline);

            online = new Online();
            jc.addCommand("online", online);

            methods = new Methods();
            jc.addCommand("methods", methods);

            requirements = new Requirements();
            jc.addCommand("requirements", requirements);

            jc.parse(args);

            // Parse for commands
            if (jc.getParsedCommand() != null) {
                if (jc.getParsedCommand().equalsIgnoreCase("offline")) {
                    command = Command.OFFLINE;
                    RunCommandOffline();
                } else if (jc.getParsedCommand().equalsIgnoreCase("online")) {
                    command = Command.ONLINE;
                    RunCommandOnline();
                } else if (jc.getParsedCommand().equalsIgnoreCase("methods")) {
                    command = Command.METHODS;
                    RunCommandMethods();
                } else if (jc.getParsedCommand().equalsIgnoreCase("requirements")) {
                    command = Command.REQUIREMENTS;
                    RunCommandRequirements();
                }
            }

            // No commands or options were found
            else {
                throw new MissingCommandException("Missing a command. Add '--help'");
            }

        } catch (MissingCommandException e) {
            System.err.println(e.getMessage() + System.lineSeparator());
        } catch (ParameterException e) {
            System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
            System.err.println(e.getMessage() + System.lineSeparator());
            if (jc.getParsedCommand() != null) {
                jc.usage(jc.getParsedCommand());
            }
        } catch (Exception e) {
            System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
            System.err.println(e.getMessage() + System.lineSeparator());
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
        } else {
            throw new ParameterException("Incorrect argument to --debug");
        }
    }

    private void RunCommandRequirements() throws Exception {
        SortedSet<String> reqs = new TreeSet<>();
        for (Context context : getContexts(requirements.model.iterator())) {
            for (Requirement req : context.getRequirements()) {
                reqs.add(req.getKey());
            }
        }
        for (String req : reqs) {
            System.out.println(req);
        }
    }

    private void RunCommandMethods() throws Exception {
        SortedSet<String> names = new TreeSet<>();
        for (Context context : getContexts(methods.model.iterator())) {
            for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
                if (null != vertex.getName()) {
                    names.add(vertex.getName());
                }
            }
            for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
                if (edge.getName() != null) {
                    names.add(edge.getName());
                }
            }
        }

        for (String name : names) {
            System.out.println(name);
        }
    }

    private void RunCommandOnline() throws Exception {
        if (online.service.equalsIgnoreCase(Online.SERVICE_WEBSOCKET)) {
            WebSocketServer GraphWalkerWebSocketServer = new WebSocketServer(online.port);
            try {
                GraphWalkerWebSocketServer.startService();
            } catch (Exception e) {
                logger.error("Something went wrong.", e);
            }
        } else if (online.service.equalsIgnoreCase(Online.SERVICE_RESTFUL)) {
            List<Context> executionContexts = getContextsWithPathGenerators((online.model.iterator()));
            ResourceConfig rc = new DefaultResourceConfig();
            try {
                rc.getSingletons().add(new Restful(new SimpleMachine(executionContexts), online));
            } catch (MachineException e) {
                System.err.println("Was the argument --model correctly?");
                throw e;
            }

            String url = "http://0.0.0.0:" + online.port;

            HttpServer server = GrizzlyServerFactory.createHttpServer(url, rc);
            System.out.println("Try http://localhost:"
                + online.port
                + "/graphwalker/hasNext or http://localhost:"
                + online.port
                + "/graphwalker/getNext");
            System.out.println("Press Control+C to end...");
            try {
                server.start();
                Thread.currentThread().join();
            } catch (Exception e) {
              logger.error("An error occurred when running command online: ", e);
            } finally {
                server.stop();
            }
        } else {
          throw new ParameterException("--service expected either WEBSOCKET or RESTFUL");
        }
    }

    private void RunCommandOffline() throws Exception {
        SimpleMachine machine = new SimpleMachine(getContextsWithPathGenerators(offline.model.iterator()));
        while (machine.hasNextStep()) {
            try {
                machine.getNextStep();
                if (offline.json) {
                    System.out.println(Util.getStepAsJSON(machine, offline.verbose, offline.unvisited).toString());
                } else {
                    System.out.println(Util.getStepAsString(machine, offline.verbose, offline.unvisited));
                }
            } catch (MachineException e) {
                throw e;
            }
        }
    }

    private List<Context> getContextsWithPathGenerators(Iterator itr) throws Exception {
        List<Context> executionContexts = new ArrayList<>();
        boolean triggerOnce = true;
        while (itr.hasNext()) {
            String modelFileName = (String) itr.next();
            ContextFactory factory = ContextFactoryScanner.get(Paths.get(modelFileName));
            Context context = null;
            try {
                context = factory.create(Paths.get(modelFileName));
            } catch (DslException e) {
                System.err.println("When parsing model: '" + modelFileName + "' " + e.getMessage() + System.lineSeparator());
                throw new Exception("Model syntax error");
            }
            context.setPathGenerator(GeneratorFactory.parse((String) itr.next()));

            if (triggerOnce &&
                (!offline.startElement.isEmpty() ||
                (!online.startElement.isEmpty()))) {
                triggerOnce = false;

                List<Element> elements = null;
                if (command == Command.OFFLINE) {
                    elements = context.getModel().findElements(offline.startElement);
                } else if (command == Command.ONLINE) {
                    elements = context.getModel().findElements(online.startElement);
                }

                if  (elements == null) {
                    throw new ParameterException("--start-element Did not find matching element in the model: " + modelFileName);
                } else if (elements.size() > 1 ) {
                    throw new ParameterException("--start-element There are more than one matching element in the model: " + modelFileName);
                }
                context.setNextElement(elements.get(0));
            }

            executionContexts.add(context);
        }
        return executionContexts;
    }

    private List<Context> getContexts(Iterator itr) throws Exception {
        List<Context> executionContexts = new ArrayList<>();
        while (itr.hasNext()) {
            String modelFileName = (String) itr.next();
            ContextFactory factory = ContextFactoryScanner.get(Paths.get(modelFileName));
            Context context = null;
            try {
                context = factory.create(Paths.get(modelFileName));
            } catch (DslException e) {
                System.err.println("When parsing model: '" + modelFileName + "' " + e.getMessage() + System.lineSeparator());
                throw new Exception("Model syntax error");
            }
            executionContexts.add(context);
        }
        return executionContexts;
    }

    private void verifyModel(RuntimeModel model) {
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
