package org.graphwalker.core.condition;

/**
 * @author Miroslav Janeski
 */
public abstract class DependencyCoverageStopConditionBase extends StopConditionBase {

	private final int percent;

	private final int dependency;

	protected DependencyCoverageStopConditionBase(int percent, int dependency) {
		super(String.valueOf(percent));
		if (0 > percent) {
			throw new StopConditionException("A coverage cannot be negative");
		}
		this.percent = percent;
		if (0 > dependency) {
			throw new StopConditionException("A dependency cannot be negative");
		}
		this.dependency = dependency;
	}

	public int getPercent() {
		return percent;
	}

	protected double getPercentAsDouble() {
		return (double) getPercent() / 100;
	}

	public int getDependency() {
		return dependency;
	}

	protected double getDependencyAsDouble() {
		return (double) getDependency() / 100;
	}
}
