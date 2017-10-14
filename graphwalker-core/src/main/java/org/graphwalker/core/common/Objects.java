package org.graphwalker.core.common;

/*-
 * #%L
 * GraphWalker Core
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

import java.util.*;

/**
 * @author Nils Olsson
 */
public abstract class Objects {

  public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
    return !isNotNullOrEmpty(map);
  }

  public static <K, V> boolean isNotNullOrEmpty(Map<K, V> map) {
    return null != map && !map.isEmpty();
  }

  public static <V> boolean isNullOrEmpty(Set<V> set) {
    return !isNotNullOrEmpty(set);
  }

  public static <V> boolean isNotNullOrEmpty(Set<V> set) {
    return null != set && !set.isEmpty();
  }

  public static <V> boolean isNullOrEmpty(List<V> list) {
    return !isNotNullOrEmpty(list);
  }

  public static <V> boolean isNotNullOrEmpty(List<V> list) {
    return null != list && !list.isEmpty();
  }

  public static boolean isNullOrEmpty(String string) {
    return !isNotNullOrEmpty(string);
  }

  public static boolean isNotNullOrEmpty(String string) {
    return null != string && !"".equals(string);
  }

  public static boolean isNotNull(Object object) {
    return null != object;
  }

  public static boolean isNull(Object object) {
    return null == object;
  }

  public static boolean equals(Object a, Object b) {
    return (a == b) || (isNotNull(a) && a.equals(b));
  }

  public static int hash(Object... values) {
    return Arrays.hashCode(values);
  }

  public static <T> List<T> unmodifiableList(List<? extends T> list) {
    return Collections.unmodifiableList(new ArrayList<>(list));
  }

  public static <T> Set<T> unmodifiableSet(Set<? extends T> set) {
    return Collections.unmodifiableSet(new HashSet<>(set));
  }

  public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> map) {
    return Collections.unmodifiableMap(new HashMap<>(map));
  }
}
