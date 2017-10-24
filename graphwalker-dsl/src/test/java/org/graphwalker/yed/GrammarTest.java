package org.graphwalker.yed;

/*
 * #%L
 * GraphWalker text parser
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

import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.graphwalker.dsl.yed.YEdEdgeParser;
import org.graphwalker.dsl.yed.YEdLabelLexer;
import org.graphwalker.dsl.yed.YEdVertexParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class GrammarTest {

  private List<String> vertices = Arrays.asList(
      "",
      " ",
      "word1",
      "word1 BLOCKED",
      "word1\nBLOCKED",
      "word1 INIT: x=y;",
      "word1 INIT:x=y;",
      "word1 INIT: x=y;z=0;",
      "word1\n INIT: x=y;",
      "word1 REQTAG:UC02 3.4.1",
      "word1 REQTAG:UC02 3.4.1, UC02 3.4.2",
      "word1\nREQTAG:UC02 3.4.1",
      "word1\nREQTAG:UC02 3.4.1, UC02 3.4.2",
      "word1.word2",
      "word1;word2",
      "word1.word2;word3",
      "word1;word2.word3",
      "word1.word2.word3",
      "word1.word2.word3;word1.word2.word3;word1.word2.word3",
      "word1 // comment",
      "word1\n// my one line comment\nBLOCKED",
      "SHARED:A1",
      "SHARED :A2",
      "SHARED : A3",
      " SHARED: A4",
      "REQTAG=R1",
      "REQTAG = R1",
      "REQTAG= R1,R2 , R3, R4"
  );

  private List<String> edges = Arrays.asList(
      "",
      " ",
      "word1",
      "word1[x=>y]",
      "word1\n[x=>y]",
      "word1/x=y;",
      "word1\n/x=y;",
      "word1[x=>y]/x=y;",
      "word1\n[x=>y]\n/x=y;",
      "word1.word2",
      "word1;word2",
      "word1.word2;word3",
      "word1;word2.word3",
      "word1.word2.word3",
      "word1.word2.word3;word1.word2.word3;word1.word2.word3",
      "word1 // comment",
      "word1\n// my one line comment\n[x>y]",
      "word1 / value = \"ett tu tre\";",
      "weight = 1",
      "word1 weight=0.1 // test this too",
      "word1 weight=1.0",
      "REQTAG=R1",
      "REQTAG = R1",
      "REQTAG= R1,R2 , R3, R4",
      "word1 weight=0.3",
      "word1\nweight=0.3",
      "word1\nweight=0.33333",
      "word1\nweight=.3",
      "word1\nweight=0",
      "WORD1\nweight=1",
      "word1[x=>y]/x=y;\nweight=0.3",
      "word1\nWEIGHT=0.33333",
      "word1 dependency=1",
      "word1\ndependency=1",
      "word1\ndependency=0",
      "word1[x=>y]/x=y;\ndependency=0.3",
      "word1\nDEPENDENCY=2",
      "e_Init / DropUrl=\"https://a.b.c.org/x/y/z/items.aspx\";urlInfo=\"http://data/node\";REST=true;",
      "init / elements = [1,2,3]; value = 0; toString = function(){for(var i = 0; i<elements.length;i++){value+=elements[i]}return value};"
  );

  @Test
  public void testVertexParser() {
    for (String vertex : vertices) {
      CharStream inputStream = CharStreams.fromString(vertex);
      YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      YEdVertexParser parser = new YEdVertexParser(tokens);
      parser.parse();
      Assert.assertThat("Could not parse: " + vertex, parser.getNumberOfSyntaxErrors(), is(0));
    }
  }

  @Test
  public void testEdgeParser() {
    for (String edge : edges) {
      CharStream inputStream = CharStreams.fromString(edge);
      YEdLabelLexer lexer = new YEdLabelLexer(inputStream);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      YEdEdgeParser parser = new YEdEdgeParser(tokens);
      parser.parse();
      Assert.assertThat("Could not parse: " + edge, parser.getNumberOfSyntaxErrors(), is(0));
    }
  }
}
