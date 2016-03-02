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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static org.graphwalker.core.common.Objects.isNotNull;
import static org.graphwalker.core.common.Objects.isNotNullOrEmpty;
import static org.graphwalker.core.common.Objects.isNull;

/**
 * @author Nils Olsson
 */
public final class Assert<T> {

    // language chains, to improve the readability of assertions
    public final Assert<T> a;
    public final Assert<T> an;
    public final Assert<T> to;
    public final Assert<T> be;
    public final Assert<T> it;
    public final Assert<T> is;
    public final Assert<T> that;
    public final Assert<T> has;
    public final Assert<T> have;
    public final Assert<T> which;
    public final Assert<T> with;
    public final Assert<T> at;
    public final Assert<T> of;
    public final Assert<T> same;

    // special chains, to improve functionality of assertions
    public final Assert<T> and;
    public final Assert<T> not;

    private final boolean invert;
    private final T object;

    private Assert(T object) {
        a = an = to = be = it = is = that = and = has = have = which = with = at = of = same = this;
        not = new Assert<>(object, this);
        this.invert = false;
        this.object = object;
    }

    private Assert(T object, Assert<T> parent) {
        a = an = to = be = it = is = that = has = have = which = with = at = of = same = this;
        and = not = parent;
        this.invert = true;
        this.object = object;
    }

    public static <T> Assert<T> expect(T object) {
        return new Assert<>(object);
    }

    public Assert<?> property(String name) {
        return property(name, MessageFactory.build("property", invert, name));
    }

    public Assert<?> property(String name, String message) {
        if (hasField(name)) {
            success(message);
            return new Assert<>(get(name));
        } else if (hasMethod(name)) {
            success(message);
            return new Assert<>(invoke(name));
        } else if (hasMethod("get" + capitalize(name))) {
            success(message);
            return new Assert<>(invoke("get" + capitalize(name)));
        }
        fail(message);
        return this;
    }

    public Assert<T> size(int size) {
        return size(size, MessageFactory.build("size", invert, object.getClass().isArray() ? Array.getLength(object) : invoke("size"), size));
    }

    public Assert<T> size(int size, String message) {
        if (object.getClass().isArray() && Array.getLength(object) == size) {
            success(message);
        } else if (hasMethod("size") && invoke("size").equals(size)) {
            success(message);
        } else {
            fail(message);
        }
        return this;
    }

    public Assert<T> not(Object object) {
        return not.equal(object);
    }

    public Assert<T> not(Object object, String message) {
        return not.equal(object, message);
    }

    public Assert<T> is(Object object) {
        return equal(object);
    }

    public Assert<T> is(Object object, String message) {
        return equal(object, message);
    }

    public Assert<T> equal(Object object) {
        return equal(object, MessageFactory.build("equal", invert, format(this.object), format(object)));
    }

    public Assert<T> equal(Object object, String message) {
        if (areEqual(object, this.object)) {
            success(message);
        } else {
            fail(message);
        }
        return this;
    }

    public Assert<T> a(Class<?> clazz) {
        return a(clazz, MessageFactory.build("type", invert, object.getClass().getName(), isNull(clazz) ? "null" : clazz.getName()));
    }

    public Assert<T> a(Class<?> clazz, String message) {
        if (isNull(clazz) || !clazz.isAssignableFrom(object.getClass())) {
            fail(message);
        } else {
            success(message);
        }
        return this;
    }

    public Assert<T> an(Class<?> clazz) {
        return a(clazz);
    }

    public Assert<T> an(Class<?> clazz, String message) {
        return a(clazz, message);
    }

    public Assert<T> type(Class<?> clazz) {
        return a(clazz);
    }

    public Assert<T> type(Class<?> clazz, String message) {
        return a(clazz, message);
    }

    protected void fail(String message) {
        if (!invert) {
            if (isNull(message)) {
                throw new AssertionError();
            }
            throw new AssertionError(message);
        }
    }

    protected void success(String message) {
        if (invert) {
            if (isNull(message)) {
                throw new AssertionError();
            }
            throw new AssertionError(message);
        }
    }

    private boolean hasField(String name) {
        try {
            object.getClass().getField(name);
        } catch (NoSuchFieldException e) {
            return false;
        }
        return true;
    }

    private Object get(String name) {
        try {
            return object.getClass().getField(name).get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private boolean hasMethod(String name) {
        try {
            object.getClass().getMethod(name);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    private Object invoke(String name) {
        try {
            return isNull(object) ? "null" : object.getClass().getMethod(name).invoke(object);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private String capitalize(String name) {
        if (isNotNullOrEmpty(name)) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;
    }

    private String format(Object object) {
        if (isNull(object)) {
            return "null";
        } else {
            return object.getClass().getName() + "<" + String.valueOf(object) + ">";
        }
    }

    private boolean areEqual(Object object1, Object object2) {
        if (isNull(object1)) {
            return isNull(object2);
        } else if (isNotNull(object2) && isArray(object1)) {
            return isArray(object2) && areArraysEqual(object1, object2);
        } else {
            return object1.equals(object2);
        }
    }

    private boolean areArraysEqual(Object object1, Object object2) {
        return areArrayLengthsEqual(object1, object2) && areArrayElementsEqual(object1, object2);
    }

    private boolean areArrayLengthsEqual(Object object1, Object object2) {
        return Array.getLength(object1) == Array.getLength(object2);
    }

    private boolean areArrayElementsEqual(Object object1, Object object2) {
        for (int i = 0; i < Array.getLength(object1); i++) {
            if (!areEqual(Array.get(object1, i), Array.get(object2, i))) return false;
        }
        return true;
    }

    private boolean isArray(Object object) {
        return object.getClass().isArray();
    }

    static class MessageFactory {

        static final Map<String, Message> messages = new HashMap<>();

        static {
            messages.put("equal", new Message("expected: {0} but was: {1}", "expected not: {0} but was: {1}"));
            messages.put("type", new Message("incompatible types: required: {0} found: {1}", "compatible types: required not: {0} found: {1}"));
            messages.put("size", new Message("expected size: {0} but was: {1}", "expected different size: {0} but was: {1}"));
            messages.put("property", new Message("property: {0} not found", "property: {0} found"));
        }

        static String build(String key, boolean invert, Object... arguments) {
            return MessageFormat.format(messages.get(key).getMessage(invert), arguments);
        }

        static class Message {

            final String standardMessage;
            final String invertedMessage;

            Message(String standardMessage, String invertedMessage) {
                this.standardMessage = standardMessage;
                this.invertedMessage = invertedMessage;
            }

            String getMessage(boolean inverted) {
                return inverted ? invertedMessage : standardMessage;
            }
        }
    }
}
