package org.graphwalker.io.xmlbeans.yed;

/*
 * #%L
 * GraphWalker Input/Output
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.graphdrawing.graphml.xmlns.GraphmlDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class XMLBeansTest {

  @Test
  public void doTest() throws IOException, XmlException {
    GraphmlDocument graphmlDocument = GraphmlDocument.Factory.parse(getClass().getResourceAsStream("/graphml/UC01.graphml"));
    assertThat(graphmlDocument.selectPath(
        "declare namespace xq='http://graphml.graphdrawing.org/xmlns';" +
        "$this/xq:graphml/xq:graph/xq:node"
    ).length, is(8));
    assertThat(graphmlDocument.selectPath(
        "declare namespace xq='http://graphml.graphdrawing.org/xmlns';" +
        "$this/xq:graphml/xq:graph/xq:edge"
    ).length, is(12));
  }
}
