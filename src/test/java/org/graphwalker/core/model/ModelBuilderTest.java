package org.graphwalker.core.model;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Nils Olsson
 */
public class ModelBuilderTest {

    @Test
    public void build() {
        EFSM efsm = new EFSM.Builder().build();
        Assert.assertThat(efsm, notNullValue());
    }
}
