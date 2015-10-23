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

import org.apache.commons.io.FilenameUtils;
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
import java.nio.file.Files;
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

    @Override
    public <T extends Context> T write(T context, Path path) throws IOException {
        Files.newOutputStream(path).write(String.valueOf(getJsonFromContext(context)).getBytes());
        return context;
    }

    public String getJsonFromContext(Context context) {
        JSONObject jsonGraph = new JSONObject();
        if (context.getModel().getName() != null) {
            jsonGraph.put("name", context.getModel().getName());
        }
        if (context.getPathGenerator() != null) {
            jsonGraph.put("generator", context.getPathGenerator().toString());
        }

        JSONArray jsonVertices = new JSONArray();
        for (Vertex.RuntimeVertex vertex : context.getModel().getVertices()) {
            JSONObject jsonVertex = new JSONObject();
            jsonVertex.put("id", vertex.getId());
            jsonVertex.put("name", vertex.getName());
            if (context.getNextElement() == vertex) {
                jsonVertex.put("startElement", "true");
            }
            jsonVertices.put(jsonVertex);
        }
        jsonGraph.put("vertices", jsonVertices);

        JSONArray jsonEdges = new JSONArray();
        for (Edge.RuntimeEdge edge : context.getModel().getEdges()) {
            JSONObject jsonEdge = new JSONObject();
            jsonEdge.put("id", edge.getId());
            jsonEdge.put("name", edge.getName());
            if (edge.getSourceVertex() != null) {
                jsonEdge.put("srcVertexId", edge.getSourceVertex().getId());
            }
            if (edge.getTargetVertex() != null) {
                jsonEdge.put("dstVertexId", edge.getTargetVertex().getId());
            }

            if (edge.getGuard() != null) {
                jsonEdge.put("guard", edge.getGuard().getScript());
            }

            if (edge.hasActions()) {
                JSONArray jsonActions = new JSONArray();
                for (Action action : edge.getActions()) {
                    JSONObject jsonAction = new JSONObject();
                    jsonAction.put("action", action.getScript());
                    jsonActions.put(jsonAction);
                }
                jsonEdge.put("actions", jsonActions);
            }
            jsonEdges.put(jsonEdge);
        }
        jsonGraph.put("edges", jsonEdges);
        return jsonGraph.toString(2);
    }

    public <T extends Context> T create(String jsonString, T context) {
        Element startElement = null;
        Map<String, Vertex> elements = new HashMap<>();
        Model model = new Model();
        JSONObject jsonGraph = new JSONObject(jsonString);

        JSONArray jsonVertices = jsonGraph.getJSONArray("vertices");
        for (int vertexIndex = 0; vertexIndex < jsonVertices.length(); ++vertexIndex) {
            JSONObject jsonVertex = jsonVertices.getJSONObject(vertexIndex);

            logger.debug("New vertex");
            Vertex vertex = new Vertex().setId(jsonVertex.getString("id")).setName(jsonVertex.getString("name"));
            logger.debug("  id: " + jsonVertex.getString("id"));
            logger.debug("  name: " + jsonVertex.getString("name"));

            model.addVertex(vertex);
            elements.put(vertex.getId(), vertex);
            try {
                if (jsonVertex.getBoolean("startElement")) {
                    logger.debug("  startElement: " + jsonVertex.getBoolean("startElement"));
                    startElement = vertex.build();
                }
            } catch (JSONException ex) {
                ;
            }
        }

        JSONArray jsonEdges = jsonGraph.getJSONArray("edges");
        for (int edgeIndex = 0; edgeIndex < jsonEdges.length(); ++edgeIndex) {
            JSONObject jsonEdge = jsonEdges.getJSONObject(edgeIndex);

            logger.debug("New edge");
            Edge edge = new Edge().setId(jsonEdge.getString("id")).setName(jsonEdge.getString("name"));
            logger.debug("  id: " + jsonEdge.getString("id"));
            logger.debug("  name: " + jsonEdge.getString("name"));

            // The source vertex is not mandatory, since it implies the starting element
            try {
                edge.setSourceVertex(elements.get(jsonEdge.getString("srcVertexId")));
                logger.debug("  srcVertexId: " + jsonEdge.getString("srcVertexId"));
            } catch (JSONException ex) {
                logger.debug("No srcVertexId");
            }

            // Actions (java script) is not mandatory
            try {
                JSONArray jsonActions = jsonEdge.getJSONArray("actions");
                for (int actionIndex = 0; actionIndex < jsonVertices.length(); ++actionIndex) {
                    JSONObject jsonAction = jsonActions.getJSONObject(actionIndex);
                    edge.addAction(new Action(jsonAction.getString("action")));
                    logger.debug("  action: " + jsonAction.getString("action"));
                }
            } catch (JSONException ex) {
                logger.debug("No actions");
            }

            edge.setTargetVertex(elements.get(jsonEdge.getString("dstVertexId")));
            logger.debug("  dstVertexId: " + jsonEdge.getString("dstVertexId"));
            model.addEdge(edge);

            try {
                if (jsonEdge.getBoolean("startElement")) {
                    logger.debug("  startElement: " + jsonEdge.getBoolean("startElement"));
                    startElement = edge.build();
                }
            } catch (JSONException ex) {
                ;
            }

            try {
                edge.setGuard(new Guard(jsonEdge.getString("guard")));
                logger.debug("  guard: " + edge.getGuard().getScript());
            } catch (JSONException ex) {
                logger.debug("No guard");
            }
        }

        try {
            model.setName(jsonGraph.getString("name"));
            logger.debug("Model name: " + jsonGraph.getString("name"));
        } catch (JSONException ex) {
            logger.debug("No model name");
        }

        context.setModel(model.build());

        try {
            context.setPathGenerator(GeneratorFactory.parse(jsonGraph.getString("generator")));
            logger.debug("Generator: " + jsonGraph.getString("generator"));
        } catch (JSONException ex) {
            ;
        }

        if (startElement != null) {
            context.setNextElement(startElement);
        }

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

    public Context create(String jsonString) {
        return create(jsonString, new JsonContext());
    }
}
