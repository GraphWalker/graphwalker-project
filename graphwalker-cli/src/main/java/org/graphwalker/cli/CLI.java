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
import org.graphwalker.cli.commands.*;
import org.graphwalker.cli.util.LoggerUtil;
import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Requirement;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.dsl.antlr.DslException;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.java.test.TestExecutor;
import org.graphwalker.modelchecker.ContextsChecker;
import org.graphwalker.restful.Restful;
import org.graphwalker.restful.Util;
import org.graphwalker.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.graphwalker.core.common.Objects.isNotNullOrEmpty;
import static org.graphwalker.core.model.Model.RuntimeModel;

public class CLI {

  private static final Logger logger = LoggerFactory.getLogger(CLI.class);

  private Offline offline;
  private Online online;
  private Methods methods;
  private Requirements requirements;
  private Convert convert;
  private Source source;
  private Check check;

  enum Command {
    NONE,
    OFFLINE,
    ONLINE,
    METHODS,
    REQUIREMENTS,
    CONVERT,
    SOURCE,
    CHECK
  }

  private Command command = Command.NONE;

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
    Options options = new Options();
    JCommander jc = new JCommander(options);
    jc.setProgramName("java -jar graphwalker.jar");
    try {
      jc.parseWithoutValidation(args);
    } catch (Exception e) {
      // ignore
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

        convert = new Convert();
        jc.addCommand("convert", convert);

        source = new Source();
        jc.addCommand("source", source);

        check = new Check();
        jc.addCommand("check", check);

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

      convert = new Convert();
      jc.addCommand("convert", convert);

      source = new Source();
      jc.addCommand("source", source);

      check = new Check();
      jc.addCommand("check", check);

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
        } else if (jc.getParsedCommand().equalsIgnoreCase("convert")) {
          command = Command.CONVERT;
          RunCommandConvert();
        } else if (jc.getParsedCommand().equalsIgnoreCase("source")) {
          command = Command.SOURCE;
          RunCommandSource();
        } else if (jc.getParsedCommand().equalsIgnoreCase("check")) {
          command = Command.SOURCE;
          RunCommandCheck();
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

  private void RunCommandCheck() throws Exception {
    List<Context> contexts = getContextsWithPathGenerators(check.model.iterator());
    List<String> issues = ContextsChecker.hasIssues(contexts);
    if (!issues.isEmpty()) {
      for (String issue : issues) {
        System.out.println(issue);
      }
    } else {
      System.out.println("No issues found with the model(s).");
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
      ResourceConfig rc = new DefaultResourceConfig();
      try {
        rc.getSingletons().add(new Restful(getContextsWithPathGenerators(online.model.iterator()), online.verbose, online.unvisited));
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

  private void RunCommandConvert() throws Exception {
    String inputFileName = convert.input.get(0);
    String outputFileName = convert.input.get(1);

    ContextFactory inputFactory = ContextFactoryScanner.get(Paths.get(inputFileName));
    Context context;
    try {
      context = inputFactory.create(Paths.get(inputFileName));
    } catch (DslException e) {
      System.err.println("When parsing model: '" + inputFileName + "' " + e.getMessage() + System.lineSeparator());
      throw new Exception("Model syntax error");
    }

    ContextFactory outputFactory = ContextFactoryScanner.get(Paths.get(outputFileName));
    outputFactory.write(context, Paths.get(outputFileName));
  }

  private void RunCommandSource() throws Exception {
    String modelFileName = source.input.get(0);
    String templateFileName = source.input.get(1);

    // Read the model
    ContextFactory inputFactory = ContextFactoryScanner.get(Paths.get(modelFileName));
    Context context;
    try {
      if (inputFactory instanceof JsonContextFactory) {
        List<Context> contexts = inputFactory.createMultiple(Paths.get(modelFileName));
        if (isNotNullOrEmpty(contexts)) {
          context = contexts.get(0);
        } else {
          logger.error("No valid models found in: " + modelFileName);
          throw new RuntimeException("No valid models found in: " + modelFileName);
        }
      } else {
        context = inputFactory.create(Paths.get(modelFileName));
      }
    } catch (DslException e) {
      System.err.println("When parsing model: '" + modelFileName + "' " + e.getMessage() + System.lineSeparator());
      throw new Exception("Model syntax error");
    }
    SortedSet<String> names = new TreeSet<>();
    for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
      if (vertex.hasName()) {
        names.add(vertex.getName());
      }
    }
    for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
      if (edge.hasName()) {
        names.add(edge.getName());
      }
    }

    // Read the template
    StringBuilder templateStrBuilder = new StringBuilder();
    String line;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(templateFileName)))) {
      while ((line = reader.readLine()) != null) {
        templateStrBuilder.append(line).append("\n");
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new RuntimeException("Could not read the file: " + templateFileName);
    }
    String templateStr = templateStrBuilder.toString();

    // Apply the template and generate the source code to std out
    String header = "", body = "", footer = "";
    Pattern p = Pattern.compile("HEADER<\\{\\{([.\\s\\S]+)\\}\\}>HEADER([.\\s\\S]+)FOOTER<\\{\\{([.\\s\\S]+)\\}\\}>FOOTER");
    Matcher m = p.matcher(templateStr);
    if (m.find()) {
      header = m.group(1);
      body = m.group(2);
      footer = m.group(3);
    }

    System.out.println(header);
    for (String name : names) {
      System.out.println(body.replaceAll("\\{LABEL\\}", name));
    }
    System.out.println(footer);

  }

  private void RunCommandOffline() throws Exception {
    if (offline.model.size() > 0) {
      TestExecutor executor = new TestExecutor(getContextsWithPathGenerators(offline.model.iterator()));
      executor.getMachine().addObserver(new Observer() {
        @Override
        public void update(Machine machine, Element element, EventType type) {
          if (EventType.BEFORE_ELEMENT.equals(type)) {
            System.out.println(Util.getStepAsJSON(machine, offline.verbose, offline.unvisited).toString());
          }
        }
      });
      executor.execute();
    } else if (!offline.gw3.isEmpty()) {
      SimpleMachine machine = new SimpleMachine(new JsonContextFactory().createMultiple(Paths.get(offline.gw3)));
      while (machine.hasNextStep()) {
        machine.getNextStep();
        System.out.println(Util.getStepAsJSON(machine, offline.verbose, offline.unvisited).toString());
      }
    }
  }

  public List<Context> getContextsWithPathGenerators(Iterator itr) throws Exception {
    List<Context> executionContexts = new ArrayList<>();
    boolean triggerOnce = true;
    while (itr.hasNext()) {
      String modelFileName = (String) itr.next();
      ContextFactory factory = ContextFactoryScanner.get(Paths.get(modelFileName));
      Context context;
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

        if (elements == null) {
          throw new ParameterException("--start-element Did not find matching element in the model: " + modelFileName);
        } else if (elements.size() > 1) {
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
      Context context;
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
    if (model.getVertices().size() < 1) {
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
    version += "For a complete list of this package software dependencies, see http://graphwalker.org/archive/site/graphwalker-cli/dependencies.html" + System.getProperty("line.separator");

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

  public Online getOnline() {
    return online;
  }
}
