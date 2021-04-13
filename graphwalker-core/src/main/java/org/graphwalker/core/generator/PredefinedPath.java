package org.graphwalker.core.generator;

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PredefinedPath extends PathGeneratorBase<StopCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(PredefinedPath.class);

  public PredefinedPath(StopCondition stopCondition) {
    setStopCondition(stopCondition);
  }

  @Override
  public Context getNextStep() {
    Context context = super.getNextStep();
    Element currentElement = context.getCurrentElement();
    List<Element> elements = context.filter(context.getModel().getElements(currentElement));
    if (elements.isEmpty()) {
      LOG.error("currentElement: " + currentElement);
      LOG.error("context.getModel().getElements(): " + context.getModel().getElements());
      throw new NoPathFoundException(context.getCurrentElement());
    }
    Element nextElement;
    if (currentElement instanceof Edge.RuntimeEdge) {
      nextElement = getNextElementFromEdge(context, elements, (Edge.RuntimeEdge) currentElement);
    } else if (currentElement instanceof Vertex.RuntimeVertex) {
      nextElement = getNextElementFromVertex(context, elements, (Vertex.RuntimeVertex) currentElement);
      context.setPredefinedPathCurrentElementIndex(context.getPredefinedPathCurrentEdgeIndex() + 1);
    } else {
      LOG.error("Current element is neither an edge or a vertex");
      throw new NoPathFoundException(context.getCurrentElement());
    }
    context.setCurrentElement(nextElement);
    return context;
  }

  private Element getNextElementFromEdge(Context context, List<Element> reachableElements, Edge.RuntimeEdge currentElement) {
    if (reachableElements.size() != 1) {
      LOG.error("Next vertex of predefined path is ambiguous (after step " + context.getPredefinedPathCurrentEdgeIndex() + ", from edge with id \"" + currentElement.getId() + "\")");
      throw new NoPathFoundException(currentElement);
    }
    return reachableElements.get(0);
  }

  private Element getNextElementFromVertex(Context context, List<Element> reachableElements, Vertex.RuntimeVertex currentElement) {
    Element nextElement = context.getModel().getPredefinedPath().get(context.getPredefinedPathCurrentEdgeIndex());
    if (!reachableElements.contains(nextElement)) {
      LOG.error("Next edge with id \"" + nextElement.getId() + "\" from predefined path is unreachable (either the guarding condition was not met or the edge has a different source vertex.");
      throw new NoPathFoundException(currentElement);
    }
    return nextElement;
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }
}
