package org.graphwalker.dsl.antlr.dot;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.dsl.dot.DOTBaseListener;
import org.graphwalker.dsl.dot.DOTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by krikar on 8/27/14.
 */
public class AntlrDotListener extends DOTBaseListener {

  private static final Logger logger = LoggerFactory.getLogger(AntlrDotListener.class);

  private Map<String, Vertex> vertices = new HashMap<>();
  private Vector<Edge> edges = new Vector<>();

  private Vertex src, dst = null;
  private Edge edge = null;

  private boolean expectEdge = false;
  private boolean expectVertex = false;
  private boolean expectEdgeLabel = false;
  private boolean expectVertexLabel = false;


  @Override
  public void enterNode_id(DOTParser.Node_idContext ctx) {
    expectVertex = true;
    logger.trace("Parsing vertex: " + ctx.getText());
    if (!vertices.containsKey(ctx.getText())) {
      if (src == null) {
        logger.trace("Create source vertex: " + ctx.getText());
        src = new Vertex();
        src.setId(ctx.getText());
        src.setName(ctx.getText());
        vertices.put(ctx.getText(), src);
      } else if (dst == null) {
        logger.trace("Create target vertex: " + ctx.getText());
        dst = new Vertex();
        dst.setId(ctx.getText());
        dst.setName(ctx.getText());
        vertices.put(ctx.getText(), dst);
        if (edge != null) {
          logger.trace("Setting source and target vertices for edge");
          edge.setSourceVertex(src);
          edge.setTargetVertex(dst);
        }
      }
    } else {
      if (src == null) {
        src = vertices.get(ctx.getText());
        logger.trace("Using earlier read source vertex: " + src.getId());
      } else if (dst == null) {
        dst = vertices.get(ctx.getText());
        logger.trace("Using earlier read target vertex: " + dst.getId());
      }
    }
  }

  @Override
  public void enterEdgeop(DOTParser.EdgeopContext ctx) {
    expectEdge = true;
    edge = new Edge();
    edges.add(edge);
    logger.debug("Create edge");
  }

  @Override
  public void enterId(DOTParser.IdContext ctx) {
    if (ctx.getText().equalsIgnoreCase("LABEL")) {
      if (expectEdge) {
        expectEdgeLabel = true;
      } else if (expectVertex) {
        expectVertexLabel = true;
      }
      return;
    }

    String label = ctx.getText();
    if (label.length() > 2) {
      label = label.substring(1, label.length() - 1);
    }

    if (expectEdgeLabel) {
      logger.debug("Edge label: " + label);
      expectEdgeLabel = false;
      expectEdge = false;

      edge.setId(label);
      edge.setName(label);
      edge.setSourceVertex(src);
      edge.setTargetVertex(dst);
      src = dst = null;
    } else if (expectVertexLabel) {
      logger.debug("Vertex label: " + label);
      expectVertexLabel = false;
      if (dst != null) {
        dst.setName(label);
      } else if (src != null) {
        src.setName(label);
      }
    }
  }

  public Map<String, Vertex> getVertices() {
    return vertices;
  }

  public Vector<Edge> getEdges() {
    return edges;
  }


}
