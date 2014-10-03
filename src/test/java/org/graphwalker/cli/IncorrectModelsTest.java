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


public class IncorrectModelsTest extends CLITestRoot {
    /**
     * wrong vertex syntax
     */
    @Test
    public void wrongVertexSyntax() {
        String args[] = {"offline", "-m", "graphml/Incorrect Models/wrongVertexSyntax.graphml", "random(edge_coverage(100))"};
        runCommand(args);
        Assert.assertThat(errMsg, is("An error occurred when running command: " +
            "offline -m graphml/Incorrect Models/wrongVertexSyntax.graphml random(edge_coverage(100))" +
            System.lineSeparator() + "no viable alternative at input '1'" + System.lineSeparator() + System.lineSeparator()));
        Assert.assertThat(outMsg, is(""));
    }

    /**
     * missing Start vertex
     */
    @Test
    public void onlyOneVertex() {
        String args[] = {"offline", "-m", "graphml/Incorrect Models/singleVertex.graphml", "random(edge_coverage(100))"};
        runCommand(args);
        Assert.assertThat(errMsg, is("An error occurred when running command: " +
            "offline -m graphml/Incorrect Models/singleVertex.graphml random(edge_coverage(100))" +
            System.lineSeparator() + "No in-edges! Vertex: 'v_1'" + System.lineSeparator() + System.lineSeparator()));
        Assert.assertThat(outMsg, is(""));
    }

    /**
     * single [start] vertex
     */
    @Test
    public void singleStartVertex() {
        String args[] = {"offline", "-m", "graphml/Incorrect Models/singleStartVertex.graphml", "random(edge_coverage(100))"};
        runCommand(args);
        Assert.assertThat(errMsg, is("An error occurred when running command: " +
            "offline -m graphml/Incorrect Models/singleStartVertex.graphml random(edge_coverage(100))" +
            System.lineSeparator() + "No start context found" + System.lineSeparator() + System.lineSeparator()));
        Assert.assertThat(outMsg, is(""));
    }
}
