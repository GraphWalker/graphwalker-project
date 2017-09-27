package org.graphwalker.java.source.cache;

/*-
 * #%L
 * GraphWalker Java
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import com.google.gson.annotations.Expose;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public final class CacheEntry {

  @Expose
  private long modified;

  @Expose
  private boolean generated;

  public CacheEntry(long modified, boolean generated) {
    this.modified = modified;
    this.generated = generated;
  }

  public FileTime getLastModifiedTime() {
    return FileTime.from(modified, TimeUnit.MILLISECONDS);
  }

  public boolean isGenerated() {
    return generated;
  }
}
