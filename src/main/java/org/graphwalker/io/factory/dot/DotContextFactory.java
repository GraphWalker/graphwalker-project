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
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.*;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.dot.DOTLexer;
import org.graphwalker.io.dot.DOTParser;
import org.graphwalker.io.factory.ContextFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        context.setModel(model);

        BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream(path.toString())));
        StringBuilder out = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        } catch (IOException e) {
            throw new DotContextFactoryException("Could not read the file.");
        }
        System.out.println(out.toString());   //Prints the string content read from input stream
        try {
            reader.close();
        } catch (IOException e) {
            throw new DotContextFactoryException("Could not read the file.");
        }

        DOTLexer lexer = new DOTLexer(new ANTLRInputStream(out.toString()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        DOTParser parser = new DOTParser(tokens);
        ParseTreeWalker walker = new ParseTreeWalker();

        AntlrDotListener listener = new AntlrDotListener();
        walker.walk(listener, parser.graph());

        for (Vertex vertex : listener.vertices.values()) {
            model.addVertex(vertex);
        }
        for (Edge edge : listener.edges.values()) {
            model.addEdge(edge);
        }

        return context;
    }
}
