package org.graphwalker.core.model;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class JsonTest {

    @Test
    public void convertModelTest() {
        Model model1 = new Model()
            .setName("MyModel")
            .setId("MyModelId")
            .addAction(new Action("MyActionScript"))
            .addRequirement(new Requirement("REQ123"))
            .setProperty("MyProperty", "valueA")
            .addEdge(new Edge()
                .setName("MyEdgeName")
                .setId("MyEdgeId")
                .setProperty("MyEdgeProperty", "valueX")
                .setSourceVertex(new Vertex()
                    .setName("MySourceVertex")
                    .setId("MySourceVertexId")
                    .setProperty("MySourceProperty", "valueY")
                )
                .setTargetVertex(new Vertex()
                    .setName("MyTargetVertex")
                    .setId("MyTargetVertexId")
                    .setProperty("MyTargetProperty", "valueZ")
                )
            );
        Gson gson = new Gson();
        String json1 = gson.toJson(model1);
        Model model2 = gson.fromJson(json1, Model.class);
        String json2 = gson.toJson(model2);
        Assert.assertEquals(json1, json2);
    }
}
