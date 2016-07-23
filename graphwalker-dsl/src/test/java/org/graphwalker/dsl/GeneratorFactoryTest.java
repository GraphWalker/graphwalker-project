package org.graphwalker.dsl;

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

import org.graphwalker.core.condition.*;
import org.graphwalker.core.generator.*;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorFactoryTest {

  @Test // Single stop condition
  public void test1() {
    PathGenerator generator = GeneratorFactory.parse("random(edge_coverage(100))");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(EdgeCoverage.class));
    Assert.assertThat(((EdgeCoverage) generator.getStopCondition()).getPercent(), is(100));
  }

  @Test // Single stop condition
  public void test2() {
    PathGenerator generator = GeneratorFactory.parse("random(vertex_coverage(100))");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(VertexCoverage.class));
    Assert.assertThat(((VertexCoverage) generator.getStopCondition()).getPercent(), is(100));
  }

  @Test // Single stop condition
  public void test3() {
    PathGenerator generator = GeneratorFactory.parse("a_star(reached_vertex(v_ABC))");
    Assert.assertThat(generator, instanceOf(AStarPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(ReachedVertex.class));
    Assert.assertThat(((ReachedVertex) generator.getStopCondition()).getValue(), is("v_ABC"));
  }

  @Test // Single stop condition
  public void test4() {
    PathGenerator generator = GeneratorFactory.parse("random(reached_edge(edgeName))");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(ReachedEdge.class));
    Assert.assertThat(((ReachedEdge) generator.getStopCondition()).getValue(), is("edgeName"));
  }

  @Test // Single stop condition
  public void test5() {
    PathGenerator generator = GeneratorFactory.parse("random(reached_vertex(vertexName))");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(ReachedVertex.class));
    Assert.assertThat(((ReachedVertex) generator.getStopCondition()).getValue(), is("vertexName"));
  }

  @Test // Single stop condition
  public void test6() {
    PathGenerator generator = GeneratorFactory.parse("random(time_duration(600))");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(TimeDuration.class));
    Assert.assertThat(((TimeDuration) generator.getStopCondition()).getDuration(), is(600l));
  }

  @Test // 2 stop condition, logical OR'd, and with white spaces in expression
  public void test7() {
    PathGenerator generator = GeneratorFactory.parse("random ( edge_coverage(100) and vertex_coverage (100) )");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(CombinedCondition.class));
  }

  @Test // 2 stop condition, logical OR'd, and with white spaces in expression
  public void test8() {
    PathGenerator generator = GeneratorFactory.parse("random ( reached_vertex(Some_vertex) or reached_edge ( Some_edge ) )");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(AlternativeCondition.class));

    AlternativeCondition condition = (AlternativeCondition) generator.getStopCondition();
    Assert.assertThat(condition.getStopConditions().get(0), instanceOf(ReachedVertex.class));
    Assert.assertThat(((ReachedVertex) condition.getStopConditions().get(0)).getValue(), is("Some_vertex"));
    Assert.assertThat(condition.getStopConditions().get(1), instanceOf(ReachedEdge.class));
    Assert.assertThat(((ReachedEdge) condition.getStopConditions().get(1)).getValue(), is("Some_edge"));
  }

  @Test // 2 stop condition, logical OR'd, and with white spaces in expression
  public void test9() {
    PathGenerator generator = GeneratorFactory.parse("random ( edge_coverage(100) or vertex_coverage (100) )");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(AlternativeCondition.class));
  }

  @Test // 2 stop condition, logical AND'd, and with white spaces in expression
  public void test10() {
    PathGenerator generator = GeneratorFactory.parse("random( edge_coverage(100) or time_duration(500) ) a_star(reached_vertex(v_ABC))");
    Assert.assertThat(generator, instanceOf(CombinedPath.class));

    CombinedPath combinedPath = (CombinedPath) generator;
    Assert.assertThat(combinedPath.getPathGenerators().get(0), instanceOf(RandomPath.class));
    Assert.assertThat(combinedPath.getPathGenerators().get(1), instanceOf(AStarPath.class));

    StopCondition condition = combinedPath.getPathGenerators().get(0).getStopCondition();
    Assert.assertThat(condition, instanceOf(StopCondition.class));
    AlternativeCondition alternativeCondition = (AlternativeCondition) condition;
    Assert.assertThat(alternativeCondition.getStopConditions().get(0), instanceOf(EdgeCoverage.class));
    Assert.assertThat(alternativeCondition.getStopConditions().get(1), instanceOf(TimeDuration.class));


    condition = combinedPath.getPathGenerators().get(1).getStopCondition();
    Assert.assertThat(condition, instanceOf(ReachedVertex.class));
    Assert.assertThat(((ReachedVertex) condition).getValue(), is("v_ABC"));
  }

  @Test // 2 stop condition, logical OR'd, and with white spaces in expression
  public void test11() {
    PathGenerator generator = GeneratorFactory.parse("RANDOM ( REACHED_VERTEX( Some_vertex) OR REACHED_EDGE( Some_edge ) )");
    Assert.assertThat(generator, instanceOf(RandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(AlternativeCondition.class));

    AlternativeCondition condition = (AlternativeCondition) generator.getStopCondition();
    Assert.assertThat(condition.getStopConditions().get(0), instanceOf(ReachedVertex.class));
    Assert.assertThat(((ReachedVertex) condition.getStopConditions().get(0)).getValue(), is("Some_vertex"));
    Assert.assertThat(condition.getStopConditions().get(1), instanceOf(ReachedEdge.class));
    Assert.assertThat(((ReachedEdge) condition.getStopConditions().get(1)).getValue(), is("Some_edge"));
  }

  @Test // Single stop condition using weighted random path generator
  public void test12() {
    PathGenerator generator = GeneratorFactory.parse("weighted_random(edge_coverage(100))");
    Assert.assertThat(generator, instanceOf(WeightedRandomPath.class));
    Assert.assertThat(generator.getStopCondition(), instanceOf(EdgeCoverage.class));
    Assert.assertThat(((EdgeCoverage) generator.getStopCondition()).getPercent(), is(100));
  }
  
  @Test // Single stop condition
  public void test13() {
	    PathGenerator generator = GeneratorFactory.parse("random(dependency_edge_coverage(80))");
	    Assert.assertThat(generator, instanceOf(RandomPath.class));
	    Assert.assertThat(generator.getStopCondition(), instanceOf(DependencyEdgeCoverage.class));
	    Assert.assertThat(((DependencyEdgeCoverage) generator.getStopCondition()).getDependency(), is(80));
	  }
}
