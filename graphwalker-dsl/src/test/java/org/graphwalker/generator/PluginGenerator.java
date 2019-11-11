package org.graphwalker.generator;

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.NoPathFoundException;
import org.graphwalker.core.generator.PathGeneratorBase;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * A generator that is a code copy of the RandomPathGenerator.
 * It serves the purpose of verifying the runtime loading of an plugin generator
 */
public class PluginGenerator extends PathGeneratorBase<StopCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(PluginGenerator.class);

  private final Random random = new Random(System.nanoTime());

  public PluginGenerator(StopCondition stopCondition) {
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
    context.setCurrentElement(elements.get(random.nextInt(elements.size())));
    return context;
  }

  @Override
  public boolean hasNextStep() {
    return !getStopCondition().isFulfilled();
  }

}
