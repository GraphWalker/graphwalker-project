package org.graphwalker.io.factory.yed;

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
import org.graphdrawing.graphml.xmlns.GraphmlDocument;
import org.graphdrawing.graphml.xmlns.NodeType;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.*;
import org.graphwalker.io.common.ResourceNotFoundException;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.graphwalker.io.yed.YEdEdgeParser;
import org.graphwalker.io.yed.YEdLabelLexer;
import org.graphwalker.io.yed.YEdVertexParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Nils Olsson
 */
public final class YEdContextFactory implements ContextFactory {

    private static final String NAMESPACE = "declare namespace xq='http://graphml.graphdrawing.org/xmlns';";
    private static final String FILE_TYPE = "graphml";
    private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.graphml"));

    @Override
    public Set<String> getSupportedFileTypes() {
        return SUPPORTED_TYPE;
    }

    @Override
    public boolean accept(java.nio.file.Path path) {
        return path.toFile().toString().endsWith(FILE_TYPE);
    }

    @Override
    public Context create(Path path) {
        return create(path, new YEdContext());
    }

    @Override
    public <T extends Context> T create(Path path, T context) {
        Edge startEdge = null;
        Map<String, Vertex> elements = new HashMap<>();
        Model model = new Model();
        GraphmlDocument document = null;
        try {
            document = GraphmlDocument.Factory.parse(ResourceUtils.getResourceAsStream(path.toString()));
        } catch (XmlException e) {
            throw new ContextFactoryException("The file appears not to be valid yEd formatted.");
        } catch (IOException e) {
            throw new ContextFactoryException("Could not read the file.");
        } catch (ResourceNotFoundException e) {
            throw new ContextFactoryException("Could not read the file.");
        }
        try {
            Vertex startVertex =addVertices(model, context, document, elements);
            startEdge = addEdges(model, context, document, elements, startVertex);
        } catch (XmlException e) {
            throw new ContextFactoryException("The file seams not to be valid yEd formatted.");
        }

        model.setName(path.toString());
        context.setModel(model.build());
        if (null != startEdge) {
            context.setNextElement(startEdge);
        }
        return context;
    }

    private Vertex addVertices(Model model, Context context, GraphmlDocument document, Map<String, Vertex> elements) throws XmlException {
        Vertex startVertex = null;
        for (XmlObject object: document.selectPath(NAMESPACE+"$this/xq:graphml/xq:graph/xq:node")) {
            if (object instanceof NodeType) {
                NodeType node = (NodeType)object;
                for (DataType data: node.getDataArray()) {
                    if (0 < data.getDomNode().getChildNodes().getLength()) {
                        if (isSupportedNode(data.xmlText())) {
                            StringBuilder label = new StringBuilder();
                            for (NodeLabelType nodeLabel : getSupportedNode(data.xmlText()).getNodeLabelArray()) {
                                label.append(((NodeLabelTypeImpl) nodeLabel).getStringValue());
                            }
                            YEdVertexParser parser = new YEdVertexParser(getTokenStream(label.toString()));
                            parser.removeErrorListeners();
                            parser.addErrorListener(new YEdDescriptiveErrorListener());
                            YEdVertexParser.ParseContext parseContext = parser.parse();
                            Vertex vertex = new Vertex();
                            if (null != parseContext.start()) {
                                elements.put(node.getId(), vertex);
                                vertex.setId(node.getId());
                                startVertex = vertex;
                                continue;
                            }
                            if (null != parseContext.name()) {
                                vertex.setName(parseContext.name().getText());
                            }
                            if (null != parseContext.shared() && null != parseContext.shared().Identifier()) {
                                vertex.setSharedState(parseContext.shared().Identifier().getText());
                            }
                            if (null != parseContext.reqtags()) {
                                vertex.addRequirements(convertVertexRequirement(parseContext.reqtags().reqtag()));
                            }
                            if (null != parseContext.actions()) {
                                model.addActions(convertVertexAction(parseContext.actions().action()));
                            }
                            if (null == parseContext.blocked()) {
                                elements.put(node.getId(), vertex);
                                vertex.setId(node.getId());
                                model.addVertex(vertex);
                            }
                        }
                    }
                }
            }
        }
        return startVertex;
    }

    private boolean isSupportedNode(String xml) {
        return xml.contains("GenericNode")
            || xml .contains("ShapeNode")
            || xml.contains("GenericGroupNode")
            || xml.contains("GroupNode")
            || xml.contains("ImageNode")
            || xml.contains("TableNode");
    }

    private com.yworks.xml.graphml.NodeType getSupportedNode(String xml) throws XmlException {
        if (xml.contains("GenericNode")) {
            return GenericNodeDocument.Factory.parse(xml).getGenericNode();
        } else if (xml.contains("ShapeNode")) {
            return ShapeNodeDocument.Factory.parse(xml).getShapeNode();
        } else if (xml.contains("GenericGroupNode")) {
            return GenericGroupNodeDocument.Factory.parse(xml).getGenericGroupNode();
        } else if (xml.contains("GroupNode")) {
            return GroupNodeDocument.Factory.parse(xml).getGroupNode();
        } else if (xml.contains("ImageNode")) {
            return ImageNodeDocument.Factory.parse(xml).getImageNode();
        } else if (xml.contains("TableNode")) {
            return TableNodeDocument.Factory.parse(xml).getTableNode();
        }
        throw new ContextFactoryException("Unsupported node type: "+xml);
    }

