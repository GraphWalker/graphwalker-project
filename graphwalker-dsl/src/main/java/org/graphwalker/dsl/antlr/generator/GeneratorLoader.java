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


import org.antlr.v4.runtime.misc.NotNull;
import org.graphwalker.core.condition.*;
import org.graphwalker.core.generator.*;
import org.graphwalker.dsl.generator.Generator_Parser;
import org.graphwalker.dsl.generator.Generator_ParserBaseListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorLoader extends Generator_ParserBaseListener {

  StopCondition stopCondition = null;
  ArrayList<PathGenerator> pathGenerators = new ArrayList<>();
  ArrayList<StopCondition> stopConditions = new ArrayList<>();

  @Override
  public void exitBooleanAndExpression(@NotNull Generator_Parser.BooleanAndExpressionContext ctx) {
    if (ctx.AND().size() > 0) {
      CombinedCondition combinedCondition = new CombinedCondition();
      combinedCondition.addStopCondition(stopConditions.get(0));
      combinedCondition.addStopCondition(stopConditions.get(1));
      stopCondition = combinedCondition;
    }
  }

  @Override
  public void exitStopCondition(@NotNull Generator_Parser.StopConditionContext ctx) {

    if (ctx.getChild(0).getText().equalsIgnoreCase("never")) {
      stopConditions.add(new Never());
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("edge_coverage") ||
      ctx.getChild(0).getText().equalsIgnoreCase("edgecoverage")) {
      stopConditions.add(new EdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("vertex_coverage") ||
      ctx.getChild(0).getText().equalsIgnoreCase("vertexcoverage")) {
      stopConditions.add(new VertexCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("reached_vertex") ||
      ctx.getChild(0).getText().equalsIgnoreCase("reachedvertex")) {
      stopConditions.add(new ReachedVertex(ctx.getChild(2).getText()));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("reached_edge") ||
      ctx.getChild(0).getText().equalsIgnoreCase("reachededge")) {
      stopConditions.add(new ReachedEdge(ctx.getChild(2).getText()));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("time_duration") ||
      ctx.getChild(0).getText().equalsIgnoreCase("timeduration")) {
      stopConditions.add(new TimeDuration(Long.parseLong(ctx.getChild(2).getText()), TimeUnit.SECONDS));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("dependency_edge_coverage") ||
      ctx.getChild(0).getText().equalsIgnoreCase("dependencyedgecoverage")) {
      stopConditions.add(new DependencyEdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    }
  }

  @Override
  public void exitLogicalExpression(@NotNull Generator_Parser.LogicalExpressionContext ctx) {
    if (ctx.OR().size() > 0) {
      AlternativeCondition alternativeCondition = new AlternativeCondition();
      alternativeCondition.addStopCondition(stopConditions.get(0));
      alternativeCondition.addStopCondition(stopConditions.get(1));
      stopCondition = alternativeCondition;
    }
  }

  @Override
  public void exitGenerator(@NotNull Generator_Parser.GeneratorContext ctx) {
    if (stopConditions.size() == 1) {
      stopCondition = stopConditions.get(0);
    }

    if (ctx.getChild(0).getText().equalsIgnoreCase("random") ||
      ctx.getChild(0).getText().equalsIgnoreCase("randompath")) {
      pathGenerators.add(new RandomPath(stopCondition));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("weighted_random") ||
      ctx.getChild(0).getText().equalsIgnoreCase("weightedrandompath")) {
      pathGenerators.add(new WeightedRandomPath(stopCondition));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("quick_random") ||
      ctx.getChild(0).getText().equalsIgnoreCase("quickrandompath")) {
      pathGenerators.add(new QuickRandomPath(stopCondition));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("a_star") ||
      ctx.getChild(0).getText().equalsIgnoreCase("astarpath")) {
      pathGenerators.add(new AStarPath((ReachedStopCondition) stopCondition));
    } else if (ctx.getChild(0).getText().equalsIgnoreCase("shortest_all_paths") ||
      ctx.getChild(0).getText().equalsIgnoreCase("shortestallpaths")) {
      pathGenerators.add(new ShortestAllPaths(stopCondition));
    }
    stopConditions.clear();
  }

  public PathGenerator getGenerator() {
    if (pathGenerators.isEmpty()) {
      return null;
    } else if (pathGenerators.size() == 1) {
      return pathGenerators.get(0);
    }

    CombinedPath combinedPath = new CombinedPath();
    for (PathGenerator pathGenerator : pathGenerators) {
      combinedPath.addPathGenerator(pathGenerator);
    }
    return combinedPath;
  }
}
