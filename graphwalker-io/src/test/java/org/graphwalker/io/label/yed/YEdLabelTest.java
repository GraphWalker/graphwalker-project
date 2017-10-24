package org.graphwalker.io.label.yed;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.graphwalker.dsl.yed.YEdEdgeParser;
import org.graphwalker.dsl.yed.YEdLabelLexer;
import org.graphwalker.dsl.yed.YEdVertexParser;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class YEdLabelTest {


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
      "name /* multi\nline\ncomment */ / i++; [i==0]",
      "BLOCKED",
      "e_AddBookToCart BLOCKED [num_of_books<=\nMAX_BOOKS] / num_of_books++; /* test */",
      "[ i ]",
      "e_Init/validLogin=false;rememberMe=false;"
  );

  @Test
  public void testVertexLabels() {
    for (String label : vertexLabels) {
      YEdVertexParser parser = new YEdVertexParser(getTokens(label));
      YEdVertexParser.ParseContext context = parser.parse();
      assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    }
  }

  @Test
  public void testEdgeLabels() {
    for (String label : edgeLabels) {
      YEdEdgeParser parser = new YEdEdgeParser(getTokens(label));
      YEdEdgeParser.ParseContext context = parser.parse();
      assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    }
  }

  @Test
  public void startVertex() {
    YEdVertexParser parser = new YEdVertexParser(getTokens(" StARt "));
    YEdVertexParser.ParseContext context = parser.parse();
    assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    assertNotNull(context.start());
  }

  @Test
  public void badVertexLabel() {
    YEdVertexParser parser = new YEdVertexParser(getTokens("1name"));
    YEdVertexParser.ParseContext context = parser.parse();
    assertThat(parser.getNumberOfSyntaxErrors(), is(1));
  }

  @Test
  public void badEdgeLabel() {
    YEdEdgeParser parser = new YEdEdgeParser(getTokens("1name"));
    YEdEdgeParser.ParseContext context = parser.parse();
    assertThat(parser.getNumberOfSyntaxErrors(), is(1));
  }

  @Test
  public void testGuard() {
    YEdEdgeParser parser = new YEdEdgeParser(getTokens("[ i[0] ]"));
    YEdEdgeParser.ParseContext context = parser.parse();
    assertThat(parser.getNumberOfSyntaxErrors(), is(0));
    assertThat(context.field(0).guard().getText(), is("[ i[0] ]"));
  }

  private CommonTokenStream getTokens(String label) {
    CharStream inputStream = CharStreams.fromString(label);
    YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
    return new CommonTokenStream(lexer);
  }
}
