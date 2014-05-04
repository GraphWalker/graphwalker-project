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
    public void buildVertex() {
        Vertex vertex = new Vertex.Builder().build();
        Assert.assertThat(vertex, notNullValue());
    }

    @Test
    public void buildEdge() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        Edge edge = new Edge.Builder().setSource(vertex1).setTarget(vertex2).build();
        Assert.assertThat(edge, notNullValue());
        Assert.assertThat(edge.getSource(), notNullValue());
        Assert.assertThat(edge.getTarget(), notNullValue());
    }

    @Test
    public void buildEFSM() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        EFSM efsm = new EFSM.Builder()
                .add(new Edge.Builder().setSource(vertex1).setTarget(vertex2))
                .build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(1));
        Assert.assertThat(efsm.getVertices().size(), is(2));
    }


    @Test
    public void updateBuilder() {
        Vertex.Builder vertex1 = new Vertex.Builder();
        Vertex.Builder vertex2 = new Vertex.Builder();
        Edge.Builder edge1 = new Edge.Builder().setSource(vertex1).setTarget(vertex2);
        EFSM.Builder efsm = new EFSM.Builder().add(edge1);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(1));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
        Edge.Builder edge2 = new Edge.Builder().setSource(vertex1).setTarget(vertex2);
        efsm.add(edge2);
        Assert.assertThat(efsm.build(), notNullValue());
        Assert.assertThat(efsm.build().getEdges().size(), is(2));
        Assert.assertThat(efsm.build().getVertices().size(), is(2));
    }

    @Test
    public void singleVertex() {
        EFSM efsm = new EFSM.Builder().add(new Vertex.Builder()).build();
        Assert.assertThat(efsm, notNullValue());
        Assert.assertThat(efsm.getEdges().size(), is(0));
        Assert.assertThat(efsm.getVertices().size(), is(1));
    }
}
