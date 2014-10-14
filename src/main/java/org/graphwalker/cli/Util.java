package org.graphwalker.cli;

/*
 * #%L
 * GraphWalker Command Line Interface
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

import org.apache.commons.io.FilenameUtils;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by krikar on 9/13/14.
 */
public abstract class Util {

    public static String getStepAsString(SimpleMachine machine, boolean verbose, boolean showUnvisited) {
        StringBuilder builder = new StringBuilder();
            if (verbose) {
                builder.append(FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName())).append(" : ");
            }
            if (machine.getCurrentContext().getCurrentElement().hasName()) {
                builder.append(machine.getCurrentContext().getCurrentElement().getName());
                if (verbose) {
                    builder.append("(").append(machine.getCurrentContext().getCurrentElement().getId()).append(")");
                    builder.append(":").append(machine.getCurrentContext().getKeys());
                }
            }

            if (showUnvisited) {
                Context context = machine.getCurrentContext();
                builder.append(" | ").append(context.getProfiler().getUnvisitedElements(context).size())
                        .append("(").append(context.getModel().getElements().size()).append(") : ");
                for (Element e : context.getProfiler().getUnvisitedElements(context)) {
                    builder.append(e.getName());
                    if (verbose) {
                        builder.append("(").append(e.getId()).append(")");
                    }
                    builder.append(" ");
                }
            }
        return builder.toString();
    }

    public static JSONObject getStepAsJSON(SimpleMachine machine, boolean verbose, boolean showUnvisited) {
        JSONObject object = new JSONObject();
        if (verbose) {
            object.put("ModelName", FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName()));
        }
        if (machine.getCurrentContext().getCurrentElement().hasName()) {
            object.put("CurrentElementName", machine.getCurrentContext().getCurrentElement().getName());
            if (verbose) {
                object.put("CurrentElementID", machine.getCurrentContext().getCurrentElement().getId());

                JSONArray jsonKeys = new JSONArray();
                for (Map.Entry<String, String> key : machine.getCurrentContext().getKeys().entrySet()) {
                    JSONObject jsonKey = new JSONObject();
                    jsonKey.put(key.getKey(), key.getValue());
                    jsonKeys.put(jsonKey);
                }
                object.put("Data", jsonKeys);
            }
        }
        if (showUnvisited) {
            Context context = machine.getCurrentContext();
            object.put("NumberOfElements", context.getModel().getElements().size());
            object.put("NumberOfUnvisitedElements", context.getProfiler().getUnvisitedElements(context).size());

            JSONArray jsonElements = new JSONArray();
            for (Element element : context.getProfiler().getUnvisitedElements(context)) {
                JSONObject jsonElement = new JSONObject();
                jsonElement.put("ElementName", element.getName());
                if (verbose) {
                    jsonElement.put("ElementId", element.getId());
                }
                jsonElements.put(jsonElement);
            }
            object.put("UnvisitedElements", jsonElements);
        }
        return object;
    }
}
