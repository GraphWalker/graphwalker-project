package org.graphwalker.io.factory.java;

/*
 * #%L
 * GraphWalker Input/Output
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kristian Karl
 */
public final class JavaContextFactory implements ContextFactory {

  private static final Logger logger = LoggerFactory.getLogger(JavaContextFactory.class);
  private static final String FILE_TYPE = "java";
  private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.java"));

  @Override
  public Set<String> getSupportedFileTypes() {
    return SUPPORTED_TYPE;
  }

  @Override
  public boolean accept(Path path) {
    return FilenameUtils.getExtension(path.toString()).equalsIgnoreCase(FILE_TYPE);
  }

  @Override
  public List<Context> create(Path path) {
    throw new ContextFactoryException("Creating a context from a java file is not supported.");
  }

  public static final List<String>
      javaCodeTemplate = ImmutableList.of(
      "import org.graphwalker.core.condition.*;",
      "import org.graphwalker.core.generator.*;",
      "import org.graphwalker.core.machine.*;",
      "import org.graphwalker.core.model.*;",
      "",
      "public class {CLASS_NAME} {",
      "",
      "  public final class ModelTestContext extends ExecutionContext {",
      "  }",
      "",
      "  public static void main(String... aArgs) {",
      "    {CLASS_NAME} modeltest = new {CLASS_NAME}();",
      "    modeltest.run();",
      "  }",
      "",
      "  private void run() {",
      "    {ADD_VERTICES}",
      "",
      "    Model model = new Model();",
      "    {ADD_EDGES}",
      "",
      "    Context context = new ModelTestContext();",
      "    context.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));",
      "    context.setNextElement(context.getModel().findElements(\"{START_ELEMENT_NAME}\").get(0));",
      "",
      "    Machine machine = new SimpleMachine(context);",
      "    while (machine.hasNextStep()) {",
      "      machine.getNextStep();",
      "      System.out.println(context.getCurrentElement().getName());",
      "    }",
      "  }",
      "}");

  @Override
  public String getAsString(List<Context> contexts) {
    String javaStr = "";
    for (Context context : contexts) {
      String template = StringUtils.join(javaCodeTemplate.toArray(), "\n");
      template = template.replaceAll("\\{CLASS_NAME\\}", context.getModel().getName());

      int index = 0;
      String add_vertices = "";
      for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
        String id;
        if (vertex.getId() != null && !vertex.getId().equals("")) {
          id = vertex.getId();
        } else {
          id = "n" + index++;
        }

        add_vertices += "Vertex " + vertex.getName() + " = new Vertex().setName(\"" + vertex.getName() + "\").setId(\"" + id + "\");";
        add_vertices += "\n";
      }
      template = template.replace("{ADD_VERTICES}", add_vertices);

      index = 0;
      String add_edges = "";
      for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
        String id;
        if (edge.getId() != null && !edge.getId().equals("")) {
          id = edge.getId();
        } else {
          id = "n" + index++;
        }

        add_edges += "model.addEdge( new Edge()";
        if (edge.getSourceVertex() != null) {
          add_edges += ".setSourceVertex(" + edge.getSourceVertex().getName() + ")";
        }
        add_edges += ".setTargetVertex(" + edge.getTargetVertex().getName() + ")";
        add_edges += ".setName(\"" + edge.getName() + "\").setId(\"" + id + "\")";

        if (edge.hasGuard()) {
          add_edges += ".setGuard(new Guard(\"" + edge.getGuard().getScript() + "\"))";
        }
        if (edge.hasActions()) {
          for (Action action : edge.getActions()) {
            add_edges += ".addAction(new Action(\"" + action.getScript() + "\"))";
          }
        }
        add_edges += ");\n";
      }
      template = template.replace("{ADD_EDGES}", add_edges);
      template = template.replace("{START_ELEMENT_NAME}", context.getNextElement().getName());

      javaStr += template;
    }
    return javaStr;
  }

  @Override
  public void write(List<Context> contexts, Path path) throws IOException {
    Path javaFile = Paths.get(path.toString(), contexts.get(0).getModel().getName() + ".java");
    try (OutputStream outputStream = Files.newOutputStream(javaFile)) {
      outputStream.write(getAsString(contexts).getBytes());
    }
  }
}
