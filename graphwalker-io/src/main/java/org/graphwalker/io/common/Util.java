package org.graphwalker.io.common;

/*
 * #%L
 * GraphWalker Input/Output
 * %%
 * Copyright (C) 2005 - 2015 GraphWalker
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

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by krikar on 2015-11-04.
 */
public class Util {
    /*
     * Search the elements for a specific property: blocked.
     * If it exists, the element will be removed from
     * the model.
     * If the element is a vertex, all in- and out edges will be removed as well.
     */
    static public void filterBlockedElements(List<Context> executionContexts) {
        for (Context context : executionContexts) {
            Model model = new Model(context.getModel());
            List<Edge> edges = model.getEdges();
            List<Vertex> vertices = model.getVertices();

            ListIterator<Edge> e_it = edges.listIterator();
            while (e_it.hasNext()) {
                Edge edge = e_it.next();
                if (edge.hasProperty("blocked")) {
                    e_it.remove();
                }
            }
            ListIterator<Vertex> v_it = vertices.listIterator();
            while (v_it.hasNext()) {
                Vertex vertex = v_it.next();
                if (vertex.hasProperty("blocked")) {
                    e_it = edges.listIterator();
                    while (e_it.hasNext()) {
                        Edge e = e_it.next();
                        if (e.getSourceVertex() == vertex) {
                            e_it.remove();
                        } else if (e.getTargetVertex() == vertex) {
                            e_it.remove();
                        }
                    }
                    v_it.remove();
                }
            }
            context.setModel(model.build());
        }
    }
}
