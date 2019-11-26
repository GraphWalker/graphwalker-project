package org.graphwalker.io.factory.json;

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

import static org.graphwalker.core.common.Objects.isNull;

import com.google.gson.Gson;
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
import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by krikar on 9/24/14.
 */
public final class JsonContextFactory implements ContextFactory {

  private static final Logger logger = LoggerFactory.getLogger(JsonContextFactory.class);
  private static final String FILE_TYPE = "json";
  private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.json"));

  @Override
  public void write(List<Context> contexts, Path path) throws IOException {
    File folder = path.toFile().getAbsoluteFile();
    Path jsonFile = Paths.get(folder.toString(), contexts.get(0).getModel().getName() + ".json");
    try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
      outputStream.write(String.valueOf(getAsString(contexts)).getBytes());
    }
  }

  public String getJsonFromModel(Model model) {
    return new Gson().toJson(model);
  }

  public String getAsString(List<Context> contexts) {
    JsonMultimodel jsonMultimodel = new JsonMultimodel();
    for (Context context : contexts) {
      JsonModel jsonModel = new JsonModel();
      jsonModel.setModel(context.getModel());

      if (context.getPathGenerator() != null) {
        jsonModel.setGenerator(context.getPathGenerator().toString());
      }
      if (context.getNextElement() != null && context.getNextElement().hasId()) {
        jsonModel.setStartElementId(context.getNextElement().getId());
      }
      jsonMultimodel.add(jsonModel);
    }
    return new Gson().toJson(jsonMultimodel);
  }

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
        contexts.addAll(read(file));
      }
    } else {
      contexts.addAll(read(path));
    }
    return contexts;
  }

  private List<Context> read(Path file) {
    StringBuilder jsonStr = new StringBuilder();
    String line;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(file.toString())))) {
      while ((line = reader.readLine()) != null) {
        jsonStr.append(line);
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("Could not read the file.");
    }
    logger.debug(jsonStr.toString());

    return create(jsonStr.toString());
  }

  public List<Context> create(String jsonStr) {
    List<Context> contexts = new ArrayList<>();
    JsonMultimodel jsonMultimodel = new Gson().fromJson(jsonStr, JsonMultimodel.class);

    if (isNull(jsonMultimodel) || isNull(jsonMultimodel.getModels())) {
      throw new ContextFactoryException("The json file is not a valid GraphWalker model(s) file");
    }

    // Seed the [global] singleton random generator, if any seed was set in the json file
    if (jsonMultimodel.getSeed() != null) {
      SingletonRandomGenerator.setSeed(jsonMultimodel.getSeed());
    }

    for (JsonModel jsonModel : jsonMultimodel.getModels()) {
      JsonContext context = new JsonContext();
      Model model = jsonModel.getModel();

      context.setModel(model.build());
      if (jsonModel.getGenerator() != null) {
        context.setPathGenerator(GeneratorFactory.parse(jsonModel.getGenerator()));
      }
      for (Element element : context.getModel().getElements()) {
        if (element.getId().equals(jsonModel.getStartElementId())) {
          context.setNextElement(element);
          break;
        }
      }
      contexts.add(context);
    }

    return contexts;
  }
}
