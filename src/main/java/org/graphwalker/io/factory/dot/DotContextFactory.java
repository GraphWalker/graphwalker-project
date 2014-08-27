package org.graphwalker.io.factory.dot;

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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.*;
import org.graphwalker.io.factory.ContextFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Kristian Karl
 */
public final class DotContextFactory implements ContextFactory {

    private static final String FILE_TYPE = "dot";
    private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.dot"));

    private Vertex startVertex = null;
    private Edge startEdge = null;
    private Map<String, Vertex> elements = new HashMap<>();

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
        return create(path, new DotContext());
    }

    @Override
    public Context create(Path path, Context context) {

        Model model = new Model();

        addVertices(model, context);
        addEdges(model, context);

        context.setModel(model);
        if (null != startEdge) {
            context.setNextElement(startEdge);
        } else {
            for (Vertex.RuntimeVertex vertex: context.getModel().getVertices()) {
                if (context.getModel().getOutEdges(vertex).isEmpty()) {
                    context.setNextElement(vertex);
                }
            }

        }
        return context;
    }

    private void addVertices(Model model, Context context) {

    }


    private void addEdges(Model model, Context context) {

    }
}
