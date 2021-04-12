package org.graphwalker.core.condition;

public class PredefinedPathStopCondition extends StopConditionBase {

  public PredefinedPathStopCondition(String value) {
    super(value);
  }

  @Override
  public boolean isFulfilled() {
    return getContext().getPredefinedPathCurrentElementIndex() == getContext().getModel().getPredefinedPath().size();
  }

  @Override
  public double getFulfilment() {
    return (double) getContext().getPredefinedPathCurrentElementIndex() / getContext().getModel().getPredefinedPath().size();
  }

}
