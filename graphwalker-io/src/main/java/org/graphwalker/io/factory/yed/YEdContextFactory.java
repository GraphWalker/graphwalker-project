package org.graphwalker.io.factory.yed;

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

import com.yworks.xml.graphml.*;
import com.yworks.xml.graphml.impl.EdgeLabelTypeImpl;
import com.yworks.xml.graphml.impl.NodeLabelTypeImpl;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.graphdrawing.graphml.xmlns.*;
import org.graphdrawing.graphml.xmlns.NodeType;
import org.graphdrawing.graphml.xmlns.impl.DataTypeImpl;
import org.graphdrawing.graphml.xmlns.impl.KeyForTypeImpl;
import org.graphdrawing.graphml.xmlns.impl.KeyTypeImpl;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.*;
import org.graphwalker.dsl.antlr.yed.YEdDescriptiveErrorListener;
import org.graphwalker.dsl.yed.YEdEdgeParser;
import org.graphwalker.dsl.yed.YEdLabelLexer;
import org.graphwalker.dsl.yed.YEdVertexParser;
import org.graphwalker.io.common.ResourceNotFoundException;
import org.graphwalker.io.common.ResourceUtils;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author Nils Olsson
 */
public final class YEdContextFactory implements ContextFactory {

  private static final Logger logger = LoggerFactory.getLogger(YEdContextFactory.class);
  private static final String NAMESPACE = "declare namespace xq='http://graphml.graphdrawing.org/xmlns';";
  private static final String FILE_TYPE = "graphml";
  private static final Set<String> SUPPORTED_TYPE = new HashSet<>(Arrays.asList("**/*.graphml"));

  @Override
  public Set<String> getSupportedFileTypes() {
    return SUPPORTED_TYPE;
  }

  @Override
  public boolean accept(java.nio.file.Path path) {
    return FilenameUtils.getExtension(path.toString()).equalsIgnoreCase(FILE_TYPE);
  }

  @Override
  public List<Context> create(Path path) throws IOException {
    List<Context> contexts = new ArrayList<>();

    if (ResourceUtils.isDirectory(path)) {
      DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
      for (Path file : directoryStream) {
        contexts.add(read(file));
      }
    } else {
      contexts.add(read(path));
    }
    return contexts;
  }

  public List<Context> create(String graphmlStr) {
    List<Context> contexts = new ArrayList<>();
    contexts.add(read(graphmlStr));
    return contexts;
  }

  private Context read(Path path) {
    GraphmlDocument document = null;
    try {
      document = GraphmlDocument.Factory.parse(ResourceUtils.getResourceAsStream(path.toString()));
    } catch (XmlException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("The file appears not to be valid yEd formatted.");
    } catch (IOException | ResourceNotFoundException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("Could not read the file.");
    }
    return read(document, FilenameUtils.getBaseName(path.toString()));
  }

  private Context read(String graphmlStr) {
    GraphmlDocument document = null;
    try {
      document = GraphmlDocument.Factory.parse(graphmlStr);
    } catch (XmlException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("The file appears not to be valid yEd formatted.");
    }
    return read(document, "");
  }

  private Context read(GraphmlDocument document, String name) {
    Context context = new YEdContext();
    Edge startEdge;
    Map<String, Vertex> elements = new HashMap<>();
    Model model = new Model();
    try {
      Vertex startVertex = addVertices(model, document, elements);
      startEdge = addEdges(model, document, elements, startVertex);
    } catch (XmlException e) {
      logger.error(e.getMessage());
      throw new ContextFactoryException("The file seems not to be of valid yEd format.");
    }

    model.setName(name);
    context.setModel(model.build());
    if (null != startEdge) {
      context.setNextElement(startEdge);
    }

    return context;
  }

