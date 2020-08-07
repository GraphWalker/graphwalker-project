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
import org.graphwalker.cli.util.UnsupportedFileFormat;
import org.graphwalker.core.event.EventType;
import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.machine.Context;
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
import org.graphwalker.io.factory.dot.DotContextFactory;
import org.graphwalker.io.factory.java.JavaContextFactory;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.io.factory.yed.YEdContextFactory;
import org.graphwalker.java.test.TestExecutor;
import org.graphwalker.modelchecker.ContextsChecker;
import org.graphwalker.restful.Restful;
import org.graphwalker.restful.Util;
import org.graphwalker.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.graphwalker.core.common.Objects.isNullOrEmpty;
import static org.graphwalker.io.common.Util.printVersionInformation;

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
          runCommandOffline();
        } else if (jc.getParsedCommand().equalsIgnoreCase("online")) {
          command = Command.ONLINE;
          runCommandOnline();
        } else if (jc.getParsedCommand().equalsIgnoreCase("methods")) {
          command = Command.METHODS;
          runCommandMethods();
        } else if (jc.getParsedCommand().equalsIgnoreCase("requirements")) {
          command = Command.REQUIREMENTS;
          runCommandRequirements();
        } else if (jc.getParsedCommand().equalsIgnoreCase("convert")) {
          command = Command.CONVERT;
          runCommandConvert();
        } else if (jc.getParsedCommand().equalsIgnoreCase("source")) {
          command = Command.SOURCE;
          runCommandSource();
        } else if (jc.getParsedCommand().equalsIgnoreCase("check")) {
          command = Command.SOURCE;
          runCommandCheck();
        }
      }

      // No commands or options were found
      else {
        throw new MissingCommandException("Missing a command. Add '--help'");
      }

    } catch (UnsupportedFileFormat | MissingCommandException e) {
      System.err.println(e.getMessage() + System.lineSeparator());
    } catch (ParameterException e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage() + System.lineSeparator());
      jc.usage();
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

  private void runCommandCheck() throws Exception, UnsupportedFileFormat {
    List<Context> contexts = getContextsWithPathGenerators(check.model.iterator());
    if (check.blocked) {
      org.graphwalker.io.common.Util.filterBlockedElements(contexts);
    }

    List<String> issues = ContextsChecker.hasIssues(contexts);
    if (!issues.isEmpty()) {
      for (String issue : issues) {
        System.out.println(issue);
      }
    } else {
      System.out.println("No issues found with the model(s).");
    }
  }

  private void runCommandRequirements() throws Exception, UnsupportedFileFormat {
    SortedSet<String> reqs = new TreeSet<>();
    List<Context> contexts = getContexts(requirements.model.iterator());
    if (requirements.blocked) {
      org.graphwalker.io.common.Util.filterBlockedElements(contexts);
    }

    for (Context context : contexts) {
      for (Requirement req : context.getRequirements()) {
        reqs.add(req.getKey());
      }
    }
    for (String req : reqs) {
      System.out.println(req);
    }
  }

  private void runCommandMethods() throws Exception, UnsupportedFileFormat {
    SortedSet<String> names = new TreeSet<>();
    List<Context> contexts = getContexts(methods.model.iterator());
    if (methods.blocked) {
      org.graphwalker.io.common.Util.filterBlockedElements(contexts);
    }

    for (Context context : contexts) {
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

  private void runCommandOnline() throws Exception, UnsupportedFileFormat {
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
        List<Context> contexts = getContextsWithPathGenerators(online.model.iterator());

        rc.getSingletons().add(new Restful(contexts, online.verbose, online.unvisited, online.blocked));
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
      } catch (InterruptedException e) {
        // Typically the user pressed Ctrl+C
        ;
      } catch (Exception e) {
        logger.error("An error occurred when running command online: ", e);
      } finally {
        server.stop();
      }
    } else {
      throw new ParameterException("--service expected either WEBSOCKET or RESTFUL");
    }
  }

  private void runCommandConvert() throws Exception, UnsupportedFileFormat {
    String inputFileName = convert.input;

    ContextFactory inputFactory = getContextFactory(inputFileName);
    List<Context> contexts;
    try {
      contexts = inputFactory.create(Paths.get(inputFileName));
    } catch (DslException e) {
      System.err.println("When parsing model: '" + inputFileName + "' " + e.getMessage() + System.lineSeparator());
      throw new Exception("Model syntax error");
    }

    if (convert.blocked) {
      org.graphwalker.io.common.Util.filterBlockedElements(contexts);
    }

    ContextFactory outputFactory = getContextFactory("foo." + convert.format);
    System.out.println(outputFactory.getAsString(contexts));
  }

  private void runCommandSource() throws Exception, UnsupportedFileFormat {
    String modelFileName = source.input.get(0);
    String templateFileName = source.input.get(1);

    // Read the model
    ContextFactory inputFactory = getContextFactory(modelFileName);
    List<Context> contexts;
    try {
      contexts = inputFactory.create(Paths.get(modelFileName));
      if (isNullOrEmpty(contexts)) {
        logger.error("No valid models found in: " + modelFileName);
        throw new RuntimeException("No valid models found in: " + modelFileName);
      }
    } catch (DslException e) {
      System.err.println("When parsing model: '" + modelFileName + "' " + e.getMessage() + System.lineSeparator());
      throw new Exception("Model syntax error");
    }

    if (source.blocked) {
      org.graphwalker.io.common.Util.filterBlockedElements(contexts);
    }

    for (Context context : contexts) {
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
  }

  private void runCommandOffline() throws Exception, UnsupportedFileFormat {
    if (offline.model.size() > 0) {
      List<Context> contexts = getContextsWithPathGenerators(offline.model.iterator());
      if (offline.blocked) {
        org.graphwalker.io.common.Util.filterBlockedElements(contexts);
      }

      TestExecutor executor = new TestExecutor(contexts);
      executor.getMachine().addObserver((machine, element, type) -> {
        if (EventType.BEFORE_ELEMENT.equals(type)) {
          System.out.println(Util.getStepAsJSON(machine, offline.verbose, offline.unvisited).toString());
        }
      });

      if (offline.seed != 0) {
        SingletonRandomGenerator.setSeed(offline.seed);
      }

      executor.execute();
    } else if (!offline.gw3.isEmpty()) {
      //TODO Fix gw3. Should not be there
      List<Context> contexts = new JsonContextFactory().create(Paths.get(offline.gw3));

      if (offline.seed != 0) {
        SingletonRandomGenerator.setSeed(offline.seed);
      }

      if (offline.blocked) {
        org.graphwalker.io.common.Util.filterBlockedElements(contexts);
      }

      SimpleMachine machine = new SimpleMachine(contexts);
      while (machine.hasNextStep()) {
        machine.getNextStep();
        System.out.println(Util.getStepAsJSON(machine, offline.verbose, offline.unvisited).toString());
      }
    }
  }

  public List<Context> getContextsWithPathGenerators(Iterator itr) throws Exception, UnsupportedFileFormat {
    List<Context> executionContexts = new ArrayList<>();
    boolean triggerOnce = true;
    while (itr.hasNext()) {
      String modelFileName = (String) itr.next();
      ContextFactory factory = getContextFactory(modelFileName);
      List<Context> contexts;
      try {
        contexts = factory.create(Paths.get(modelFileName));
      } catch (DslException e) {
        System.err.println("When parsing model: '" + modelFileName + "' " + e.getMessage() + System.lineSeparator());
        throw new Exception("Model syntax error");
      }
      // TODO fix all occurences of get(0) is not safe
      contexts.get(0).setPathGenerator(GeneratorFactory.parse((String) itr.next()));

      if (triggerOnce &&
        (!offline.startElement.isEmpty() || !online.startElement.isEmpty())) {
        triggerOnce = false;

        List<Element> elements = null;
        if (command == Command.OFFLINE) {
          elements = contexts.get(0).getModel().findElements(offline.startElement);
        } else if (command == Command.ONLINE) {
          elements = contexts.get(0).getModel().findElements(online.startElement);
        }

        if (elements == null) {
          throw new ParameterException("--start-element Did not find matching element in the model: " + modelFileName);
        } else if (elements.size() > 1) {
          throw new ParameterException("--start-element There are more than one matching element in the model: " + modelFileName);
        }
        contexts.get(0).setNextElement(elements.get(0));
      }

      executionContexts.addAll(contexts);
    }
    return executionContexts;
  }

  private ContextFactory getContextFactory(String modelFileName) throws UnsupportedFileFormat {
    ContextFactory factory;
    if (new YEdContextFactory().accept(Paths.get(modelFileName))) {
      factory = new YEdContextFactory();
    } else if (new JsonContextFactory().accept(Paths.get(modelFileName))) {
      factory = new JsonContextFactory();
    } else if (new DotContextFactory().accept(Paths.get(modelFileName))) {
      factory = new DotContextFactory();
    } else if (new JavaContextFactory().accept(Paths.get(modelFileName))) {
      factory = new JavaContextFactory();
    } else {
      throw new UnsupportedFileFormat(modelFileName);
    }
    return factory;
  }

  private List<Context> getContexts(Iterator itr) throws Exception, UnsupportedFileFormat {
    List<Context> executionContexts = new ArrayList<>();
    while (itr.hasNext()) {
      String modelFileName = (String) itr.next();
      ContextFactory factory = getContextFactory(modelFileName);
      List<Context> contexts;
      try {
        contexts = factory.create(Paths.get(modelFileName));
      } catch (DslException e) {
        System.err.println("When parsing model: '" + modelFileName + "' " + e.getMessage() + System.lineSeparator());
        throw new Exception("Model syntax error");
      }
      executionContexts.addAll(contexts);
    }
    return executionContexts;
  }
}