    private Edge addEdges(Model model, Context context, GraphmlDocument document, Map<String, Vertex> elements, Vertex startVertex) throws XmlException {
        Edge startEdge = null;
        for (XmlObject object: document.selectPath(NAMESPACE+"$this/xq:graphml/xq:graph/xq:edge")) {
            if (object instanceof org.graphdrawing.graphml.xmlns.EdgeType) {
                org.graphdrawing.graphml.xmlns.EdgeType edgeType = (org.graphdrawing.graphml.xmlns.EdgeType)object;
                for (DataType data: edgeType.getDataArray()) {
                    if (0 < data.getDomNode().getChildNodes().getLength()) {
                        if (isSupportedEdge(data.xmlText())) {
                            StringBuilder label = new StringBuilder();
                            for (EdgeLabelType edgeLabel : getSupportedEdge(data.xmlText()).getEdgeLabelArray()) {
                                label.append(((EdgeLabelTypeImpl) edgeLabel).getStringValue());
                            }
                            YEdEdgeParser.ParseContext parseContext = new YEdEdgeParser(getTokenStream(label.toString())).parse();
                            Edge edge = new Edge();
                            if (null != elements.get(edgeType.getSource())) {
                                edge.setSourceVertex(elements.get(edgeType.getSource()));
                            }
                            if (null != elements.get(edgeType.getTarget())) {
                                edge.setTargetVertex(elements.get(edgeType.getTarget()));
                            }
                            if (null != parseContext.name()) {
                                edge.setName(parseContext.name().getText());
                            }
                            if (null != parseContext.guard()) {
                                // TODO: Fix this in the parser
                                String text = parseContext.guard().getText().trim();
                                edge.setGuard(new Guard(text.substring(1, text.length() - 1)));
                            }
                            if (null != parseContext.actions()) {
                                edge.addActions(convertEdgeAction(parseContext.actions().action()));
                            }
                            if (null != parseContext.reqtags()) {
                                edge.addRequirements(convertEdgeRequirement(parseContext.reqtags().reqtag()));
                            }
                            if (null == parseContext.blocked()) {
                                if (null != edge.getTargetVertex() ) {
                                    if (null != startVertex && edgeType.getSource().equals(startVertex.getId())) {
                                        edge.setSourceVertex(null);
                                        edge.setId(edgeType.getId());
                                        model.addEdge(edge);
                                        startEdge = edge;
                                    } else if (null != edge.getSourceVertex()) {
                                        edge.setId(edgeType.getId());
                                        model.addEdge(edge);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return startEdge;
    }

    private boolean isSupportedEdge(String xml) {
        return xml.contains("PolyLineEdge")
            || xml.contains("GenericEdge")
            || xml.contains("ArcEdge")
            || xml.contains("QuadCurveEdge")
            || xml.contains("SplineEdge")
            || xml.contains("BezierEdge");
    }

    private com.yworks.xml.graphml.EdgeType getSupportedEdge(String xml) throws XmlException {
        if (xml.contains("GenericEdge")) {
            return GenericEdgeDocument.Factory.parse(xml).getGenericEdge();
        } else if (xml.contains("PolyLineEdge")) {
            return PolyLineEdgeDocument.Factory.parse(xml).getPolyLineEdge();
        } else if (xml.contains("ArcEdge")) {
            return ArcEdgeDocument.Factory.parse(xml).getArcEdge();
        } else if (xml.contains("QuadCurveEdge")) {
            return QuadCurveEdgeDocument.Factory.parse(xml).getQuadCurveEdge();
        } else if (xml.contains("SplineEdge")) {
            return SplineEdgeDocument.Factory.parse(xml).getSplineEdge();
        } else if (xml.contains("BezierEdge")) {
            return BezierEdgeDocument.Factory.parse(xml).getBezierEdge();
        }
        throw new ContextFactoryException("Unsupported edge type: "+xml);
    }

    private List<Action> convertEdgeAction(List<YEdEdgeParser.ActionContext> actionContexts) {
        List<Action> actions = new ArrayList<>();
        for (YEdEdgeParser.ActionContext actionContext: actionContexts) {
            actions.add(new Action(actionContext.getText()));
        }
        return actions;
    }

    private List<Action> convertVertexAction(List<YEdVertexParser.ActionContext> actionContexts) {
        List<Action> actions = new ArrayList<>();
        for (YEdVertexParser.ActionContext actionContext: actionContexts) {
            actions.add(new Action(actionContext.getText()));
        }
        return actions;
    }

    private Set<Requirement> convertEdgeRequirement(List<YEdEdgeParser.ReqtagContext> reqtagContexts) {
        Set<Requirement> requirements = new HashSet<>();
        for (YEdEdgeParser.ReqtagContext reqtagContext: reqtagContexts) {
            requirements.add(new Requirement(reqtagContext.getText()));
        }
        return requirements;
    }

    private Set<Requirement> convertVertexRequirement(List<YEdVertexParser.ReqtagContext> reqtagContexts) {
        Set<Requirement> requirements = new HashSet<>();
        for (YEdVertexParser.ReqtagContext reqtagContext: reqtagContexts) {
            requirements.add(new Requirement(reqtagContext.getText()));
        }
        return requirements;
    }

    private CommonTokenStream getTokenStream(String label) {
        ANTLRInputStream inputStream = new ANTLRInputStream(label);
        YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new YEdDescriptiveErrorListener());
        return new CommonTokenStream(lexer);
    }

}
