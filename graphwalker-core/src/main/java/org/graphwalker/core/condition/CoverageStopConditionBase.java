package org.graphwalker.core.condition;

/**
 * @author Nils Olsson
 */
public abstract class CoverageStopConditionBase extends StopConditionBase {

  private final int percent;

  protected CoverageStopConditionBase(int percent) {
    super(String.valueOf(percent));
    if (0 > percent) {
      throw new StopConditionException("A coverage cannot be negative");
    }
    this.percent = percent;
  }

  public int getPercent() {
    return percent;
  }

  protected double getPercentAsDouble() {
    return (double) getPercent() / 100;
  }
}
