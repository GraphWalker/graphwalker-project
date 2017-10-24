package org.graphwalker.io.factory;

/*
 * #%L
 * GraphWalker Input/Output
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Nils Olsson
 */
public class ContextFactoryScannerTest {

  @Test
  public void validJjson() {
    File jsonFile = new File(getTestClassesDirectory(), ".json");
    assertNotNull(ContextFactoryScanner.get(jsonFile.toPath()));
  }

  @Test(expected = ContextFactoryException.class)
  public void invalidJson() {
    File jsonFile = new File(getTestClassesDirectory(), ".jjson");
    assertNotNull(ContextFactoryScanner.get(jsonFile.toPath()));
  }

  @Test
  public void validDot() {
    File dotFile = new File(getTestClassesDirectory(), "dot/3v2e.dot");
    assertNotNull(ContextFactoryScanner.get(dotFile.toPath()));
  }

  @Test(expected = ContextFactoryException.class)
  public void invalidDot() {
    File dotFile = new File(getTestClassesDirectory(), "dot/3v2e.Ddot");
    assertNotNull(ContextFactoryScanner.get(dotFile.toPath()));
  }

  @Test
  public void validGraphml() {
    File graphmlFile = new File(getTestClassesDirectory(), "graphml/UC01.graphml");
    assertNotNull(ContextFactoryScanner.get(graphmlFile.toPath()));
  }

  @Test(expected = ContextFactoryException.class)
  public void invalidGraphml() {
    File graphmlFile = new File(getTestClassesDirectory(), "graphml/UC01.graphmlgraphml");
    assertNotNull(ContextFactoryScanner.get(graphmlFile.toPath()));
  }

  @Test
  public void validJava() {
    File javaFile = new File(getTestClassesDirectory(), "java/UC01.java");
    assertNotNull(ContextFactoryScanner.get(javaFile.toPath()));
  }

  @Test(expected = ContextFactoryException.class)
  public void invalidJava() {
    File javaFile = new File(getTestClassesDirectory(), "java/java.javva");
    assertNotNull(ContextFactoryScanner.get(javaFile.toPath()));
  }

  public File getTestClassesDirectory() {
    return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
  }
}
