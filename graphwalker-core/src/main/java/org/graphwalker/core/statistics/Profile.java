package org.graphwalker.core.statistics;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Profile {

  private final Context context;
  private final Element element;
  private final List<Execution> executions;

  public Profile(Context context, Element element, List<Execution> executions) {
    this.context = context;
    this.element = element;
    this.executions = executions;
  }

  public Context getContext() {
    return context;
  }

  public Element getElement() {
    return element;
  }

  public long getExecutionCount() {
    return executions.size();
  }

  public long getMinExecutionTime() {
    return getMinExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getMinExecutionTime(TimeUnit unit) {
    return unit.convert(executions.stream()
      .mapToLong(Execution::getDuration).min().getAsLong(), TimeUnit.NANOSECONDS);
  }

  public long getMaxExecutionTime() {
    return getMaxExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getMaxExecutionTime(TimeUnit unit) {
    return unit.convert(executions.stream()
      .mapToLong(Execution::getDuration).max().getAsLong(), TimeUnit.NANOSECONDS);
  }

  public long getTotalExecutionTime() {
    return getTotalExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getTotalExecutionTime(TimeUnit unit) {
    return unit.convert(executions.stream()
      .mapToLong(Execution::getDuration).sum(), TimeUnit.NANOSECONDS);
  }

  public long getAverageExecutionTime() {
    return getAverageExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getAverageExecutionTime(TimeUnit unit) {
    return unit.convert(Math.round(executions.stream()
      .mapToLong(Execution::getDuration).average().getAsDouble()), TimeUnit.NANOSECONDS);
  }

  public long getFirstExecutionTime() {
    return getFirstExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getFirstExecutionTime(TimeUnit unit) {
    return executions.stream().findFirst().get().getDuration(unit);
  }

  public long getLastExecutionTime() {
    return getLastExecutionTime(TimeUnit.NANOSECONDS);
  }

  public long getLastExecutionTime(TimeUnit unit) {
    return executions.stream().reduce((first, second) -> second).get().getDuration(unit);
  }
}
