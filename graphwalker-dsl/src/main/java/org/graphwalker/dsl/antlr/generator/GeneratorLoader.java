package org.graphwalker.dsl.antlr.generator;

/*
 * #%L
 * GraphWalker Command Line Interface
 * %%
 * Copyright (C) 2005 - 2017 GraphWalker
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.graphwalker.core.condition.*;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.CombinedPath;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.generator.QuickRandomPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.generator.WeightedRandomPath;
import org.graphwalker.dsl.generator.GeneratorParser;
import org.graphwalker.dsl.generator.GeneratorParserBaseListener;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorLoader extends GeneratorParserBaseListener {

  private StopCondition stopCondition = null;
  private final List<PathGenerator> pathGenerators = new ArrayList<>();
  private final List<StopCondition> stopConditions = new ArrayList<>();

  @Override
  public void exitBooleanAndExpression(GeneratorParser.BooleanAndExpressionContext ctx) {
    if (!ctx.AND().isEmpty()) {
      CombinedCondition combinedCondition = new CombinedCondition();
      stopConditions.forEach(combinedCondition::addStopCondition);
      stopCondition = combinedCondition;
    }
  }

  @Override
  public void exitStopCondition(GeneratorParser.StopConditionContext ctx) {
    String conditionName = ctx.getChild(0).getText().toLowerCase();
    if ("never".equals(conditionName)) {
      stopConditions.add(new Never());
    } else if ("edge_coverage".equals(conditionName) || "edgecoverage".equals(conditionName)) {
      stopConditions.add(new EdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("vertex_coverage".equals(conditionName) || "vertexcoverage".equals(conditionName)) {
      stopConditions.add(new VertexCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("reached_vertex".equals(conditionName) || "reachedvertex".equals(conditionName)) {
      stopConditions.add(new ReachedVertex(ctx.getChild(2).getText()));
    } else if ("reached_shared_state".equals(conditionName) || "reachedsharedstate".equals(conditionName)) {
      stopConditions.add(new ReachedSharedState(ctx.getChild(2).getText()));
    } else if ("reached_edge".equals(conditionName) || "reachededge".equals(conditionName)) {
      stopConditions.add(new ReachedEdge(ctx.getChild(2).getText()));
    } else if ("time_duration".equals(conditionName) || "timeduration".equals(conditionName)) {
      stopConditions.add(new TimeDuration(Long.parseLong(ctx.getChild(2).getText()), TimeUnit.SECONDS));
    } else if ("dependency_edge_coverage".equals(conditionName) || "dependencyedgecoverage".equals(conditionName)) {
      stopConditions.add(new DependencyEdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("requirement_coverage".equals(conditionName) || "requirementcoverage".equals(conditionName)) {
      stopConditions.add(new RequirementCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("length".equals(conditionName)) {
      stopConditions.add(new Length(Integer.parseInt(ctx.getChild(2).getText())));
    }
  }

  @Override
  public void exitLogicalExpression(GeneratorParser.LogicalExpressionContext ctx) {
    if (!ctx.OR().isEmpty()) {
      AlternativeCondition alternativeCondition = new AlternativeCondition();
      stopConditions.forEach(alternativeCondition::addStopCondition);
      stopCondition = alternativeCondition;
    }
  }

  @Override
  public void exitGenerator(GeneratorParser.GeneratorContext context) {
    if (stopConditions.size() == 1) {
      stopCondition = stopConditions.get(0);
    }
    String generatorName = context.getChild(0).getText().toLowerCase();
    if ("random".equals(generatorName) || "randompath".equals(generatorName)) {
      pathGenerators.add(new RandomPath(stopCondition));
    } else if ("weighted_random".equals(generatorName) || "weightedrandompath".equals(generatorName)) {
      pathGenerators.add(new WeightedRandomPath(stopCondition));
    } else if ("quick_random".equals(generatorName) || "quickrandom".equals(generatorName) || "quickrandompath".equals(generatorName)) {
      pathGenerators.add(new QuickRandomPath(stopCondition));
    } else if ("a_star".equals(generatorName) || "astarpath".equals(generatorName)) {
      pathGenerators.add(new AStarPath((ReachedStopCondition) stopCondition));
    } else if ("shortest_all_paths".equals(generatorName) || "shortestallpaths".equals(generatorName)) {
      pathGenerators.add(new ShortestAllPaths(stopCondition));
    } else {
      Class generatorClass = GeneratorFactoryScanner.get(generatorName);
      try {
        pathGenerators.add((PathGenerator) generatorClass.getConstructor(StopCondition.class).newInstance(stopCondition));
      } catch (Exception e) {
        throw new GeneratorFactoryException("No suitable generator found with name: " + generatorName + ", because: " + e.getCause());
      }
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
    pathGenerators.forEach(combinedPath::addPathGenerator);
    return combinedPath;
  }
}
