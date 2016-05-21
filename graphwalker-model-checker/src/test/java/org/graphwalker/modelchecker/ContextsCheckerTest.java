package org.graphwalker.modelchecker;

import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
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
    List<Context> contexts = new JsonContextFactory().createMultiple(Paths.get("json/petClinic.json"));
    List<String> issues = ContextsChecker.hasIssues(contexts);
    Assert.assertThat(issues.size(), is(2));
  }
}
