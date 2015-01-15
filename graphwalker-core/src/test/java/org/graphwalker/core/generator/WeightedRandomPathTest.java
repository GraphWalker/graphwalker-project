package org.graphwalker.core.generator;

/*
 * #%L
 * GraphWalker Core
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

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Kristian Karl
 */
public class WeightedRandomPathTest {

    private final Vertex source = new Vertex().setName("source");
    private final Vertex target = new Vertex().setName("target");
    private final Edge edge1 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.5).setName("edge1");
    private final Edge edge2 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.25).setName("edge2");
    private final Edge edge3 = new Edge().setSourceVertex(source).setTargetVertex(target).setWeight(0.15).setName("edge3");
    private final Edge edge4 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge4");
    private final Edge edge5 = new Edge().setSourceVertex(source).setTargetVertex(target).setName("edge5");
    private final Edge back2SourceEdge = new Edge().setSourceVertex(target).setTargetVertex(source).setName("back2SourceEdge");
    private final Model model = new Model()
            .addEdge(edge1)
            .addEdge(edge2)
            .addEdge(edge3)
            .addEdge(edge4)
            .addEdge(edge5)
            .addEdge(back2SourceEdge);

    @Test
    public void runWeightedRandom() {
        PathGenerator generator = new WeightedRandomPath(new EdgeCoverage(100));
        SimpleMachine machine = new SimpleMachine(new ExecutionContext(model, generator) {
        }.setCurrentElement(source.build()));

        while (machine.hasNextStep()) {
            System.out.println(machine.getCurrentContext().getCurrentElement().getName());
            machine.getNextStep();
        }
    }
}
