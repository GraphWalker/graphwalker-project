package org.graphwalker.core.generator;

import org.graphwalker.core.algorithm.AStar;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.graphwalker.core.common.Objects.isNull;
import static org.graphwalker.core.model.Edge.RuntimeEdge;

public class AwesomePath extends PathGeneratorBase<StopCondition> {

  private Path<Element> path = null;

  public AwesomePath(StopCondition stopCondition) {
    setStopCondition(stopCondition);
  }

  @Override
  public Context getNextStep() {
    Context context = getContext();
    if (isNull(path) || path.isEmpty()) {
      // lets try find a path to any unvisited element in the model
      path = getPath(context);
      // remove the first element, due to that is also the current element
      path.removeFirst();
    }
    // set the next element in the path as the current element
    return context.setCurrentElement(path.removeFirst());
  }

  private Path<Element> getPath(Context context) {
    try {
      // find the shortest unvisited route
      return getShortestPath(context);
    } catch (Throwable t) {
      // ignore, try with the next unvisited element
    }
    // if there is no more routes, make sure that we stop in vertex, so that we fulfill the base condition for all stop conditions
    if (context.getCurrentElement() instanceof RuntimeEdge) {
      return new Path<>(Arrays.asList(context.getCurrentElement(), ((RuntimeEdge) context.getCurrentElement()).getTargetVertex()));
    }
    // we stoped on a vertex, but there is still unvisited elements, that we don't know how to access
    throw new NoPathFoundException("Could not find a valid path from element: "
      + context.getCurrentElement().getName()
      + " (" + context.getCurrentElement().getId() + ")");
  }

  private Path<Element> getShortestPath(Context context) {
    List<Path> paths = new ArrayList<>();
    // generate routes to all unvisited elements
    for (Element element: context.getProfiler().getUnvisitedElements(context)) {
      try {
        Path<Element> path = context.getAlgorithm(AStar.class).getShortestPath(context.getCurrentElement(), element);
        if (path.size() > 1) {
          // if we found a path that contains more then the current element, add the path to possible routes
          paths.add(path);
        }
      } catch (Throwable t) {
        // ignore, try with the next unvisited element
      }
    }
    // sort the possible routes, so that we return the shortest
    paths.sort((a, b) -> {
      if (a.size() > b.size()) {
        return 1;
      }
      return -1;
    });
    return paths.get(0);
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }
}
