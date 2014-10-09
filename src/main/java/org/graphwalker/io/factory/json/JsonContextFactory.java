package org.graphwalker.io.factory.json;

/*
 * #%L
 * GraphWalker Input/Output
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.*;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by krikar on 9/24/14.
 */
public final class JsonContextFactory implements ContextFactory {
    private static final Logger logger = LoggerFactory.getLogger(JsonContextFactory.class);
    private static final String FILE_TYPE = "json";
    private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.json"));

    @Override
    public <T extends Context> T create(Path path, T context) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(path.toString())));
        StringBuilder out = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException e) {
            throw new ContextFactoryException("Could not read the file.");
        }
        logger.debug(out.toString());
        try {
            reader.close();
        } catch (IOException e) {
            throw new ContextFactoryException("Could not read the file.");
        }

        create(out.toString(), context);
        return context;
    }

    public <T extends Context> T create(String jsonString, T context) {
        Element startElement = null;
        Map<String, Vertex> elements = new HashMap<>();
        Model gwModel = new Model();
        JSONObject root = new JSONObject(jsonString);

        JSONArray vertices = root.getJSONArray("vertices");
        for (int vertexIndex = 0; vertexIndex < vertices.length(); ++vertexIndex) {
            JSONObject vertex = vertices.getJSONObject(vertexIndex);

            logger.debug("New vertex");
            Vertex v = new Vertex().setId(vertex.getString("id")).setName(vertex.getString("name"));
            logger.debug("  id: " + vertex.getString("id"));
            logger.debug("  name: " + vertex.getString("name"));

            gwModel.addVertex(v);
            elements.put(v.getId(), v);
            try {
                if (vertex.getBoolean("startElement")) {
                    logger.debug("  startElement: " + vertex.getBoolean("startElement"));
                    startElement = v.build();
                }
            } catch (JSONException ex) {
                ;
            }
        }

        JSONArray edges = root.getJSONArray("edges");
        for (int edgeIndex = 0; edgeIndex < edges.length(); ++edgeIndex) {
            JSONObject edge = edges.getJSONObject(edgeIndex);

            logger.debug("New edge");
            Edge e = new Edge().setId(edge.getString("id")).setName(edge.getString("name"));
            logger.debug("  id: " + edge.getString("id"));
            logger.debug("  name: " + edge.getString("name"));

            // The source vertex is not mandatory, since it implies the starting element
            try {
                e.setSourceVertex(elements.get(edge.getString("srcVertexId")));
                logger.debug("  srcVertexId: " + edge.getString("srcVertexId"));
            } catch (JSONException ex) {
                ;
            }

            // Actions (java script) is not mandatory
            try {
                JSONArray actions = edge.getJSONArray("actions");
                for (int actionIndex = 0; actionIndex < vertices.length(); ++actionIndex) {
                    JSONObject action = actions.getJSONObject(actionIndex);
                    e.addAction(new Action(action.getString("action")));
                    logger.debug("  action: " + action.getString("action"));
                }
            } catch (JSONException ex) {
                ;
            }

            e.setTargetVertex(elements.get(edge.getString("dstVertexId")));
            logger.debug("  dstVertexId: " + edge.getString("dstVertexId"));
            gwModel.addEdge(e);

            try {
                if (edge.getBoolean("startElement")) {
                    logger.debug("  startElement: " + edge.getBoolean("startElement"));
                    startElement = e.build();
                }
            } catch (JSONException ex) {
                ;
            }
        }

        gwModel.setName(root.getString("name"));
        logger.debug("Model name: " + root.getString("name"));
        context.setModel(gwModel.build());
        context.setPathGenerator(GeneratorFactory.parse(root.getString("generator")));
        logger.debug("Generator: " + root.getString("generator"));
        context.setNextElement(startElement);
        return context;
    }

    @Override
    public Set<String> getSupportedFileTypes() {
        return SUPPORTED_TYPE;
    }

    @Override
    public boolean accept(Path path) {
        return path.toFile().toString().endsWith(FILE_TYPE);
    }

    @Override
    public Context create(Path path) {
        return create(path, new JsonContext());
    }

    public Context create(String jsonString) {
        return create(jsonString, new JsonContext());
    }
}
