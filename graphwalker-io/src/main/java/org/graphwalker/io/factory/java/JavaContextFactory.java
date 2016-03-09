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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Kristian Karl
 */
public final class JavaContextFactory implements ContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(JavaContextFactory.class);
    private static final String FILE_TYPE = "java";
    private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.java"));

    private Vertex startVertex = null;
    private Edge startEdge = null;
    private Map<String, Vertex> elements = new HashMap<>();

    @Override
    public Set<String> getSupportedFileTypes() {
        return SUPPORTED_TYPE;
    }

    @Override
    public boolean accept(Path path) {
        return FilenameUtils.getExtension(path.toString()).equalsIgnoreCase(FILE_TYPE);
    }

    @Override
    public Context create(Path path) {
        return create(path, new JavaContext());
    }

    @Override
    public List<Context> createMultiple(Path path) {
        return null;
    }

    @Override
    public <T extends Context> T create(Path path, T context) {
        throw new ContextFactoryException("Creating a model from Java is not supported");
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
    public <T extends Context> T write(T context, Path path) throws IOException {
        String template = StringUtils.join(javaCodeTemplate.toArray(), "\n");
        template = template.replaceAll("\\{CLASS_NAME\\}", FilenameUtils.getBaseName(path.toString()));

        int index = 0;
        String add_vertices = "";
        for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
            String id = "";
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

        Files.newOutputStream(path).write(String.valueOf(template).getBytes());

        return context;
    }
}
