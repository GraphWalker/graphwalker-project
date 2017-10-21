package org.graphwalker.dsl.antlr.generator;

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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.dsl.antlr.DslErrorListner;
import org.graphwalker.dsl.antlr.DslException;
import org.graphwalker.dsl.generator.GeneratorParser;
import org.graphwalker.dsl.generator.LogicalLexer;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorFactory {

  private GeneratorFactory() {
  }

  public static PathGenerator parse(String str) {
    CharStream inputStream = CharStreams.fromString(str);
    LogicalLexer lexer = new LogicalLexer(inputStream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    GeneratorParser parser = new GeneratorParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(new DslErrorListner());
    GeneratorParser.ParseContext context = parser.parse();

    ParseTreeWalker walker = new ParseTreeWalker();
    GeneratorLoader generatorLoader = new GeneratorLoader();
    walker.walk(generatorLoader, context);

    PathGenerator generator = generatorLoader.getGenerator();
    if (generator == null) {
      throw new DslException("No valid generator found.");
    }
    if (generator.getStopCondition() == null) {
      throw new DslException("No valid stop condition found.");
    }

    return generator;
  }
}
