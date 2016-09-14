package org.graphwalker.core.condition;

/**
 * @author Miroslav Janeski
 */
public abstract class DependencyCoverageStopConditionBase extends StopConditionBase {

	private final int dependency;

	protected DependencyCoverageStopConditionBase(int dependency) {
		super(String.valueOf(dependency));
		if (0 > dependency) {
			throw new StopConditionException("A dependency cannot be negative");
		}
		this.dependency = dependency;
	}
	
	public int getDependency() {
		return dependency;
	}

	protected double getDependencyAsDouble() {
		return (double) getDependency() / 100;
	}
}
