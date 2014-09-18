package org.graphwalker.java.test;

/*
 * #%L
 * GraphWalker Java
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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.java.annotation.GraphWalker;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class TestExecutorTest {

    @GraphWalker(start = "myStartElement")
    public class MultipleStartElements extends ExecutionContext {
        public MultipleStartElements() {
            Vertex vertex = new Vertex();
            Model model = new Model()
                    .addEdge(new Edge().setName("myStartElement").setSourceVertex(vertex).setTargetVertex(vertex))
                    .addEdge(new Edge().setName("myStartElement").setSourceVertex(vertex).setTargetVertex(vertex));
            setModel(model.build());

        }
    }

    @Test(expected = TestExecutionException.class)
    public void multipleStartElements() {
        Executor executor = new TestExecutor(new MultipleStartElements());
        executor.execute();
    }

    @GraphWalker(start = "myOnlyStartElement")
    public class SingleStartElements extends ExecutionContext {
        public SingleStartElements() {
            Vertex vertex = new Vertex().setName("myOnlyStartElement");
            Model model = new Model()
                    .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex))
                    .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
            setModel(model.build());
        }
    }

    @Test
    public void singleStartElements() {
        Executor executor = new TestExecutor(new SingleStartElements());
        executor.execute();
    }

    @GraphWalker(start = "nonExistingStartElement")
    public class NonExistingStartElement extends ExecutionContext {
        public NonExistingStartElement() {
            Vertex vertex = new Vertex();
            Model model = new Model()
                    .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex))
                    .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
            setModel(model.build());
        }
    }

    @Test(expected = TestExecutionException.class)
    public void nonExistingStartElement() {
        Executor executor = new TestExecutor(new NonExistingStartElement());
        executor.execute();
    }

}
