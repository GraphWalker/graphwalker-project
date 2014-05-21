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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Element;

/**
 * @author Nils Olsson
 */
public final class Profiler {

    private final ExecutionContext context;
    private final Profile profile = new Profile();

    public Profiler(ExecutionContext context) {
        this.context = context;
    }

    public void start() {
        Element element = context.getCurrentElement();
        if (null != element) {
            if (!profile.containsKey(element)) {
                profile.put(element, new ProfileUnit());
            }
            profile.setTotalVisitCount(profile.getTotalVisitCount()+1);
            ProfileUnit profileUnit = profile.get(element);
            profileUnit.setVisitCount(profileUnit.getVisitCount()+1);
        }
    }

    public void stop() {
        /*
        Element element = context.getCurrentElement();
        if (null != element) {
            if (!profile.containsKey(element)) {
                profile.put(element, new ProfileUnit());
            }
            profile.setTotalVisitCount(profile.getTotalVisitCount()+1);
            ProfileUnit profileUnit = profile.get(element);
            profileUnit.setVisitCount(profileUnit.getVisitCount()+1);
        }
        */
    }

    public boolean isVisited(Element element) {
        ProfileUnit profileUnit = profile.get(element);
        if (null == profileUnit) {
            return false;
        }
        return profileUnit.getVisitCount()>0;
    }

    public long getTotalVisitCount() {
        return profile.getTotalVisitCount();
    }
}