  @Override
  public String getAsString(List<Context> contexts) {
    StringBuilder graphmlStr = new StringBuilder();
    for (Context context : contexts) {
      String newLine = System.lineSeparator();
      StringBuilder str = new StringBuilder();

      str.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>").append(newLine);
      str.append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + "http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\" "
        + "xmlns:y=\"http://www.yworks.com/xml/graphml\">").append(newLine);
      str.append("  <key id=\"d0\" for=\"node\" yfiles.type=\"nodegraphics\"/>").append(newLine);
      str.append("  <key id=\"d1\" for=\"edge\" yfiles.type=\"edgegraphics\"/>").append(newLine);
      str.append("  <graph id=\"G\" edgedefault=\"directed\">").append(newLine);

      for (Vertex.RuntimeVertex v : context.getModel().getVertices()) {
        str.append("    <node id=\"" + v.getId() + "\">").append(newLine);
        str.append("      <data key=\"d0\" >").append(newLine);
        str.append("        <y:ShapeNode >").append(newLine);
        str.append("          <y:Geometry  x=\"241.875\" y=\"158.701171875\" width=\"95.0\" height=\"30.0\"/>").append(newLine);
        str.append("          <y:Fill color=\"#CCCCFF\"  transparent=\"false\"/>").append(newLine);
        str.append("          <y:BorderStyle type=\"line\" width=\"1.0\" color=\"#000000\" />").append(newLine);
        str.append("          <y:NodeLabel x=\"1.5\" y=\"5.6494140625\" width=\"92.0\" height=\"18.701171875\" "
          + "visible=\"true\" alignment=\"center\" fontFamily=\"Dialog\" fontSize=\"12\" "
          + "fontStyle=\"plain\" textColor=\"#000000\" modelName=\"internal\" modelPosition=\"c\" " + "autoSizePolicy=\"content\">"
          + v.getName());

        str.append("</y:NodeLabel>").append(newLine);
        str.append("          <y:Shape type=\"rectangle\"/>").append(newLine);
        str.append("        </y:ShapeNode>").append(newLine);
        str.append("      </data>").append(newLine);
        str.append("    </node>").append(newLine);
      }

      for (Edge.RuntimeEdge e : context.getModel().getEdges()) {
        Vertex.RuntimeVertex src = e.getSourceVertex();
        Vertex.RuntimeVertex dest = e.getTargetVertex();

        if (src == null || dest == null) {
          continue;
        }

        str.append("    <edge id=\"" + e.getId() + "\" source=\"" + src.getId() + "\" target=\"" + dest.getId() + "\">").append(newLine);
        str.append("      <data key=\"d1\" >").append(newLine);
        str.append("        <y:PolyLineEdge >").append(newLine);
        str.append("          <y:Path sx=\"-23.75\" sy=\"15.0\" tx=\"-23.75\" ty=\"-15.0\">").append(newLine);
        str.append("            <y:Point x=\"273.3125\" y=\"95.0\"/>").append(newLine);
        str.append("            <y:Point x=\"209.5625\" y=\"95.0\"/>").append(newLine);
        str.append("            <y:Point x=\"209.5625\" y=\"143.701171875\"/>").append(newLine);
        str.append("            <y:Point x=\"265.625\" y=\"143.701171875\"/>").append(newLine);
        str.append("          </y:Path>").append(newLine);
        str.append("          <y:LineStyle type=\"line\" width=\"1.0\" color=\"#000000\" />").append(newLine);
        str.append("          <y:Arrows source=\"none\" target=\"standard\"/>").append(newLine);

        if (!e.getName().isEmpty()) {
          String label = e.getName();
          
          if (e.hasGuard()) {
            label += newLine + "[" + e.getGuard().getScript() + "]";
          }
          if (e.hasActions()) {
            label += newLine + "/";
            for (Action action : e.getActions()) {
              label += action.getScript();
            }
          }
          
          if (e.getDependency()!=0) {
        	  	label +=  "\ndependency=" + e.getDependency();
          }
          
          label = label.replaceAll("&", "&amp;");
          label = label.replaceAll("<", "&lt;");
          label = label.replaceAll(">", "&gt;");
          label = label.replaceAll("'", "&apos;");
          label = label.replaceAll("\"", "&quot;");

          str.append("          <y:EdgeLabel x=\"-148.25\" y=\"30.000000000000014\" width=\"169.0\" height=\"18.701171875\" "
            + "visible=\"true\" alignment=\"center\" fontFamily=\"Dialog\" fontSize=\"12\" "
            + "fontStyle=\"plain\" textColor=\"#000000\" modelName=\"free\" modelPosition=\"anywhere\" "
            + "preferredPlacement=\"on_edge\" distance=\"2.0\" ratio=\"0.5\">" + label);
          str.append("</y:EdgeLabel>").append(newLine);
        }

        str.append("          <y:BendStyle smoothed=\"false\"/>").append(newLine);
        str.append("        </y:PolyLineEdge>").append(newLine);
        str.append("      </data>").append(newLine);
        str.append("    </edge>").append(newLine);

      }

      str.append("  </graph>").append(newLine);
      str.append("</graphml>").append(newLine);

      graphmlStr.append(str);
    }
    return graphmlStr.toString();
  }

  @Override
  public void write(List<Context> contexts, Path path) throws IOException {
    File folder = path.toFile().getAbsoluteFile();
    Path graphmlFile = Paths.get(folder.toString(), contexts.get(0).getModel().getName() + ".graphml");
    try (OutputStream outputStream = Files.newOutputStream(graphmlFile)) {
      outputStream.write(String.valueOf(getAsString(contexts)).getBytes());
    }
  }

  private Vertex addVertices(Model model, GraphmlDocument document, Map<String, Vertex> elements) throws XmlException {
    Vertex startVertex = null;
    Deque<XmlObject> workQueue = new ArrayDeque<>();
    workQueue.addAll(Arrays.asList(document.selectPath(NAMESPACE + "$this/xq:graphml/xq:graph/xq:node")));

    List<KeyType> keys = getKeyArray(document);
    Map<String, KeyType> propKeys = new HashMap<>();
    for (KeyType key : keys) {
      if (key.getFor() == KeyForTypeImpl.NODE && !key.isSetYfilesType()) {
        propKeys.put(key.getId(), key);
      }
    }

    while (!workQueue.isEmpty()) {
      XmlObject object = workQueue.pop();
      if (object instanceof NodeType) {
        NodeType node = (NodeType) object;
        if (0 < node.getGraphArray().length) {
          for (GraphType subgraph : node.getGraphArray()) {
            workQueue.addAll(Arrays.asList(subgraph.getNodeArray()));
          }
        } else {
          Vertex vertex = new Vertex();
          for (Map.Entry<String, KeyType> entry : propKeys.entrySet()) {
            KeyType value = entry.getValue();
            if (value.isSetDefault()) {
              vertex.setProperty(value.getAttrName(), ((KeyTypeImpl) value).getStringValue().trim());
            }
          }
          for (DataType data : node.getDataArray()) {
            String propName;
            String propCurrentValue;
            String key = data.getKey();
            if (propKeys.containsKey(key)) {
              KeyType currentKey = propKeys.get(key);
              propName = currentKey.getAttrName();
              propCurrentValue = ((DataTypeImpl) data).getStringValue().trim();
              vertex.setProperty(propName, propCurrentValue);
            }

            if (0 < data.getDomNode().getChildNodes().getLength()) {
              if (isSupportedNode(data.xmlText())) {
                StringBuilder label = new StringBuilder();
                com.yworks.xml.graphml.NodeType nodeType = getSupportedNode(data.xmlText());
                if (nodeType == null) {
                  throw new XmlException("Expected a valid vertex");
                }

                for (NodeLabelType nodeLabel : nodeType.getNodeLabelArray()) {
                  label.append(((NodeLabelTypeImpl) nodeLabel).getStringValue());
                }
                YEdVertexParser parser = new YEdVertexParser(getTokenStream(label.toString()));
                parser.removeErrorListeners();
                parser.addErrorListener(YEdDescriptiveErrorListener.INSTANCE);
                YEdVertexParser.ParseContext parseContext = parser.parse();

                vertex.setProperty("x", nodeType.getGeometry().getX());
                vertex.setProperty("y", nodeType.getGeometry().getY());
                if (null != parseContext.start()) {
                  elements.put(node.getId(), vertex);
                  vertex.setId(node.getId());
                  startVertex = vertex;
                } else {
                  for (YEdVertexParser.FieldContext field : parseContext.field()) {
                    if (null != field.names()) {
                      vertex.setName(field.names().getText());
                    }
                    if (null != field.shared() && null != field.shared().Identifier()) {
                      vertex.setSharedState(field.shared().Identifier().getText());
                    }
                    if (null != field.reqtags()) {
                      vertex.setRequirements(convertVertexRequirement(field.reqtags().reqtagList().reqtag()));
                    }
                    if (null != field.actions()) {
                      model.addActions(convertVertexAction(field.actions().action()));
                    }
                    if (null != field.blocked()) {
                      vertex.setProperty("blocked", true);
                    }
                  }
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
    return startVertex;
  }

  private boolean isSupportedNode(String xml) {
    return xml.contains("GenericNode")
      || xml.contains("ShapeNode")
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
    throw new ContextFactoryException("Unsupported node type: " + xml);
  }

  private List<KeyType> getKeyArray(GraphmlDocument document) {
    if (document.getGraphml() != null) {
      return Arrays.asList(document.getGraphml().getKeyArray());
    }
    return Collections.emptyList();
  }

  private Edge addEdges(Model model, GraphmlDocument document, Map<String, Vertex> elements, Vertex startVertex) throws XmlException {
    Edge startEdge = null;

    List<KeyType> keys = getKeyArray(document);
    Map<String, KeyType> propKeys = new HashMap<>();
    for (KeyType key : keys) {
      if (key.getFor() == KeyForTypeImpl.EDGE && !key.isSetYfilesType()) {
        propKeys.put(key.getId(), key);
      }
    }

    for (XmlObject object : document.selectPath(NAMESPACE + "$this/xq:graphml/xq:graph/xq:edge")) {
      if (object instanceof org.graphdrawing.graphml.xmlns.EdgeType) {
        org.graphdrawing.graphml.xmlns.EdgeType edgeType = (org.graphdrawing.graphml.xmlns.EdgeType) object;
        if (edgeType == null) {
          throw new XmlException("Expected a valid edge");
        }
        Edge edge = new Edge();
        for (Map.Entry<String, KeyType> entry : propKeys.entrySet()) {
          KeyType value = entry.getValue();
          if (value.isSetDefault()) {
            edge.setProperty(value.getAttrName(), ((KeyTypeImpl) value).getStringValue().trim());
          }
        }
        for (DataType data : edgeType.getDataArray()) {
          String propName;
          String propCurrentValue;
          String key = data.getKey();
          if (propKeys.containsKey(key)) {
            KeyType currentKey = propKeys.get(key);
            propName = currentKey.getAttrName();
            propCurrentValue = ((DataTypeImpl) data).getStringValue().trim();
            edge.setProperty(propName, propCurrentValue);
          }
          if (0 < data.getDomNode().getChildNodes().getLength()) {
            if (isSupportedEdge(data.xmlText())) {
              StringBuilder label = new StringBuilder();
              com.yworks.xml.graphml.EdgeType supportedEdge = getSupportedEdge(data.xmlText());
              if (supportedEdge != null) {
                for (EdgeLabelType edgeLabel : supportedEdge.getEdgeLabelArray()) {
                  label.append(((EdgeLabelTypeImpl) edgeLabel).getStringValue());
                }
              }
              YEdEdgeParser parser = new YEdEdgeParser(getTokenStream(label.toString()));
              parser.removeErrorListeners();
              parser.addErrorListener(YEdDescriptiveErrorListener.INSTANCE);
              YEdEdgeParser.ParseContext parseContext = parser.parse();

              if (null != elements.get(edgeType.getSource())) {
                edge.setSourceVertex(elements.get(edgeType.getSource()));
              }
              if (null != elements.get(edgeType.getTarget())) {
                edge.setTargetVertex(elements.get(edgeType.getTarget()));
              }
              for (YEdEdgeParser.FieldContext field : parseContext.field()) {
                if (null != field.names()) {
                  edge.setName(field.names().getText());
                }
                if (null != field.guard()) {
                  // TODO: Fix this in the parser
                  String text = field.guard().getText().trim();
                  edge.setGuard(new Guard(text.substring(1, text.length() - 1)));
                }
                if (null != field.actions()) {
                  edge.addActions(convertEdgeAction(field.actions().action()));
                }
                if (null != field.reqtags()) {
                  edge.setRequirements(convertEdgeRequirement(field.reqtags().reqtagList().reqtag()));
                }
                if (null != field.blocked()) {
                  edge.setProperty("blocked", true);
                }
                if (null != field.weight() && null != field.weight().Value()) {
                  edge.setWeight(Double.parseDouble(field.weight().Value().getText()));
                }
                if (null != field.dependency() && null != field.dependency().Value()) {
                    edge.setDependency(Integer.parseInt((field.dependency().Value().getText())));
                  }
              }
              if (null != edge.getTargetVertex()) {
                if (null != startVertex &&
                  null != edgeType.getSource() &&
                  edgeType.getSource().equals(startVertex.getId())) {
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
    throw new ContextFactoryException("Unsupported edge type: " + xml);
  }

  private List<Action> convertEdgeAction(List<YEdEdgeParser.ActionContext> actionContexts) {
    List<Action> actions = new ArrayList<>();
    for (YEdEdgeParser.ActionContext actionContext : actionContexts) {
      actions.add(new Action(actionContext.getText()));
    }
    return actions;
  }

  private List<Action> convertVertexAction(List<YEdVertexParser.ActionContext> actionContexts) {
    List<Action> actions = new ArrayList<>();
    for (YEdVertexParser.ActionContext actionContext : actionContexts) {
      actions.add(new Action(actionContext.getText()));
    }
    return actions;
  }

  private Set<Requirement> convertEdgeRequirement(List<YEdEdgeParser.ReqtagContext> reqtagContexts) {
    Set<Requirement> requirements = new HashSet<>();
    for (YEdEdgeParser.ReqtagContext reqtagContext : reqtagContexts) {
      requirements.add(new Requirement(reqtagContext.getText()));
    }
    return requirements;
  }

  private Set<Requirement> convertVertexRequirement(List<YEdVertexParser.ReqtagContext> reqtagContexts) {
    Set<Requirement> requirements = new HashSet<>();
    for (YEdVertexParser.ReqtagContext reqtagContext : reqtagContexts) {
      requirements.add(new Requirement(reqtagContext.getText()));
    }
    return requirements;
  }

  private CommonTokenStream getTokenStream(String label) {
    CharStream inputStream = CharStreams.fromString(label);
    YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
    lexer.removeErrorListeners();
    lexer.addErrorListener(YEdDescriptiveErrorListener.INSTANCE);
    return new CommonTokenStream(lexer);
  }

}
