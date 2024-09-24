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

import org.graphwalker.core.condition.*;
import org.graphwalker.core.generator.*;
import org.graphwalker.dsl.generator.GeneratorParser;
import org.graphwalker.dsl.generator.GeneratorParserBaseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorLoader extends GeneratorParserBaseListener {

  private static final Logger logger = LoggerFactory.getLogger(GeneratorLoader.class);

  private StopCondition stopCondition = null;
  private final List<PathGenerator> pathGenerators = new ArrayList<>();

  static public class Node<T>{
    private T data = null;
    private List<Node> children = new ArrayList<>();
    private Node parent = null;

    public Node(T data) {
      this.data = data;
    }
    public void addChild(Node child) {
      child.setParent(this);
      this.children.add(child);
    }

    public void addChild(T data) {
      Node<T> newChild = new Node<>(data);
      this.addChild(newChild);
    }

    public List<Node> getChildren() {
      return children;
    }

    public T getData() {
      return data;
    }

    private void setParent(Node parent) {
      this.parent = parent;
    }
  }

  Node root = null;
  Node currentNode = null;

  @Override
  public void enterAndExpression(GeneratorParser.AndExpressionContext ctx) {
    Node node = new Node(new CombinedCondition());
    currentNode.addChild(node);
    currentNode = node;
  }

  @Override
  public void exitAndExpression(GeneratorParser.AndExpressionContext ctx) {
    CombinedCondition combinedCondition = (CombinedCondition) currentNode.getData();
    for ( Object child: currentNode.getChildren() ) {
      combinedCondition.addStopCondition((StopCondition) ((Node)child).getData());
    }
    currentNode = currentNode.parent;
  }

  @Override
  public void enterOrExpression(GeneratorParser.OrExpressionContext ctx) {
    Node node = new Node(new AlternativeCondition());
    currentNode.addChild(node);
    currentNode = node;
  }

  @Override
  public void exitOrExpression(GeneratorParser.OrExpressionContext ctx) {
    AlternativeCondition alternativeCondition = (AlternativeCondition) currentNode.getData();
    for ( Object child: currentNode.getChildren() ) {
      alternativeCondition.addStopCondition((StopCondition) ((Node)child).getData());
    }
    currentNode = currentNode.parent;
  }

  @Override
  public void exitStopCondition(GeneratorParser.StopConditionContext ctx) {
    String conditionName = ctx.getChild(0).getText().toLowerCase();
    if ("never".equals(conditionName)) {
      currentNode.addChild(new Never());
    } else if ("edge_coverage".equals(conditionName) || "edgecoverage".equals(conditionName)) {
      currentNode.addChild(new EdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("vertex_coverage".equals(conditionName) || "vertexcoverage".equals(conditionName)) {
      currentNode.addChild(new VertexCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("reached_vertex".equals(conditionName) || "reachedvertex".equals(conditionName)) {
      currentNode.addChild(new ReachedVertex(ctx.getChild(2).getText()));
    } else if ("reached_shared_state".equals(conditionName) || "reachedsharedstate".equals(conditionName)) {
      currentNode.addChild(new ReachedSharedState(ctx.getChild(2).getText()));
    } else if ("reached_edge".equals(conditionName) || "reachededge".equals(conditionName)) {
      currentNode.addChild(new ReachedEdge(ctx.getChild(2).getText()));
    } else if ("time_duration".equals(conditionName) || "timeduration".equals(conditionName)) {
      currentNode.addChild(new TimeDuration(Long.parseLong(ctx.getChild(2).getText()), TimeUnit.SECONDS));
    } else if ("dependency_edge_coverage".equals(conditionName) || "dependencyedgecoverage".equals(conditionName)) {
      currentNode.addChild(new DependencyEdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("requirement_coverage".equals(conditionName) || "requirementcoverage".equals(conditionName)) {
      currentNode.addChild(new RequirementCoverage(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("length".equals(conditionName)) {
      currentNode.addChild(new Length(Integer.parseInt(ctx.getChild(2).getText())));
    } else if ("predefined_path".equals(conditionName) || "predefinedpath".equals(conditionName)) {
      currentNode.addChild(new PredefinedPathStopCondition());
    }
  }

  public void enterGenerator(GeneratorParser.GeneratorContext context) {
    root = new Node<StopCondition>(null);
    currentNode = root;
  }

  @Override
  public void exitGenerator(GeneratorParser.GeneratorContext context) {
    if ( root.getData() != null ) {
      stopCondition = (StopCondition)root.getData();
    } else {
      if (root.getChildren().size() == 0 ) {
        stopCondition = null;
      } else {
        Node node = (Node) root.getChildren().get(0);
        stopCondition = (StopCondition) node.getData();
      }
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
    } else if ("predefined_path".equals(generatorName) || "predefinedpath".equals(generatorName)) {
      pathGenerators.add(new PredefinedPath(stopCondition));
    } else {
        PathGenerator pathGenerator = GeneratorFactoryScanner.get(generatorName);
        pathGenerator.setStopCondition(stopCondition);
        pathGenerators.add(pathGenerator);
        logger.debug("Generator: " + pathGenerator.getClass().getName() + " is loaded");
    }
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
