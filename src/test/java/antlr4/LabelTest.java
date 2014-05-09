package antlr4;

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

import org.antlr.v4.runtime.*;
import org.graphwalker.io.LabelLexer;
import org.graphwalker.io.LabelParser;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class LabelTest {

    @Test
    public void simple() { // e_AddBookToCart [num_of_books<=MAX_BOOKS] / num_of_books++; /* test */
        ANTLRInputStream inputStream = new ANTLRInputStream("3e_AddBookToCart");
        LabelLexer lexer = new LabelLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LabelParser parser = new LabelParser(tokens);
        LabelParser.LabelContext context = parser.label();
        int i = 0;
    }


}
