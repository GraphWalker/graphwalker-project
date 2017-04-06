package org.graphwalker.core;

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

import static org.graphwalker.core.Assert.expect;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class AssertTest {

  @Test
  public void testSize() {
    List<Integer> list = new ArrayList<>();
    expect(list).to.have.size(0);
    list.add(1);
    expect(list).to.have.size(1);
  }

  @Test
  public void testArraySize() {
    int[] array = new int[]{1, 2, 3};
    expect(array).to.have.size(3);
  }

  @Test(expected = AssertionError.class)
  public void testSizeFailure() {
    expect(new ArrayList<>()).to.be.of.size(1);
  }

  @Test
  public void testA() {
    expect(0).to.be.a(Integer.class);
  }

  @Test(expected = AssertionError.class)
  public void testANull() {
    expect(0).to.be.a(null);
  }

  @Test
  public void testAMessage() {
    expect(0).to.be.a(Integer.class, "message");
  }

  @Test
  public void testAEmptyMessage() {
    expect(0).to.be.a(Integer.class, null);
  }

  @Test(expected = AssertionError.class)
  public void testAFailure() {
    expect(0L).to.be.a(Double.class);
  }

  @Test
  public void testAn() {
    expect(0).to.be.an(Integer.class);
  }

  @Test
  public void testAnMessage() {
    expect(0).to.be.an(Integer.class, "message");
  }

  @Test(expected = AssertionError.class)
  public void testAnFailure() {
    expect(0L).to.be.an(Double.class);
  }

  @Test
  public void testEqualNullMessage() {
    expect(1).to.equal(1, null);
  }

  @Test(expected = AssertionError.class)
  public void testNotEqualNullMessage() {
    expect(1).not.to.equal(1, null);
  }

  @Test(expected = AssertionError.class)
  public void testNull() {
    expect(1).to.equal(null);
  }

  @Test
  public void testEqual1() {
    expect(1).to.be.equal(1);
  }

  @Test
  public void testEqual2() {
    expect(2L).to.equal(2L);
  }

  @Test
  public void testEqual3() {
    expect(3.0f).to.equal(3.0f);
  }

  @Test
  public void testEqual4() {
    expect(4.0).to.equal(4.0);
  }

  @Test
  public void testEqual5() {
    int[] array1 = new int[]{1, 2, 3};
    int[] array2 = new int[]{1, 2, 3};
    expect(array1).to.equal(array2);
  }

  @Test
  public void testEqual6() {
    List<Integer> list1 = Arrays.asList(1, 2, 3);
    List<Integer> list2 = Arrays.asList(1, 2, 3);
    expect(list1).to.equal(list2);
  }

  @Test
  public void testEqual7() {
    expect(true).to.equal(true);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure1() {
    expect(1).to.equal(2);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure2() {
    expect(2L).to.equal(3L);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure3() {
    expect(3.0f).to.equal(3.1f);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure4() {
    expect(4.0).to.equal(4.1);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure5a() {
    int[] array1 = new int[]{1, 2, 3};
    int[] array2 = new int[]{1, 2};
    expect(array1).to.equal(array2);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure5b() {
    int[] array1 = new int[]{1, 2, 3};
    int[] array2 = new int[]{1, 2, 3, 4};
    expect(array1).to.equal(array2);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure6a() {
    List<Integer> list1 = Arrays.asList(1, 2, 3);
    List<Integer> list2 = Arrays.asList(1, 2);
    expect(list1).to.equal(list2);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure6b() {
    List<Integer> list1 = Arrays.asList(1, 2, 3);
    List<Integer> list2 = Arrays.asList(1, 2, 3, 4);
    expect(list1).to.equal(list2);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure7() {
    expect(true).to.equal(false);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailure() {
    expect(2).to.be.a(Integer.class).and.equal(4);
  }

  @Test(expected = AssertionError.class)
  public void testEqualFailureNullMessage() {
    expect(2).to.be.a(Integer.class).and.equal(4, null);
  }

  @Test
  public void testIs() {
    expect(2).is(2);
  }

  @Test(expected = AssertionError.class)
  public void testIsMessage() {
    expect(2).is(4, "Wrong value");
  }

  @Test
  public void testProperty() {
    expect(new ArrayList<>()).to.have.property("size");
  }

  @Test
  public void testProperty1() {
    expect(new MyPropertyTest()).to.have.property("value1");
  }

  @Test
  public void testProperty2() {
    expect(new MyPropertyTest()).to.have.property("value2");
  }

  @Test
  public void testProperty3a() {
    expect(new MyPropertyTest()).to.have.property("value3");
  }

  @Test
  public void testProperty3b() {
    expect(new MyPropertyTest()).to.have.property("getValue3");
  }

  @Test(expected = AssertionError.class)
  public void testProperty4() {
    expect(new MyPropertyTest()).to.have.property("value4");
  }

  @Test(expected = AssertionError.class)
  public void testProperty5() {
    expect(new MyPropertyTest()).to.have.property("value5");
  }

  @Test(expected = AssertionError.class)
  public void testProperty6() {
    expect(new MyPropertyTest()).to.have.property("value6");
  }

  @Test(expected = AssertionError.class)
  public void testPropertyFailure() {
    expect(new ArrayList<>()).to.have.property("length");
  }

  @Test
  public void testNot() {
    expect(new ArrayList<>()).to.not.have.property("length");
  }

  @Test
  public void testNotNullMessage() {
    expect(new ArrayList<>()).to.not.have.property("length", null);
  }

  @Test
  public void testChain() {
    expect(1).to.be.of.type(Integer.class)
        .and.not.of.type(Long.class, null)
        .and.have.a.property("MAX_VALUE").that.is(0x7fffffff)
        .and.not(0x80000000);
  }

  @Test(expected = AssertionError.class)
  public void testChainFailure() {
    expect(1).to.be.of.type(Integer.class)
        .and.not.of.type(Long.class, "wrong type")
        .and.have.a.property("MAX_VALUE").that.is.not(0x80000000)
        .and.is(0x7ffffffe);
  }

  @Test(expected = AssertionError.class)
  public void testNotFailure() {
    expect(new ArrayList<>()).to.not.have.property("size");
  }

  @Test
  public void verifyEqualMessage() {
    String message = null;
    try {
      expect(1).to.equal(2);
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("expected: java.lang.Integer<1> but was: java.lang.Integer<2>"));
  }

  @Test
  public void verifyNotEqualMessage() {
    String message = null;
    try {
      expect(1).not.to.equal(1);
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("expected not: java.lang.Integer<1> but was: java.lang.Integer<1>"));
  }

  @Test
  public void verifyTypeMessage() {
    String message = null;
    try {
      expect(Integer.class).to.equal(Long.class);
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("expected: java.lang.Class<class java.lang.Integer> but was: java.lang.Class<class java.lang.Long>"));
  }

  @Test
  public void verifyWrongTypeMessage() {
    String message = null;
    try {
      expect(Integer.class).not.to.equal(Integer.class);
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("expected not: java.lang.Class<class java.lang.Integer> but was: java.lang.Class<class java.lang.Integer>"));
  }

  @Test
  public void verifySizeMessage() {
    String message = null;
    try {
      expect(new int[]{1}).to.have.size(2);
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("expected size: 1 but was: 2"));
  }

  @Test
  public void verifyWrongSizeMessage() {
    String message = null;
    try {
      expect(new int[]{1}).not.to.have.size(1);
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("expected different size: 1 but was: 1"));
  }

  @Test
  public void verifyPropertyNotFoundMessage() {
    String message = null;
    try {
      expect(Integer.class).have.property("myMethods");
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("property: myMethods not found"));
  }

  @Test
  public void verifyPropertyFoundMessage() {
    String message = null;
    try {
      expect(Integer.class).not.have.property("methods");
    } catch (AssertionError e) {
      message = e.getMessage();
    }
    assertThat(message, is("property: methods found"));
  }

  class MyPropertyTest {

    public int value1 = 1;
    private int value2 = 2;
    private int value3 = 3;
    private int value4 = 4;

    public int value2() {
      return value2;
    }

    public int getValue3() {
      return value3;
    }

    private int getValue4() {
      return value4;
    }

    public int getValue5(int value) {
      return value;
    }
  }
}
