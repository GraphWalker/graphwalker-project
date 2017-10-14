package org.graphwalker.core.condition;

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

import java.util.concurrent.TimeUnit;

/**
 * <h1>TimeDuration</h1>
 * The TimeDuration stop condition is fulfilled when the executed time of the test has exceeded the
 * time given as a parameter in the constructor.
 * </p>
 *
 * @author Nils Olsson
 */
public class TimeDuration extends StopConditionBase {

  private final long duration;
  private final long timestamp;

  public TimeDuration(long duration, TimeUnit unit) {
    super(String.valueOf(duration));
    if (0 > duration) {
      throw new StopConditionException("A duration cannot be negative");
    }
    this.timestamp = System.nanoTime();
    this.duration = TimeUnit.NANOSECONDS.convert(duration, unit);
  }

  public long getDuration() {
    return TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS);
  }

  @Override
  public boolean isFulfilled() {
    return getFulfilment() >= FULFILLMENT_LEVEL && super.isFulfilled();
  }

  @Override
  public double getFulfilment() {
    return (double) (System.nanoTime() - timestamp) / duration;
  }
}
