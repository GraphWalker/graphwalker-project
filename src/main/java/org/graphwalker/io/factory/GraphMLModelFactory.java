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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.graphwalker.core.model.*;
import org.graphwalker.io.LabelLexer;
import org.graphwalker.io.LabelParser;
import org.graphwalker.io.common.ResourceUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.*;
import static org.graphwalker.io.LabelParser.ActionContext;

/**
 * @author Nils Olsson
 */
public final class GraphMLModelFactory implements ModelFactory {

    private Map<String, Vertex> elements = new HashMap<>();

    @Override
    public Model create(String file) {
        try {
            return parse(file);
        } catch (Throwable throwable) {
            throw new ModelFactoryException(throwable);
        }
    }

    private Model parse(String file) throws XMLStreamException {
        Model model = new Model();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(ResourceUtils.getResourceAsStream(file));
        while (reader.hasNext()) {
            if (reader.next() == START_ELEMENT) {
                if ("node".equals(reader.getLocalName())) {
                    Vertex vertex = new Vertex();
                    elements.put(reader.getAttributeValue(null, "id"), vertex);
                    parseVertex(vertex, reader);
                    model.addVertex(vertex);
                }
                if ("edge".equals(reader.getLocalName())) {
                    Edge edge = new Edge();
                    String source = reader.getAttributeValue(null, "source");
                    if (null != elements.get(source)) {
                        edge.setSourceVertex(elements.get(source));
                    }
                    String target = reader.getAttributeValue(null, "target");
                    if (null != elements.get(target)) {
                        edge.setTargetVertex(elements.get(target));
                    }
                    parseEdge(edge, reader);
                    model.addEdge(edge);
                }
            }
        }
        return model;
    }

    private void parseVertex(Vertex vertex, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT: {
                    if ("NodeLabel".equals(reader.getLocalName())) {
                        parseNodeLabel(vertex, reader, reader.getAttributeValue(0));
                    }
                }
                break;
                case END_ELEMENT: {
                    if ("node".equals(reader.getLocalName())) {
                        return;
                    }
                }
            }
        }
    }

    private void parseNodeLabel(Vertex vertex, XMLStreamReader reader, String id) throws XMLStreamException {
        StringBuilder builder = new StringBuilder();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case CHARACTERS: {
                    builder.append(reader.getText());
                }
                break;
                case END_ELEMENT: {
                    LabelParser.ParseContext context = parseLabel(builder.toString());
                    if (null != context.name()) {
                        vertex.setName(context.name().getText());
                    }
                    if (null != context.shared() && null != context.shared().Identifier()) {
                        vertex.setSharedState(context.shared().Identifier().getText());
                    }
                    elements.put(id, vertex);
                    return;
                }
            }
        }
    }

    private void parseEdge(Edge edge, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT: {
                    if ("EdgeLabel".equals(reader.getLocalName()) && reader.hasNext()) {
                        parseEdgeLabel(edge, reader);
                    }
                }
                break;
                case END_ELEMENT: {
                    if ("edge".equals(reader.getLocalName())) {
                        return;
                    }
                }
                break;
            }
        }
    }

    private void parseEdgeLabel(Edge edge, XMLStreamReader reader) throws XMLStreamException {
        StringBuilder builder = new StringBuilder();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case CHARACTERS: {
                    builder.append(reader.getText());
                }
                break;
                case END_ELEMENT: {
                    LabelParser.ParseContext context = parseLabel(builder.toString());
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
                    return;
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

    private LabelParser.ParseContext parseLabel(String label) {
        ANTLRInputStream inputStream = new ANTLRInputStream(label);
        LabelLexer lexer = new LabelLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LabelParser parser = new LabelParser(tokens);
        return parser.parse();
    }
}
