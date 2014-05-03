package org.graphwalker.core.model;

import org.graphwalker.core.model.efsm.Edge;
import org.graphwalker.core.model.efsm.Vertex;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Nils Olsson
 */
public class ModelBuilderTest {

    @Test
    public void build() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        EFSM efsm = new EFSM.Builder()
                .add(new Edge.Builder().source(vertex1).target(vertex2))
                .build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(1));
        Assert.assertThat(efsm.getVertices().size(), is(2));
    }
}
