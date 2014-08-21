package org.graphwalker.core.condition;

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class InternalStateTest {

    @Test
    public void testIsFulfilled() {
        Vertex vertex = new Vertex();
        Model model = new Model()
                .addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex).addAction(new Action("index++")))
                .addAction(new Action("var index = 0"));
        StopCondition stopCondition = new InternalState("index == 99");
        ExecutionContext context = new ExecutionContext(model, new RandomPath(stopCondition)).setCurrentElement(vertex.build());
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            Assert.assertThat(stopCondition.getFulfilment(context), is(0.0));
            Assert.assertFalse(stopCondition.isFulfilled(context));
            machine.getNextStep();
        }
        Assert.assertThat(stopCondition.getFulfilment(context), is(1.0));
        Assert.assertTrue(stopCondition.isFulfilled(context));
        Assert.assertThat(context.getKeys().get("index"), is("99"));
    }
}
