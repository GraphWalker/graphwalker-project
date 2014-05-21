package org.graphwalker.io.label;

/*
 * #%L
 * GraphWalker Input/Output
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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.graphwalker.io.EdgeParser;
import org.graphwalker.io.LabelLexer;
import org.graphwalker.io.VertexParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class LabelTest {


    private List<String> vertexLabels = Arrays.asList(
            "",
            "Label",
            "/* multi\nline\ncomment */                  Label",
            "",
            "v_vertex  \nSHARED:MyState",
            "SHARED: MyState"
    );

    private List<String> edgeLabels = Arrays.asList(
            "",
            "Label",
            "e_AddBookToCart [num_of_books<=\nMAX_BOOKS] / num_of_books++; /* test */",
            "e_init / num_of_books = 0; MAX_BOOKS = 5;",
            "/* multi\nline\ncomment */ / i++; [i==0] name",
            "BLOCKED",
            "BLOCKED e_AddBookToCart [num_of_books<=\nMAX_BOOKS] / num_of_books++; /* test */",
            "[ i ]",
            "e_Init/validLogin=false;rememberMe=false;"
    );

    @Test
    public void testVertexLabels() {
        for (String label: vertexLabels) {
            ANTLRInputStream inputStream = new ANTLRInputStream(label);
            LabelLexer lexer = new LabelLexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            VertexParser parser = new VertexParser(tokens);
            VertexParser.ParseContext context = parser.parse();
            Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        }
    }

    @Test
    public void testEdgeLabels() {
        for (String label: edgeLabels) {
            ANTLRInputStream inputStream = new ANTLRInputStream(label);
            LabelLexer lexer = new LabelLexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            EdgeParser parser = new EdgeParser(tokens);
            EdgeParser.ParseContext context = parser.parse();
            Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        }
    }


    @Test(expected = AssertionError.class)
    public void badVertexLabel() {
        ANTLRInputStream inputStream = new ANTLRInputStream("1name");
        LabelLexer lexer = new LabelLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VertexParser parser = new VertexParser(tokens);
        VertexParser.ParseContext context = parser.parse();
        Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    }

    @Test(expected = AssertionError.class)
    public void badEdgeLabel() {
        ANTLRInputStream inputStream = new ANTLRInputStream("1name");
        LabelLexer lexer = new LabelLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        EdgeParser parser = new EdgeParser(tokens);
        EdgeParser.ParseContext context = parser.parse();
        Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    }
}
