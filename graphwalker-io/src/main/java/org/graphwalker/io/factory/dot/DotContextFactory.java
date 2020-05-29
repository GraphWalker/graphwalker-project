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

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.dsl.antlr.dot.DotModelListener;
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
      try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
        for (Path file : directory) {
          contexts.add(read(file));
        }
      }
    } else {
      contexts.add(read(path));
    }
    return contexts;
  }

  private Context read(Path path) {
    Context context = new DotContext();
    // Model model = new Model();
    try (InputStream inputStream = ResourceUtils.getResourceAsStream(path.toString())) {
      DOTLexer lexer = new DOTLexer(CharStreams.fromStream(inputStream));
      CommonTokenStream tokens = new CommonTokenStream(lexer);

      DOTParser parser = new DOTParser(tokens);
      ParseTreeWalker walker = new ParseTreeWalker();
      DotModelListener modelListener = new DotModelListener();
      walker.walk(modelListener, parser.graph());
      final Model model = modelListener.getModel();
      model.getEdges().stream()
        .filter(edge -> edge.getSourceVertex() == null)
        .forEach(context::setNextElement);
      context.setModel(model.build());
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("Could not read the file.");
    }
    return context;
  }

  @Override
  public String getAsString(List<Context> contexts) {
    StringBuilder dotStr = new StringBuilder();

    for (Context context : contexts) {
      String newLine = System.lineSeparator();
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
    try (OutputStream outputStream = Files.newOutputStream(dotFile)) {
      outputStream.write(String.valueOf(getAsString(contexts)).getBytes());
    }
  }
}
