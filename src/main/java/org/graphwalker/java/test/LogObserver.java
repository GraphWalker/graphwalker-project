package org.graphwalker.java.test;

/*
 * #%L
 * GraphWalker Java
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

import org.graphwalker.core.event.EventType;
import org.graphwalker.core.event.Observable;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Olsson
 */
public final class LogObserver implements Observer<Element> {

    static final Logger logger = LoggerFactory.getLogger(LogObserver.class);

    private Element lastElement = null;

    @Override
    public void update(Observable<Element> observable, Element object, EventType type) {
        try {
            if (null != object) {
                StringBuilder builder = new StringBuilder();
                builder.append("State changed [");
                if (null != lastElement) {
                    builder.append(lastElement.hasName() ? lastElement.getName() : "Element[" + lastElement.getId() + "]");
                    builder.append(" -> ");
                }
                builder.append(object.hasName() ? object.getName() : "Element[" + object.getId() + "]");
                builder.append("]");
                logger.info(builder.toString());
                lastElement = object;
            }
        } catch (Throwable t) {
            int i = 0;
        }
    }
}
