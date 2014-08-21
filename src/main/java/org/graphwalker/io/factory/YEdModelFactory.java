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
import org.graphwalker.io.common.ResourceNotFoundException;
import org.graphwalker.io.common.ResourceUtils;

import java.io.IOException;
import java.util.*;

import static org.graphwalker.io.EdgeParser.ActionContext;


/**
 * @author Nils Olsson
 */
public final class YEdModelFactory implements ModelFactory {

    private static final String NAMESPACE = "declare namespace xq='http://graphml.graphdrawing.org/xmlns';";
    private static final String FILE_TYPE = "graphml";
    private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.graphml"));

    private Map<String, Vertex> elements = new HashMap<>();

    @Override
    public boolean accept(java.nio.file.Path path) {
        return path.toFile().toString().endsWith(FILE_TYPE);
    }

    @Override
    public Model create(String file) {
        Model model = new Model();
        GraphmlDocument document = null;
        try {
          document = GraphmlDocument.Factory.parse(ResourceUtils.getResourceAsStream(file));
        } catch (XmlException e) {
          throw new ModelFactoryException("The file appears not to be valid yEd formatted.");
        } catch (IOException e) {
          throw new ModelFactoryException("Could not read the file.");
        } catch (ResourceNotFoundException e) {
          throw new ModelFactoryException("Could not read the file.");
        }
        try {
          addVertices(model, document);
          addEdges(model, document);
        } catch (XmlException e) {
          throw new ModelFactoryException("The file seams not to be valid yEd formatted.");
        } catch (YEdParsingException e) {
          throw new ModelFactoryException("The model does not fulfill the rules for GraphWalker");
        }
        return model;
    }

    @Override
    public Set<String> getSupportedFileTypes() {
        return SUPPORTED_TYPE;
    }

    private void addVertices(Model model, GraphmlDocument document) throws XmlException {
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
                            VertexParser parser = new VertexParser(getTokenStream(label.toString()));
                            parser.removeErrorListeners();
                            parser.addErrorListener(new DescriptiveErrorListener());
                            VertexParser.ParseContext context = parser.parse();
                            Vertex vertex = new Vertex();
                            if (null != context.start()) {
                                vertex.setStartVertex(true);
                            }
                            if (null != context.name()) {
                                vertex.setName(context.name().getText());
                            }
                            if (null != context.shared() && null != context.shared().Identifier()) {
                                vertex.setSharedState(context.shared().Identifier().getText());
                            }
                            if (null == context.blocked()) {
                                elements.put(node.getId(), vertex);
                                vertex.setId(node.getId());
                                model.addVertex(vertex);
                            }
                        }
                    }
                }
            }
        }
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
        throw new ModelFactoryException("Unsupported node type: "+xml);
    }

    private void addEdges(Model model, GraphmlDocument document) throws XmlException {
        for (XmlObject object: document.selectPath(NAMESPACE+"$this/xq:graphml/xq:graph/xq:edge")) {
            if (object instanceof EdgeType) {
                EdgeType edgeType = (EdgeType)object;
                for (DataType data: edgeType.getDataArray()) {
                    if (0 < data.getDomNode().getChildNodes().getLength()) {
                        if (isSupportedEdge(data.xmlText())) {
                            StringBuilder label = new StringBuilder();
                            for (EdgeLabelType edgeLabel : getSupportedEdge(data.xmlText()).getEdgeLabelArray()) {
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
                                // TODO: Fix this in the parser
                                String text = context.guard().getText().trim();
                                edge.setGuard(new Guard(text.substring(1, text.length() - 1)));
                            }
                            if (null != context.actions()) {
                                edge.addActions(convertEdgeAction(context.actions().action()));
                            }
                            if (null == context.blocked()) {
                                if (null != edge.getSourceVertex() && null != edge.getTargetVertex()) {
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
        throw new ModelFactoryException("Unsupported edge type: "+xml);
    }

    private List<Action> convertEdgeAction(List<EdgeParser.ActionContext> actionContexts) {
        List<Action> actions = new ArrayList<>();
        for (EdgeParser.ActionContext actionContext: actionContexts) {
            actions.add(new Action(actionContext.getText()));
        }
        return actions;
    }

    private CommonTokenStream getTokenStream(String label) {
        ANTLRInputStream inputStream = new ANTLRInputStream(label);
        LabelLexer lexer = new LabelLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DescriptiveErrorListener());
        return new CommonTokenStream(lexer);
    }

}
