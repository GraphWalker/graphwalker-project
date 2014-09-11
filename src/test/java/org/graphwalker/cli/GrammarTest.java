package org.graphwalker.cli;

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
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class GrammarTest {

    private List<String> generators = Arrays.asList(
        "random(never)",
        "a_star(never)",
        "random(vertex_coverage(100))",
        "random(edge_coverage(100))",
        "random(reached_vertex(v_SomeVertex))",
        "random(reached_vertex(e_SomeEdge))",
        "random(requirement_coverage(100))",
        "random(time(500))",
        "random(length(24))",
        "random(edge_coverage(100) or time(500))",
        "random(edge_coverage(100) || time(500))",
        "random(reached_vertex(e_SomeEdge) && edge_coverage(100))",
        "random(reached_vertex(e_SomeEdge) and edge_coverage(100))",
        "random((reached_vertex(e_SomeEdge) and reached_edge(e_SomeEdge)) || time(5000))",
        "random(edge_coverage(100) and never) a_star(reached_vertex(v_SomeName) || edge_coverage(90))",
        "random(reached_vertex(e_SomeEdge) and edge_coverage(100)) random((reached_vertex(e_SomeEdge) and reached_edge(e_SomeEdge)) || time(5000))"
    );

    @Test
    public void testCLI() {
        for (String generator : generators) {
            ANTLRInputStream inputStream = new ANTLRInputStream(generator);
            CLI_Lexer lexer = new CLI_Lexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CLI_Parser parser = new CLI_Parser(tokens);
            CLI_Parser.ParseContext context = parser.parse();
            Assert.assertThat("Could not parse: " + generator, parser.getNumberOfSyntaxErrors(), is(0));
        }
    }
}
