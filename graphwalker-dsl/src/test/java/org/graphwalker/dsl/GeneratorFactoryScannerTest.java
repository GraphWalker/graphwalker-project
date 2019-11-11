package org.graphwalker.dsl;

import org.graphwalker.dsl.antlr.generator.GeneratorFactoryScanner;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GeneratorFactoryScannerTest {
  @Test
  public void validPluginGenerator() {
    assertNotNull(GeneratorFactoryScanner.get("PluginGenerator"));
  }
}
