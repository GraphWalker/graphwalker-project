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
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by krikar on 9/13/14.
 */
public class Util {
    static public String getStepAsString(SimpleMachine machine, boolean verbose, boolean showUnvisited ) {
        StringBuilder str = new StringBuilder();
        try {
            machine.getNextStep();
        } catch (MachineException e) {
            ;
        } finally {
            if (verbose) {
                str.append(FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName()) + " : ");
            }
            if (machine.getCurrentContext().getCurrentElement().hasName()) {
                str.append(machine.getCurrentContext().getCurrentElement().getName());
                if (verbose) {
                    str.append("(" + machine.getCurrentContext().getCurrentElement().getId() + ")");
                    str.append(":" + machine.getCurrentContext().getKeys());
                }
            }

            if (showUnvisited) {
                str.append(" | " + machine.getCurrentContext().getProfiler().getUnvisitedElements().size() +
                        "(" + machine.getCurrentContext().getModel().getElements().size() + ") : ");

                for (Element e : machine.getCurrentContext().getProfiler().getUnvisitedElements()) {
                    str.append(e.getName());
                    if (verbose) {
                        str.append("(" + e.getId() + ")");
                    }
                    str.append(" ");
                }
            }
        }
        return str.toString();
    }

    static public JSONObject getStepAsJSON(SimpleMachine machine, boolean verbose, boolean showUnvisited ) {
        JSONObject obj = new JSONObject();
        try {
            machine.getNextStep();
        } catch (MachineException e) {
            ;
        } finally {
            if (verbose) {
                obj.put("ModelName", FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName()));
            }
            if (machine.getCurrentContext().getCurrentElement().hasName()) {
                obj.put("CurrentElementName", machine.getCurrentContext().getCurrentElement().getName());
                if (verbose) {
                    obj.put("CurrentElementID", machine.getCurrentContext().getCurrentElement().getId());

                    JSONArray jsonKeys = new JSONArray();
                    for (Map.Entry<String,String> key : machine.getCurrentContext().getKeys().entrySet() ) {
                        JSONObject jsonKey = new JSONObject();
                        jsonKey.put(key.getKey(), key.getValue());
                        jsonKeys.put(jsonKey);
                    }
                    obj.put("Data", jsonKeys);
                }
            }

            if (showUnvisited) {
                obj.put("NumberOfElements", machine.getCurrentContext().getModel().getElements().size());
                obj.put("NumberOfUnvisitedElements", machine.getCurrentContext().getProfiler().getUnvisitedElements().size());

                JSONArray jsonElements = new JSONArray();
                for (Element e : machine.getCurrentContext().getProfiler().getUnvisitedElements()) {
                    JSONObject jsonElement = new JSONObject();
                    jsonElement.put("ElementName", e.getName());
                    if (verbose) {
                        jsonElement.put("ElementId", e.getId());
                    }
                    jsonElements.put(jsonElement);
                }
                obj.put("UnvisitedElements", jsonElements);
            }
        }
        return obj;
    }
}
