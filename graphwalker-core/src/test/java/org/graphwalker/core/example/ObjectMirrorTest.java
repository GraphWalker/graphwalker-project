package org.graphwalker.core.example;

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils Olsson
 */
public class ObjectMirrorTest extends ExecutionContext {

    private List<String> values = new ArrayList<>();

    public boolean contains(String value) {
        return values.contains(value);
    }

    public void add(String value) {
        values.add(value);
    }

    @Test
    public void verifyMirror() {
        Vertex start = new Vertex();
        Model model = new Model()
                .addEdge(new Edge().setName("edge1")
                        .setGuard(new Guard("contains('value1')"))
                        .setSourceVertex(start.setName("vertex1"))
                        .setTargetVertex(new Vertex().setName("vertex2")))
                .addEdge(new Edge()
                        .setGuard(new Guard("!contains('value1')"))
                        .setSourceVertex(start)
                        .setTargetVertex(start)
                        .addAction(new Action("add('value1')")));
        this.setModel(model.build());
        this.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        setNextElement(start);
        Machine machine = new SimpleMachine(this);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
    }
}
