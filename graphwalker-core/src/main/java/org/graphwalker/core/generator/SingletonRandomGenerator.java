package org.graphwalker.core.generator;

/*
 * #%L
 * GraphWalker Core
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SingletonRandomGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(SingletonRandomGenerator.class);
  private static Random random = null;
  private static Boolean isSeeded = null;
  private static SingletonRandomGenerator singletonRandomGenerator = new SingletonRandomGenerator();

  private SingletonRandomGenerator() {
    random = new Random();
    isSeeded = new Boolean(false);
  }

  public static Random random() {
    if (isSeeded) {
      return random;
    }
    long seed = System.nanoTime();
    LOG.info("Seed: " + seed);
    random.setSeed(seed);
    isSeeded = true;
    return random;
  }

  public static int nextInt() {
    if (isSeeded) {
      return random.nextInt();
    }
    long seed = System.nanoTime();
    LOG.info("Seed: " + seed);
    random.setSeed(seed);
    isSeeded = true;
    return random.nextInt();
  }

  public static int nextInt(int bound) {
    if (isSeeded) {
      return random.nextInt(bound);
    }
    long seed = System.nanoTime();
    LOG.info("Seed: " + seed);
    random.setSeed(seed);
    isSeeded = true;
    return random.nextInt(bound);
  }

  public static void setSeed(long seed) {
    LOG.info("Seeded: " + seed);
    isSeeded = true;
    random.setSeed(seed);
  }
}
