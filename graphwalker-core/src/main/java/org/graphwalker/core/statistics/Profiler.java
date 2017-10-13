package org.graphwalker.core.statistics;

/*-
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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
import org.graphwalker.core.model.Element;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public interface Profiler {
  void addContext(Context context);
  Set<Context> getContexts();
  void start(Context context);
  void stop(Context context);
  long getTotalVisitCount();
  long getVisitCount(Context context, Element element);
  List<Element> getUnvisitedElements();
  List<Element> getUnvisitedElements(Context context);
  List<Element> getVisitedEdges();
  List<Element> getVisitedEdges(Context context);
  List<Element> getUnvisitedEdges();
  List<Element> getUnvisitedEdges(Context context);
  List<Element> getUnvisitedVertices();
  List<Element> getUnvisitedVertices(Context context);
  List<Element> getVisitedVertices();
  List<Element> getVisitedVertices(Context context);
  boolean isVisited(Context context, Element element);
  List<Execution> getExecutionPath();
  long getTotalExecutionTime();
  long getTotalExecutionTime(TimeUnit unit);
  List<Profile> getProfiles();
  Profile getProfile(Context context, Element element);
}
