package org.graphwalker.java.utils;

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

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.regex.Pattern;

/**
 * Inspired by http://www.shaunabram.com/hamcrest-matcher/
 */
public class OccurrencesOfString extends TypeSafeMatcher {

  private final String str;
  private final int count;

  public OccurrencesOfString(String str, int count) {
    this.str = str;
    this.count = count;
  }

  @Override
  protected boolean matchesSafely(Object o) {
    if (!(o instanceof String)) {
      return false;
    }
    String item = (String) o;
    Pattern p = Pattern.compile(str);
    java.util.regex.Matcher m = p.matcher(item);
    int occurrences = 0;
    while (m.find()) {
      occurrences++;
    }
    if (occurrences == count) {
      return true;
    }
    return false;
  }

  public void describeTo(Description description) {
    description.appendText("String containing ")
      .appendText(count + " occurrences of ")
      .appendText(str);

  }

  @Factory
  public static Matcher occurrencesOfString(String str, int count) {
    return new OccurrencesOfString(str, count);
  }
}
