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
// This file is part of the GraphWalker java package
// The MIT License
//
// Copyright (c) 2010 graphwalker.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.graphwalker.cli;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;


public class CorrectModelsTest extends CLITestRoot {

    /**
     * Simplest model
     */
    @Test
    public void simplestModel() {
        String args[] = {"offline", "-j", "false", "-m", "graphml/CorrectModels/simplestModel.graphml", "random(vertex_coverage(100))"};
        runCommand(args);
        Assert.assertThat(errMsg, is(""));
        Assert.assertThat(outMsg, is("e1" + System.lineSeparator() +
            "v1" + System.lineSeparator()));
    }

    /**
     * shortest All Paths Vertex Coverage
     */
    @Test
    public void shortestAllPathsVertexCoverage() {
        String args[] = {"offline", "-j", "false", "-m", "graphml/CorrectModels/shortestAllPathsVertexCoverage.graphml", "shortest_all_paths(vertex_coverage(100))"};
        runCommand(args);
        Assert.assertThat(errMsg, is(""));
        Assert.assertThat(outMsg, is("e1" + System.lineSeparator() +
            "v1" + System.lineSeparator() +
            "e2" + System.lineSeparator() +
            "v2" + System.lineSeparator() +
            "e4" + System.lineSeparator() +
            "v4" + System.lineSeparator() +
            "e6" + System.lineSeparator() +
            "v1" + System.lineSeparator() +
            "e3" + System.lineSeparator() +
            "v3" + System.lineSeparator()));
    }
}
