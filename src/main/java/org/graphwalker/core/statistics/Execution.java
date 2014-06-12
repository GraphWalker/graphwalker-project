package org.graphwalker.core.statistics;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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
 * @author Nils Olsson
 */
public class Execution {

    private final long timestamp;
    private final long duration;

    public Execution(long timestamp, long duration) {
        this(timestamp, duration, TimeUnit.NANOSECONDS);
    }

    public Execution(long timestamp, long duration, TimeUnit unit) {
        this.timestamp = TimeUnit.NANOSECONDS.convert(timestamp, unit);
        this.duration = TimeUnit.NANOSECONDS.convert(duration, unit);
    }

    public long getTimestamp(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timestamp, unit);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDuration(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(duration, unit);
    }

    public long getDuration() {
        return duration;
    }
}
