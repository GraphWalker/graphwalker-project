package org.graphwalker.core.algorithm;

/*
 * #%L
 * GraphWalker Core
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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public class Fleury implements Algorithm {

    private final ExecutionContext context;
    private Set<RuntimeEdge> visited;

    public Fleury(ExecutionContext context) {
        this.context = context;
    }

    public Path<Element> getTrail(RuntimeVertex vertex) {
        visited = new HashSet<>();
        // Step 1
        Path<Element> trail = new Path<>();
        RuntimeVertex currentVertex = vertex;
        trail.add(vertex);
        List<RuntimeEdge> edges = new ArrayList<>(context.getModel().getEdges());
        while (!edges.isEmpty()) {
            // Step 2
            RuntimeEdge edge = getNextEdge(currentVertex);
            // Step 3
            trail.add(edge);
            currentVertex = edge.getTargetVertex();
            trail.add(vertex);
            // Step 4
            edges.remove(edge);
        }
        return trail;
    }

    private RuntimeEdge getNextEdge(RuntimeVertex vertex) {

        // TODO: We need to be able to remove edges from the model in order to see if it's a bridge!?
        /*
        int max = context.getAlgorithm(DepthFirstSearch.class).getConnectedComponent(vertex).size();
        for (RuntimeEdge edge: context.getModel().getOutEdges(vertex)) {
            if (!visited.contains(edge)) {
                int count = context.getAlgorithm(DepthFirstSearch.class).getConnectedComponent(vertex).size();
                //if () {

                //}
            }
        }
        */
        throw new AlgorithmException();
    }

}
