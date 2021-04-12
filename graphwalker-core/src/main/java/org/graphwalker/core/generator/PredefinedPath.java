package org.graphwalker.core.generator;

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
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
    int predefinedPathCurrentElementIndex = context.getPredefinedPathCurrentElementIndex();
    Element nextElement = context.getModel().getPredefinedPath().get(predefinedPathCurrentElementIndex + 1);
    context.setCurrentElement(nextElement);
    context.setPredefinedPathCurrentElementIndex(predefinedPathCurrentElementIndex + 1);
    return context;
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }
}
