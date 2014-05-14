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


import org.antlr.v4.runtime.misc.NotNull;
import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.CombinedPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.AStarPath;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorLoader extends CLIBaseListener {

    CombinedPath combinedPath;
    StopCondition stopCondition;
    Integer integer;

    public GeneratorLoader(CombinedPath combinedPath) {
        this.combinedPath = combinedPath;
    }

    @Override
    public void exitGenerator(@NotNull CLIParser.GeneratorContext ctx) {
        if (ctx.getText().equalsIgnoreCase("RANDOM")) {
            combinedPath.addPathGenerator(new RandomPath(stopCondition));
        } else if (ctx.getText().equalsIgnoreCase("A_STAR")) {
            combinedPath.addPathGenerator(new AStarPath(stopCondition));
        }
    }

    @Override
    public void exitStopCondition(@NotNull CLIParser.StopConditionContext ctx) {
        if (ctx.edge_coverage().getText().equalsIgnoreCase("edge_coverage")) {
            stopCondition = new EdgeCoverage(100);
        }
    }

    @Override
    public void exitInteger(@NotNull CLIParser.IntegerContext ctx) {
        integer = Integer.getInteger(ctx.getText());
    }
}
