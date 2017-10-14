package org.graphwalker.core.model;

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

import java.util.Map;
import java.util.Set;

import static org.graphwalker.core.common.Objects.isNull;

/**
 * @author Nils Olsson
 */
public abstract class CachedBuilder<B, T> extends BuilderBase<B, T> {

  private T cache;

  protected void invalidateCache() {
    cache = null;
  }

  protected abstract T createCache();

  @Override
  public T build() {
    if (isNull(cache)) {
      cache = createCache();
    }
    return cache;
  }

  @Override
  public B setId(String id) {
    invalidateCache();
    return super.setId(id);
  }

  @Override
  public B setName(String name) {
    invalidateCache();
    return super.setName(name);
  }

  @Override
  public B addRequirement(Requirement requirement) {
    invalidateCache();
    return super.addRequirement(requirement);
  }

  @Override
  public B setRequirements(Set<Requirement> requirements) {
    invalidateCache();
    return super.setRequirements(requirements);
  }

  @Override
  public B setProperties(Map<String, Object> properties) {
    invalidateCache();
    return super.setProperties(properties);
  }

  @Override
  public B setProperty(String key, Object value) {
    invalidateCache();
    return super.setProperty(key, value);
  }
}
