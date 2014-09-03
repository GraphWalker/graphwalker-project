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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
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

    enum State {
        NONE,
        EDGE_LABEL,
        VERTEX_LABEL,
        EDGE_SRC_VERTEX,
        EDGE_DST_VERTEX
                }

    @Override
    public void enterNode_id(@NotNull DOTParser.Node_idContext ctx) {
        System.out.println("enterNode_id: "+ctx.getText());
        if (!vertices.containsKey(ctx.getText())) {
            edge = null;
            vertex = new Vertex();
            vertex.setId(ctx.getText());
            vertices.put(ctx.getText(), vertex);
            System.out.println("Adding node: " + ctx.getText());
        }
    }

    @Override
    public void enterAttr_stmt(@NotNull DOTParser.Attr_stmtContext ctx) {
        System.out.println("enterAttr_stmt: "+ctx.getText());
    }

    @Override
    public void enterPort(@NotNull DOTParser.PortContext ctx) {
        System.out.println("enterPort: "+ctx.getText());
    }

    @Override
    public void enterEdgeop(@NotNull DOTParser.EdgeopContext ctx) {
        System.out.println("enterEdgeop: "+ctx.getText());
    }

    @Override
    public void enterStmt_list(@NotNull DOTParser.Stmt_listContext ctx) {
        System.out.println("enterStmt_list: "+ctx.getText());
    }

    @Override
    public void enterStmt(@NotNull DOTParser.StmtContext ctx) {
        System.out.println("enterStmt: "+ctx.getText());
    }

    @Override
    public void enterSubgraph(@NotNull DOTParser.SubgraphContext ctx) {
        System.out.println("enterSubgraph: "+ctx.getText());
    }

    @Override
    public void enterGraph(@NotNull DOTParser.GraphContext ctx) {
        System.out.println("enterGraph: "+ctx.getText());
    }

    @Override
    public void enterA_list(@NotNull DOTParser.A_listContext ctx) {
        System.out.println("enterA_list: "+ctx.getText());
    }

    @Override
    public void enterAttr_list(@NotNull DOTParser.Attr_listContext ctx) {
        System.out.println("enterAttr_list: "+ctx.getText());
    }

    @Override
    public void enterNode_stmt(@NotNull DOTParser.Node_stmtContext ctx) {
        System.out.println("enterNode_stmt: "+ctx.getText());
    }

    @Override
    public void exitNode_stmt(@NotNull DOTParser.Node_stmtContext ctx) {
        System.out.println("exitNode_stmt: "+ctx.getText());
    }

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
        System.out.println("enterEveryRule: "+ctx.getText());
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
        System.out.println("exitEveryRule: "+ctx.getText());
    }

    @Override
    public void visitTerminal(@NotNull TerminalNode node) {
        System.out.println("visitTerminal: "+ node.getText());
    }

    @Override
    public void visitErrorNode(@NotNull ErrorNode node) {
        super.visitErrorNode(node);
    }

    @Override
    public void enterId(@NotNull DOTParser.IdContext ctx) {
        System.out.println("enterId: "+ctx.getText());
        switch (state) {
            case NONE:
                if (ctx.getText().equalsIgnoreCase("label")) {
                    state = State.EDGE_LABEL;
                } else if (null != edge) {

                }
                break;

            case EDGE_LABEL:
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
    public void enterEdgeRHS(@NotNull DOTParser.EdgeRHSContext ctx) {
        System.out.println("enterEdgeRHS: " + ctx.getText());
    }

    @Override
    public void enterEdge_stmt(@NotNull DOTParser.Edge_stmtContext ctx) {
        System.out.println("enterEdge_stmt: "+ctx.getText());
        if (!edges.containsKey(ctx.getText())) {
            vertex = null;
            edge = new Edge();
            edges.put(ctx.getText(), edge);
            System.out.println("Adding edge: "+ctx.getText());
        }
    }
}
