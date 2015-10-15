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

import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.factory.ContextFactory;
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
    public <T extends Context> T create(Path path, T context) {
        return null;
    }

    @Override
    public <T extends Context> T write(T context, Path path) throws IOException {
        String newLine = System.getProperty("line.separator");
        StringBuilder str = new StringBuilder();
        int index = 0;

        str.append("import org.graphwalker.core.condition.EdgeCoverage;").append(newLine);
        str.append("import org.graphwalker.core.generator.RandomPath;").append(newLine);
        str.append("import org.graphwalker.core.machine.Context;").append(newLine);
        str.append("import org.graphwalker.core.machine.TestExecutionContext;").append(newLine);
        str.append("import org.graphwalker.core.model.*;").append(newLine);
        str.append("import org.junit.Assert;").append(newLine);
        str.append("import org.junit.Test;").append(newLine);
        str.append(newLine);

        str.append("public class " + FilenameUtils.getBaseName(path.toString()) + " {");
        str.append(newLine);

        str.append("  @Test").append(newLine);
        str.append("  public void createModelTest() {").append(newLine);

        for (Vertex.RuntimeVertex vertex: context.getModel().getVertices()) {
            String id = "";
            if ( vertex.getId()!=null && !vertex.getId().equals("")) {
                id = vertex.getId();
            } else {
                id = "n" + String.valueOf(index++);
            }

            str.append("    Vertex " + vertex.getName() + " = new Vertex().setName(\"" + vertex.getName() + "\").setId(\"" + id + "\");");
            str.append(newLine);
        }
        str.append(newLine);

        index = 0;
        str.append("    Model model = new Model();" + newLine);
        for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
            String id = "";
            if ( edge.getId()!=null && !edge.getId().equals("")) {
                id = edge.getId();
            } else {
                id = "n" + String.valueOf(index++);
            }

            str.append("    model.addEdge( new Edge()");
            if (edge.getSourceVertex() != null) {
                str.append(".setSourceVertex(" + edge.getSourceVertex().getName() + ")");
            }
            str.append(".setTargetVertex(" + edge.getTargetVertex().getName() + ")");
            str.append(".setName(\"" + edge.getName() + "\")).setId(\"" + id + "\");");
            str.append(newLine);
        }

        str.append(newLine);

        str.append("    Context context = new TestExecutionContext(model, new RandomPaths(new EdgeCoverage(100)));").append(newLine);
        str.append("    context.setNextElement(" + context.getModel().getEdges().get(0).getName() + ");").append(newLine);
        str.append(newLine);

        str.append("    Assert.assertEquals(context.getModel().getVertices().size(), " + context.getModel().getVertices().size() + ");").append(newLine);
        str.append("    Assert.assertEquals(context.getModel().getEdges().size(), " + context.getModel().getEdges().size() + ");").append(newLine);
        str.append(newLine);

        str.append("    while (context.getPathGenerator().hasNextStep()) {").append(newLine);
        str.append("      context.getPathGenerator().getNextStep();").append(newLine);
        str.append("      System.out.println(context.getCurrentElement().getName());").append(newLine);
        str.append("    }").append(newLine);

        str.append("  }" + newLine);
        str.append("}" + newLine);


        Files.newOutputStream(path).write(String.valueOf(str).getBytes());

        return context;
    }
}
