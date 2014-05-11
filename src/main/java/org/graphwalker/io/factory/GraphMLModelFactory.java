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
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.LabelLexer;
import org.graphwalker.io.LabelParser;
import org.graphwalker.io.common.ResourceUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.*;

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
                    parseVertex(reader, model);
                }
                if ("edge".equals(reader.getLocalName())) {
                    parseEdge(reader, model);
                }
            }
        }
        return model;
    }

    private void parseVertex(XMLStreamReader reader, Model model) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT: {
                    if ("NodeLabel".equals(reader.getLocalName())) {
                        parseNodeLabel(reader, model, reader.getAttributeValue(0));
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

    private void parseNodeLabel(XMLStreamReader reader, Model model, String id) throws XMLStreamException {
        StringBuilder builder = new StringBuilder();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case CHARACTERS: {
                    builder.append(reader.getText());
                }
                break;
                case END_ELEMENT: {
                    LabelParser.ParseContext context = parseLabel(builder.toString());
                    Vertex vertex = new Vertex()
                            .setName(context.name().getText());
                    elements.put(id, vertex);
                    model.addVertex(vertex);

                    return;
                }
            }
        }
    }

    private void parseEdge(XMLStreamReader reader, Model model) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT: {
                    if ("EdgeLabel".equals(reader.getLocalName()) && reader.hasNext()) {
                        parseEdgeLabel(reader, model, reader.getAttributeValue(0), reader.getAttributeValue(1), reader.getAttributeValue(2));
                    }
                }
                break;
                case END_ELEMENT: {
                    if ("edge".equals(reader.getLocalName())) {
                        return;
                    }
                }
            }
        }
    }

    private void parseEdgeLabel(XMLStreamReader reader, Model model, String id, String source, String target) throws XMLStreamException {
        StringBuilder builder = new StringBuilder();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case CHARACTERS: {
                    builder.append(reader.getText());
                }
                break;
                case END_ELEMENT: {
                    LabelParser.ParseContext context = parseLabel(builder.toString());
                    Edge edge = new Edge()
                            .setName(context.name().getText())
                            .setSourceVertex(elements.get(source))
                            .setTargetVertex(elements.get(target));
                    model.addEdge(edge);
                    return;
                }
            }
        }
    }

    private LabelParser.ParseContext parseLabel(String label) {
        ANTLRInputStream inputStream = new ANTLRInputStream(label);
        LabelLexer lexer = new LabelLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LabelParser parser = new LabelParser(tokens);
        return parser.parse();
    }
}
