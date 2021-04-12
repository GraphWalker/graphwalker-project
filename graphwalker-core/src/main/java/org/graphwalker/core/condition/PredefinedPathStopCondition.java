package org.graphwalker.core.condition;

public class PredefinedPathStopCondition extends StopConditionBase {

  public PredefinedPathStopCondition() {
    super("PredefinedPath");
  }

  @Override
  public boolean isFulfilled() {
    return getContext().getPredefinedPathCurrentEdgeIndex() == getContext().getModel().getPredefinedPath().size() - 1;
  }

  @Override
  public double getFulfilment() {
    return (double) getContext().getPredefinedPathCurrentEdgeIndex() / (getContext().getModel().getPredefinedPath().size() - 1);
  }

}
