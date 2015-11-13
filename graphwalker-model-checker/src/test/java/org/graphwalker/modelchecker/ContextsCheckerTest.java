package org.graphwalker.modelchecker;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.io.factory.gw3.GW3ContextFactory;
import org.graphwalker.io.factory.json.JsonContext;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-11-08.
 */
public class ContextsCheckerTest {
    @Test
    public void testDefault() {
        List<Context> contexts = new GW3ContextFactory().createMultiple(Paths.get("gw3/petClinic.gw3"));
        List<String> issues = ContextsChecker.hasIssues(contexts);
        Assert.assertThat(issues.size(), is(2));
    }
}
