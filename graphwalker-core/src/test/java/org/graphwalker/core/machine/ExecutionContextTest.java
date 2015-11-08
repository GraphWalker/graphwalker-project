package org.graphwalker.core.machine;

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-11-06.
 */
public class ExecutionContextTest {
    @Test
    public void resetContext() {
        Vertex vertex = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(new Vertex()));
        Context context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
        context.setNextElement(vertex);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
            Assert.assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
        }
        Assert.assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);

        context.reset();
        machine = new SimpleMachine(context);
        Assert.assertTrue(machine.hasNextStep());
    }
}
