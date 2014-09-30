package org.graphwalker.java.test;

import org.graphwalker.java.annotation.Edge;
import org.graphwalker.java.annotation.Model;

/**
 * @author Nils Olsson
 */
@Model(file = "org/graphwalker/java/test/SimpleModel.graphml")
public interface SimpleModel {

    @Edge()
    void vertex1();

    @Edge()
    void vertex2();

    @Edge()
    void edge1();
}