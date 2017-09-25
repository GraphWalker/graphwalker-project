package org.graphwalker.core.statistics;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;

import java.util.List;
import java.util.Set;

public interface Profiler {
  void start(Context context);
  void stop(Context context);
  Set<Context> getContexts();
  Context getContext(Element element);
  long getTotalVisitCount();
  long getVisitCount(Element element);
  List<Element> getUnvisitedElements(Context context);
  List<Element> getUnvisitedElements();
  List<Element> getVisitedEdges();
  List<Element> getUnvisitedEdges(Context context);
  List<Element> getUnvisitedEdges();
  List<Element> getUnvisitedVertices(Context context);
  List<Element> getUnvisitedVertices();
  List<Element> getVisitedVertices();
  boolean isVisited(Element element);
  Path<Element> getPath();
  void reset();
  Profile getProfile();
}
