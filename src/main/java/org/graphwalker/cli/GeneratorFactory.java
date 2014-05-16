package org.graphwalker.cli;

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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.graphwalker.core.generator.CombinedPath;
import org.graphwalker.core.generator.PathGenerator;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorFactory {
    public static PathGenerator parse(String str) {
        ANTLRInputStream inputStream = new ANTLRInputStream(str);
        CLI_Lexer lexer = new CLI_Lexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CLI_Parser parser = new CLI_Parser(tokens);
        CLI_Parser.ParseContext context = parser.parse();

        ParseTreeWalker walker = new ParseTreeWalker();
        CombinedPath combinedPath = new CombinedPath();
        GeneratorLoader generatorLoader = new GeneratorLoader(combinedPath);
        walker.walk(generatorLoader, context);


        return null;
    }
}
