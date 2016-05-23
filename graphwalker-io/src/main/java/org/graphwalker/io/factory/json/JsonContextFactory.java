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

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.graphwalker.core.common.Objects.isNull;

/**
 * Created by krikar on 9/24/14.
 */
public final class JsonContextFactory implements ContextFactory {

  private static final Logger logger = LoggerFactory.getLogger(JsonContextFactory.class);
  private static final String FILE_TYPE = "json";
  private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.json"));

  @Override
  public <T extends Context> T create(Path path, T context) {
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

    create(out.toString(), context);
    return context;
  }

  @Override
  public <T extends Context> T write(T context, Path path) throws IOException {
    Files.newOutputStream(path).write(String.valueOf(getJsonFromContext(context)).getBytes());
    return context;
  }

  @Override
  public <T extends List<Context>> T write(T contexts, Path path) throws IOException {
    Files.newOutputStream(path).write(String.valueOf(getJsonFromContexts(contexts)).getBytes());
    return contexts;
  }

  public String getJsonFromModel(Model model) {
    return new Gson().toJson(model);
  }

  public String getJsonFromContext(Context context) {
    JsonMultimodel jsonMultimodel = new JsonMultimodel();
    JsonModel jsonModel = new JsonModel();
    jsonModel.setModel(context.getModel());

    if (context.getPathGenerator() != null) {
      jsonModel.setGenerator(context.getPathGenerator().toString());
    }
    if (context.getNextElement() != null && context.getNextElement().hasId()) {
      jsonModel.setStartElementId(context.getNextElement().getId());
    }
    jsonMultimodel.add(jsonModel);
    return new Gson().toJson(jsonMultimodel);
  }

  public String getJsonFromContexts(List<Context> contexts) {
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

  public <T extends Context> T create(String jsonString, T context) {
    return null;
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
  public Context create(Path path) {
    return null;
  }

  @Override
  public List<Context> createMultiple(Path path) {
    StringBuilder jsonStr = new StringBuilder();
    String line;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(path.toString())))) {
      while ((line = reader.readLine()) != null) {
        jsonStr.append(line);
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("Could not read the file.");
    }
    logger.debug(jsonStr.toString());

    return createMultiple(jsonStr.toString());
  }

  public List<Context> createMultiple(String jsonStr) {
    List<Context> contexts = new ArrayList<>();
    JsonMultimodel jsonMultimodel = new Gson().fromJson(jsonStr, JsonMultimodel.class);

    if (isNull(jsonMultimodel) || isNull(jsonMultimodel.getModels())) {
      return null;
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

  public Context create(String jsonString) {
    return create(jsonString, new JsonContext());
  }
}
