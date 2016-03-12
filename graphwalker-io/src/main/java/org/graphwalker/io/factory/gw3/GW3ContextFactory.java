package org.graphwalker.io.factory.gw3;

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
import org.graphwalker.io.factory.json.JsonContext;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.io.factory.json.JsonModel;
import org.graphwalker.io.factory.json.JsonMultimodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by krikar on 9/24/14.
 */
public final class GW3ContextFactory implements ContextFactory {

  private static final Logger logger = LoggerFactory.getLogger(GW3ContextFactory.class);
  private static final String FILE_TYPE = "gw3";
  private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.gw3"));

  @Override
  public <T extends Context> T create(Path path, T context) {
    new JsonContextFactory().create(path, context);
    return context;
  }

  /*
   * Reuse JsonContextFactory for single models
   */
  @Override
  public <T extends Context> T write(T context, Path path) throws IOException {
    Files.newOutputStream(path).write(String.valueOf(new JsonContextFactory().getJsonFromContext(context)).getBytes());
    return context;
  }

  /*
   * Reuse JsonContextFactory for single models
   */
  public <T extends Context> T create(String jsonString, T context) {
    new JsonContextFactory().create(jsonString, context);
    return context;
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
    return create(path, new JsonContext());
  }

  @Override
  public List<Context> createMultiple(Path path) {
    StringBuilder jsonGW3 = new StringBuilder();
    String line;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(path.toString())))) {
      while ((line = reader.readLine()) != null) {
        jsonGW3.append(line);
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("Could not read the file.");
    }
    logger.debug(jsonGW3.toString());

    return createMultiple(jsonGW3.toString());
  }

  public List<Context> createMultiple(String jsonGW3) {
    List<Context> contexts = new ArrayList<>();
    JsonMultimodel jsonMultimodel = new Gson().fromJson(jsonGW3, JsonMultimodel.class);
    for (JsonModel jsonModel : jsonMultimodel.getModels()) {
      GW3Context context = new GW3Context();
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

  /*
   * Reuse JsonContextFactory for single models
   */
  public Context create(String jsonString) {
    return create(jsonString, new JsonContext());
  }
}
