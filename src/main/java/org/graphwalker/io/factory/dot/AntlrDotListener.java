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

import org.antlr.v4.runtime.misc.NotNull;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.dot.DOTBaseListener;
import org.graphwalker.io.dot.DOTParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by krikar on 8/27/14.
 */
public class AntlrDotListener extends DOTBaseListener {

    public Map<String, Vertex> vertices = new HashMap<>();
    public Map<String, Edge> edges = new HashMap<>();

    private Edge edge = null;
    private Vertex vertex = null;
    private State state = State.NONE;

    enum State { NONE, LABEL }

    @Override
    public void enterNode_id(@NotNull DOTParser.Node_idContext ctx) {
        if (!vertices.containsKey(ctx.getText())) {
            edge = null;
            vertex = new Vertex();
            vertex.setId(ctx.getText());
            vertices.put(ctx.getText(), vertex);
            System.out.println("Adding node: " + ctx.getText());
        }
    }

    @Override
    public void enterId(@NotNull DOTParser.IdContext ctx) {
        System.out.println("enterId: "+ctx.getText());
        switch (state) {
            case NONE:
                if (ctx.getText().equalsIgnoreCase("label")) {
                    state = State.LABEL;
                } else if (null != edge) {

                }
                break;

            case LABEL:
                if (null!=edge) {
                    edge.setName(ctx.getText());
                } else if(null!=vertex) {
                    vertex.setName(ctx.getText());
                }
                state = State.NONE;
                break;
        }
    }

    @Override
    public void enterEdge_stmt(@NotNull DOTParser.Edge_stmtContext ctx) {
        if (!edges.containsKey(ctx.getText())) {
            vertex = null;
            edge = new Edge();
            edges.put(ctx.getText(), edge);
            System.out.println("Adding edge: "+ctx.getText());
        }
    }
}
