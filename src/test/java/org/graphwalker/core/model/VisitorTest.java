package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
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

import org.junit.Assert;
import org.junit.Test;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Model.RuntimeModel;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;
import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class VisitorTest {

    RuntimeModel model = new Model().addEdge(
        new Edge()
            .setName("edge1")
            .setSourceVertex(new Vertex()
                    .setName("vertex1"))
            .setTargetVertex(new Vertex()
                    .setName("vertex2")))
        .build();

    @Test
    public void visitVertex() {
        new Vertex().setName("vertex").build().accept(new MyVertexVisitor());
    }

    @Test
    public void visitVertices() {
        MyNamedVertexCounter visitor = new MyNamedVertexCounter();
        model.accept(visitor);
        Assert.assertThat(visitor.count, is(2));
    }

    @Test
    public void visitEdge() {
        new Edge().setName("edge1").setSourceVertex(new Vertex()).setTargetVertex(new Vertex()).build().accept(new MyEdgeVisitor());
    }

    @Test
    public void visitEdges() {
        Vertex startVertex = new Vertex().setName("start");
        Vertex endVertex = new Vertex().setName("end");
        RuntimeModel pseudograph = new Model()
                .addEdge(new Edge().setSourceVertex(startVertex).setTargetVertex(endVertex))
                .addEdge(new Edge().setSourceVertex(endVertex).setTargetVertex(endVertex))
                .build();
        MyLoopEdgeFinder visitor = new MyLoopEdgeFinder();
        pseudograph.accept(visitor);
        Assert.assertThat(visitor.count, is(1));
    }

    @Test
    public void visitTree() {

    }

    private class MyVertexVisitor implements ElementVisitor<RuntimeVertex> {

        @Override
        public void visit(RuntimeVertex element) {
            System.out.println(element.getName());
        }
    }

    private class MyNamedVertexCounter implements ElementVisitor<Element> {

        int count = 0;

        @Override
        public void visit(Element element) {
            if (element instanceof RuntimeModel) {
                RuntimeModel model = (RuntimeModel)element;
                // We don't need to visit() all edges to count the vertices with names, it's just PoC (we could just loop over them)
                for (Element childElement: model.getElements()) {
                    childElement.accept(this);
                }
            } else if (element instanceof RuntimeVertex && element.hasName()) {
                count++;
            }
        }
    }

    private class MyLoopEdgeFinder implements ElementVisitor<RuntimeModel> {

        int count = 0;

        @Override
        public void visit(RuntimeModel model) {
            for (RuntimeEdge edge: model.getEdges()) {
                if (edge.getSourceVertex().equals(edge.getTargetVertex())) {
                    count++;
                }
            }
        }
    }

    private class MyEdgeVisitor implements ElementVisitor<RuntimeEdge> {

        @Override
        public void visit(RuntimeEdge element) {
            System.out.println(element.getName());
        }
    }

    private class MyTreeVisitor implements ElementVisitor<Element> {

        @Override
        public void visit(Element element) {
            if (element instanceof RuntimeVertex) {
                visit((RuntimeVertex)element);
            } else if (element instanceof RuntimeEdge) {
                visit((RuntimeEdge)element);
            }
        }

        public void visit(RuntimeVertex element) {
            System.out.println("Vertex: "+element.getName());
        }

        public void visit(RuntimeEdge element) {
            System.out.println("Edge: "+element.getName());
        }
    }
}
