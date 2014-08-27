package org.graphwalker.io.label.yed;

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
import org.graphwalker.io.yed.EdgeParser;
import org.graphwalker.io.yed.LabelLexer;
import org.graphwalker.io.yed.VertexParser;
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
            VertexParser parser = new VertexParser(getTokens(label));
            VertexParser.ParseContext context = parser.parse();
            Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        }
    }

    @Test
    public void testEdgeLabels() {
        for (String label: edgeLabels) {
            EdgeParser parser = new EdgeParser(getTokens(label));
            EdgeParser.ParseContext context = parser.parse();
            Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        }
    }

    @Test
    public void startVertex() {
        VertexParser parser = new VertexParser(getTokens(" StARt "));
        VertexParser.ParseContext context = parser.parse();
        Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        Assert.assertNotNull(context.start());
    }

    @Test(expected = AssertionError.class)
    public void badVertexLabel() {
        VertexParser parser = new VertexParser(getTokens("1name"));
        VertexParser.ParseContext context = parser.parse();
        Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    }

    @Test(expected = AssertionError.class)
    public void badEdgeLabel() {
        EdgeParser parser = new EdgeParser(getTokens("1name"));
        EdgeParser.ParseContext context = parser.parse();
        Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    }

    @Test
    public void testGuard() {
        EdgeParser parser = new EdgeParser(getTokens("[ i[0] ]"));
        EdgeParser.ParseContext context = parser.parse();
        Assert.assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        Assert.assertThat(context.guard().getText(), is("[ i[0] ]"));
    }

    private CommonTokenStream getTokens(String label) {
        ANTLRInputStream inputStream = new ANTLRInputStream(label);
        LabelLexer lexer = new LabelLexer(inputStream);
        return new CommonTokenStream(lexer);
    }
}
