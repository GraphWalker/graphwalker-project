/*
 * #%L
 * GraphWalker Command Line Interface
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.Is.is;


public class ConvertFilesTest extends CLITestRoot {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void convertJsonToJson() throws IOException {
        File tempFile = testFolder.newFile("example.json");
        String args[] = {"convert", "--input", "json/example.json", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length() > 0);
    }

    @Test
    public void convertJsonToGraphml() throws IOException {
        File tempFile = testFolder.newFile("example.graphml");
        String args[] = {"convert", "--input", "json/example.json", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertJsonToJava() throws IOException {
        File tempFile = testFolder.newFile("example.java");
        String args[] = {"convert", "--input", "json/example.json", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertJsonToDot() throws IOException {
        File tempFile = testFolder.newFile("example.dot");
        String args[] = {"convert", "--input", "json/example.json", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertGraphmlToGraphml() throws IOException {
        File tempFile = testFolder.newFile("UC01_GW2.graphml");
        String args[] = {"convert", "--input", "graphml/UC01_GW2.graphml", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    /**
     * The java file generated, can be compiled with the cli jar, like:
     * java -jar graphwalker-cli.jar convert -i UC01.graphml UC01.java
     * javac -cp graphwalker-cli.jar UC01.java
     * java -cp .:graphwalker-cli.jar UC01
     * @throws IOException
     */
    @Test
    public void convertGraphmlToJava() throws IOException {
        File tempFile = testFolder.newFile("UC01_GW2.java");
        String args[] = {"convert", "--input", "graphml/UC01_GW2.graphml", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertGraphmlToDot() throws IOException {
        File tempFile = testFolder.newFile("UC01_GW2.Dot");
        String args[] = {"convert", "--input", "graphml/UC01_GW2.graphml", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertDotToDot() throws IOException {
        File tempFile = testFolder.newFile("SimpleGW.dot");
        String args[] = {"convert", "--input", "dot/SimpleGW.dot", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertDotToGraphml() throws IOException {
        File tempFile = testFolder.newFile("SimpleGW.graphml");
        String args[] = {"convert", "--input", "dot/SimpleGW.dot", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }

    @Test
    public void convertDotToJson() throws IOException {
        File tempFile = testFolder.newFile("SimpleGW.json");
        String args[] = {"convert", "--input", "dot/SimpleGW.dot", tempFile.getPath()};
        Result result = runCommand(args);
        Assert.assertThat(result.getError(), is(""));
        Assert.assertTrue(tempFile.length()>0);
    }
}
