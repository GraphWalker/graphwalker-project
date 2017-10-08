package org.graphwalker.core.statistics;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
