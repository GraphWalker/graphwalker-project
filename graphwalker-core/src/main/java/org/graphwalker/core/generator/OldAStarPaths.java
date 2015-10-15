package org.graphwalker.core.generator;

/*
 * #%L
 * GraphWalker Core
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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public final class OldAStarPaths extends PathGeneratorBase<StopCondition> {
    private static final Logger logger = LoggerFactory.getLogger(OldAStarPaths.class);

    private Path<Element> path = null;
    // Intermediate storage of fragments of paths
    List<Path<RuntimeEdge>> pathFragments = new ArrayList<>();

    public OldAStarPaths() {
        setStopCondition(new EdgeCoverage(100));
    }

    @Override
    public Context getNextStep() {
        if (null == path) {
            path = getPath();
        }
        getContext().setCurrentElement(path.removeFirst());
        return getContext();
    }

    public Path<Element> getPath() {
        // If the current element is an edge, that's the first element in the path along with
        // its target vertex.
        RuntimeVertex currentVertex;
        if (getContext().getNextElement() instanceof RuntimeEdge) {
            RuntimeEdge edge = (RuntimeEdge) getContext().getNextElement();
            currentVertex = edge.getTargetVertex();
        } else {
            currentVertex = (RuntimeVertex) getContext().getNextElement();
        }

        while (!isAllElementsVisited()) {
            followOutEdge(null, getContext().getModel().getOutEdges(currentVertex));
        }

        return concatenatingPaths(currentVertex);
    }

    private Path<Element> concatenatingPaths(RuntimeVertex currentVertex) {
        logger.debug("Number of fragments: " + pathFragments.size());
        logger.debug("Fragments: " + pathFragments.toString());
        logger.debug(pathFragments.toString());
        path = new Path<>();
        logger.debug("Adding: " + currentVertex.getName());
        path.add(currentVertex);

        if (pathFragments.size()==1) {
            for (RuntimeEdge edge : pathFragments.get(0)) {
                logger.debug("Adding: " + edge.getName());
                path.add(edge);
                logger.debug("Adding: " + edge.getTargetVertex().getName());
                path.add(edge.getTargetVertex());
            }
        } else {
            for (Path<RuntimeEdge> fragmentsTails : pathFragments) {
                for (Path<RuntimeEdge> fragments : pathFragments) {
                    if (fragments == fragmentsTails) {
                        continue;
                    }
                    if (fragments.getFirst().getSourceVertex() == fragmentsTails.getLast().getTargetVertex()) {
                        for (RuntimeEdge edge : fragmentsTails) {
                            logger.debug("Adding: " + edge.getName());
                            path.add(edge);
                            logger.debug("Adding: " + edge.getTargetVertex().getName());
                            path.add(edge.getTargetVertex());
                        }
                        for (RuntimeEdge edge : fragments) {
                            logger.debug("Adding: " + edge.getName());
                            path.add(edge);
                            logger.debug("Adding: " + edge.getTargetVertex().getName());
                            path.add(edge.getTargetVertex());
                        }
                    }
                }
            }
        }
        return path;
    }

    private boolean isAllElementsVisited() {
        HashSet<Element> elements = new HashSet<>();
        for (Path<RuntimeEdge> fragments : pathFragments) {
            elements.addAll(fragments);
        }
        return elements.size() != 0 && elements.containsAll(getContext().getModel().getEdges());
    }

    private void followOutEdge(Path<RuntimeEdge> fragment, List<RuntimeEdge> currentOutEdges) {
        List<Element> elements = new ArrayList<>();
        for (Path<RuntimeEdge> fragments : pathFragments) {
            elements.addAll(fragments);
        }
        if (elements.containsAll(currentOutEdges)) {
            return;
        }

        for (RuntimeEdge edge : currentOutEdges) {
            if (isAllElementsVisited()) {
                return;
            }
            if (fragment == null) {
                fragment = new Path<>();
                pathFragments.add(fragment);
            }
            if (fragment.contains(edge)) {
                continue;
            }
            logger.debug("Adding element to fragment: " + edge.getName());
            fragment.add(edge);
            followOutEdge(fragment, getContext().getModel().getOutEdges(edge.getTargetVertex()));
            fragment = null;
        }
    }

    @Override
    public boolean hasNextStep() {
        return !getStopCondition().isFulfilled();
    }
}
