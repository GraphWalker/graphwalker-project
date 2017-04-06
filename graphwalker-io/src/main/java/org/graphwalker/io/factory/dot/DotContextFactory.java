package org.graphwalker.io.factory.dot;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.dsl.antlr.dot.AntlrDotListener;
import org.graphwalker.dsl.dot.DOTLexer;
import org.graphwalker.dsl.dot.DOTParser;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kristian Karl
 */
public final class DotContextFactory implements ContextFactory {

  private static final Logger logger = LoggerFactory.getLogger(DotContextFactory.class);
  private static final String FILE_TYPE = "dot";
  private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.dot"));

  @Override
  public Set<String> getSupportedFileTypes() {
    return SUPPORTED_TYPE;
  }

  @Override
  public boolean accept(Path path) {
    return FilenameUtils.getExtension(path.toString()).equalsIgnoreCase(FILE_TYPE);
  }

  @Override
  public List<Context> create(Path path) throws IOException {
    List<Context> contexts = new ArrayList<>();

    if (ResourceUtils.isDirectory(path)) {
      DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
      for (Path file : directoryStream) {
        contexts.add(read(file));
      }
    } else {
      contexts.add(read(path));
    }
    return contexts;
  }

  private Context read(Path path) {
    Context context = new DotContext();
    Model model = new Model();

    StringBuilder out = new StringBuilder();
    String line;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(path.toString())))) {
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("Could not read the file.");
    }
    logger.debug(out.toString());

    DOTLexer lexer = new DOTLexer(new ANTLRInputStream(out.toString()));
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    DOTParser parser = new DOTParser(tokens);
    ParseTreeWalker walker = new ParseTreeWalker();

    AntlrDotListener listener = new AntlrDotListener();
    walker.walk(listener, parser.graph());

    Edge startEdge = null;
    for (Vertex vertex : listener.getVertices().values()) {
      if (!vertex.getName().equalsIgnoreCase("START")) {
        model.addVertex(vertex);
      }
    }
    for (Edge edge : listener.getEdges()) {
      if (edge.getSourceVertex().getName() != null &&
          edge.getSourceVertex().getName().equalsIgnoreCase("START")) {
        edge.setSourceVertex(null);
        startEdge = edge;
      }
      model.addEdge(edge);
    }

    model.setName(FilenameUtils.removeExtension(path.getFileName().toString()));
    context.setModel(model.build());
    if (null != startEdge) {
      context.setNextElement(startEdge);
    } else {
      for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
        if (context.getModel().getOutEdges(vertex).isEmpty()) {
          context.setNextElement(vertex);
        }
      }

    }
    return context;
  }

  @Override
  public String getAsString(List<Context> contexts) {
    StringBuilder dotStr = new StringBuilder();

    for (Context context : contexts) {
      String newLine = System.getProperty("line.separator");
      StringBuilder str = new StringBuilder();

      str.append("digraph " + FilenameUtils.getBaseName(context.getModel().getName())).append(" {").append(newLine);
      for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
        if (edge.getSourceVertex() != null) {
          str.append(edge.getSourceVertex().getName());
        } else {
          str.append("Start");
        }

        str.append(" -> ");
        if (edge.getTargetVertex() != null) {
          str.append(edge.getTargetVertex().getName());
        }
        str.append(" [label=\"");
        str.append(edge.getName());
        if (edge.hasGuard()) {
          str.append("\\n[").append(edge.getGuard().getScript()).append("]");
        }
        if (edge.hasActions()) {
          str.append("\\n/");
          for (Action action : edge.getActions()) {
            str.append(action.getScript());
          }
        }
        str.append("\"];").append(newLine);
      }
      str.append("}").append(newLine);
      dotStr.append(str);
    }
    return dotStr.toString();
  }

  @Override
  public void write(List<Context> contexts, Path path) throws IOException {
    File folder = path.toFile().getAbsoluteFile();
    Path dotFile = Paths.get(folder.toString(), contexts.get(0).getModel().getName() + ".dot");
    OutputStream outputStream = Files.newOutputStream(dotFile);
    try {
      outputStream.write(String.valueOf(getAsString(contexts)).getBytes());
    } finally {
      outputStream.close();
    }
  }
}
