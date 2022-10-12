package org.graphwalker.core.condition;

import org.graphwalker.core.model.Vertex;

public class PredefinedPathStopCondition extends StopConditionBase {

  public PredefinedPathStopCondition() {
    super("PredefinedPath");
  }

  @Override
  public boolean isFulfilled() {
    return getCurrentStepCount() == getTotalStepCount();
  }

  @Override
  public double getFulfilment() {
    return (double) getCurrentStepCount() / getTotalStepCount();
  }

  private int getCurrentStepCount() {
    // *2 because each index increment corresponds to a vertex-edge step pair
    int currentStepCount = getContext().getPredefinedPathCurrentEdgeIndex() * 2;
    if (getContext().getCurrentElement() instanceof Vertex.RuntimeVertex) {
      return currentStepCount + 1;
    }
    return currentStepCount;
  }

  private int getTotalStepCount() {
    return (getContext().getModel().getPredefinedPath().size() * 2) + 1;
  }

}
