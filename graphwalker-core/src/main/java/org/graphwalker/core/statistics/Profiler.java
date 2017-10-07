package org.graphwalker.core.statistics;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import java.util.List;
import java.util.Set;

public interface Profiler {
  void addContext(Context context);
  void start(Context context);
  void stop(Context context);
  long getTotalVisitCount();
  long getVisitCount(Context context, Element element);
  List<Element> getUnvisitedElements(Context context);
  List<Element> getUnvisitedElements();
  List<Element> getVisitedEdges();
  List<Element> getUnvisitedEdges(Context context);
  List<Element> getUnvisitedEdges();
  List<Element> getUnvisitedVertices(Context context);
  List<Element> getUnvisitedVertices();
  List<Element> getVisitedVertices();
  boolean isVisited(Context context, Element element);
  List<Execution> getExecutionPath();
}
