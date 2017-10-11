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

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ObjectsTest {

  @Test
  public void testIsNullOrEmpty() throws Exception {
    assertTrue(Objects.isNullOrEmpty((List)null));
    assertTrue(Objects.isNullOrEmpty((Set)null));
    assertTrue(Objects.isNullOrEmpty((Map)null));
    assertTrue(Objects.isNullOrEmpty((String)null));
    assertTrue(Objects.isNullOrEmpty(new ArrayList<>()));
    assertTrue(Objects.isNullOrEmpty(new HashSet<>()));
    assertTrue(Objects.isNullOrEmpty(new HashMap<>()));
    assertTrue(Objects.isNullOrEmpty(""));
    assertFalse(Objects.isNullOrEmpty(Arrays.asList("value")));
    assertFalse(Objects.isNullOrEmpty(new HashSet<>(Arrays.asList("value"))));
    Map<String, String> nonEmptyMap = new HashMap<>();
    nonEmptyMap.put("key", "value");
    assertFalse(Objects.isNullOrEmpty(nonEmptyMap));
    assertFalse(Objects.isNullOrEmpty("value"));
  }

  @Test
  public void testIsNotNullOrEmpty() throws Exception {
    assertFalse(Objects.isNotNullOrEmpty((List)null));
    assertFalse(Objects.isNotNullOrEmpty((Set)null));
    assertFalse(Objects.isNotNullOrEmpty((Map)null));
    assertFalse(Objects.isNotNullOrEmpty((String)null));
    assertFalse(Objects.isNotNullOrEmpty(new ArrayList<>()));
    assertFalse(Objects.isNotNullOrEmpty(new HashSet<>()));
    assertFalse(Objects.isNotNullOrEmpty(new HashMap<>()));
    assertFalse(Objects.isNotNullOrEmpty(""));
    assertTrue(Objects.isNotNullOrEmpty(Arrays.asList("value")));
    assertTrue(Objects.isNotNullOrEmpty(new HashSet<>(Arrays.asList("value"))));
    Map<String, String> nonEmptyMap = new HashMap<>();
    nonEmptyMap.put("key", "value");
    assertTrue(Objects.isNotNullOrEmpty(nonEmptyMap));
    assertTrue(Objects.isNotNullOrEmpty("value"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnmodifiableList() throws Exception {
    List<String> list = Objects.unmodifiableList(new ArrayList<>());
    list.add("value");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnmodifiableSet() throws Exception {
    Set<String> set = Objects.unmodifiableSet(new HashSet<>());
    set.add("value");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnmodifiableMap() throws Exception {
    Map<String, String> map = Objects.unmodifiableMap(new HashMap<>());
    map.put("key", "value");
  }

  @Test
  public void testIsNull() throws Exception {
    assertTrue(Objects.isNull(null));
    assertFalse(Objects.isNull("value"));
  }

  @Test
  public void testIsNotNull() throws Exception {
    assertFalse(Objects.isNotNull(null));
    assertTrue(Objects.isNotNull("value"));
  }

  @Test
  public void testEquals() throws Exception {
    assertTrue(Objects.equals(null, null));
    assertTrue(Objects.equals("value", "value"));
    assertFalse(Objects.equals(null, "value"));
    assertFalse(Objects.equals("value", null));
  }

  @Test
  public void testHash() throws Exception {
    assertTrue(Objects.hash("value") == Objects.hash("value"));
  }

  @Test
  public void testExtension() throws Exception {
    assertTrue(TestObjects.isNull(null));
  }

  private class TestObjects extends Objects {}
}
