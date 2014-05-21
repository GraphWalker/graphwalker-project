package org.graphwalker.io.factory;

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

import com.yworks.xml.graphml.*;
import com.yworks.xml.graphml.impl.EdgeLabelTypeImpl;
import com.yworks.xml.graphml.impl.NodeLabelTypeImpl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.graphdrawing.graphml.xmlns.DataType;
import org.graphdrawing.graphml.xmlns.EdgeType;
import org.graphdrawing.graphml.xmlns.GraphmlDocument;
import org.graphdrawing.graphml.xmlns.NodeType;
import org.graphwalker.core.model.*;
import org.graphwalker.io.EdgeParser;
import org.graphwalker.io.LabelLexer;
import org.graphwalker.io.VertexParser;
import org.graphwalker.io.common.ResourceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.graphwalker.io.EdgeParser.ActionContext;


/**
 * @author Nils Olsson
 */
public final class GraphMLModelFactory implements ModelFactory {

    private static final String NAMESPACE = "declare namespace xq='http://graphml.graphdrawing.org/xmlns';";

    private Map<String, Vertex> elements = new HashMap<>();

    @Override
    public Model create(String file) {
        try {
            Model model = new Model();
            GraphmlDocument document = GraphmlDocument.Factory.parse(ResourceUtils.getResourceAsStream(file));
            addVertices(model, document);
            addEdges(model, document);
            return model;
        } catch (Throwable throwable) {
            throw new ModelFactoryException(throwable);
        }
    }

    private void addVertices(Model model, GraphmlDocument document) throws XmlException {
        for (XmlObject object: document.selectPath(NAMESPACE+"$this/xq:graphml/xq:graph/xq:node")) {
            if (object instanceof NodeType) {
                NodeType node = (NodeType)object;
                for (DataType data: node.getDataArray()) {
                    if (0 < data.getDomNode().getChildNodes().getLength()) {
                        ShapeNodeType shape = ShapeNodeDocument.Factory.parse(data.xmlText()).getShapeNode();
                        StringBuilder label = new StringBuilder();
                        for (NodeLabelType nodeLabel : shape.getNodeLabelArray()) {
                            label.append(((NodeLabelTypeImpl) nodeLabel).getStringValue());
                        }
                        VertexParser.ParseContext context = new VertexParser(getTokenStream(label.toString())).parse();
                        Vertex vertex = new Vertex();
                        if (null != context.name()) {
                            vertex.setName(context.name().getText());
                        }
                        if (null != context.shared() && null != context.shared().Identifier()) {
                            vertex.setSharedState(context.shared().Identifier().getText());
                        }
                        elements.put(node.getId(), vertex);
                        model.addVertex(vertex);
                    }
                }
            }
        }
    }

    private void addEdges(Model model, GraphmlDocument document) throws XmlException {
        for (XmlObject object: document.selectPath(NAMESPACE+"$this/xq:graphml/xq:graph/xq:edge")) {
            if (object instanceof EdgeType) {
                EdgeType edgeType = (EdgeType)object;
                for (DataType data: edgeType.getDataArray()) {
                    if (0 < data.getDomNode().getChildNodes().getLength()) {
                        PolyLineEdgeType polyLineEdge = PolyLineEdgeDocument.Factory.parse(data.xmlText()).getPolyLineEdge();
                        StringBuilder label = new StringBuilder();
                        for (EdgeLabelType edgeLabel : polyLineEdge.getEdgeLabelArray()) {
                            label.append(((EdgeLabelTypeImpl) edgeLabel).getStringValue());
                        }
                        EdgeParser.ParseContext context = new EdgeParser(getTokenStream(label.toString())).parse();
                        Edge edge = new Edge();
                        if (null != elements.get(edgeType.getSource())) {
                            edge.setSourceVertex(elements.get(edgeType.getSource()));
                        }
                        if (null != elements.get(edgeType.getTarget())) {
                            edge.setTargetVertex(elements.get(edgeType.getTarget()));
                        }
                        if (null != context.name()) {
                            edge.setName(context.name().getText());
                        }
                        if (null != context.guard()) {
                            edge.setGuard(new Guard(context.guard().getText()));
                        }
                        if (null != context.actions()) {
                            edge.addActions(convert(context.actions().action()));
                        }
                        if (null != context.blocked()) {
                            edge.setBlocked(true);
                        }
                        model.addEdge(edge);
                    }
                }
            }
        }
    }

    private List<Action> convert(List<ActionContext> actionContexts) {
        List<Action> actions = new ArrayList<>();
        for (ActionContext actionContext: actionContexts) {
            actions.add(new Action(actionContext.getText()));
        }
        return actions;
    }

    private CommonTokenStream getTokenStream(String label) {
        ANTLRInputStream inputStream = new ANTLRInputStream(label);
        LabelLexer lexer = new LabelLexer(inputStream);
        return new CommonTokenStream(lexer);
    }

}
