package org.graphwalker.modelchecker;

import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by krikar on 2015-11-08.
 */
public class ContextsCheckerTest {

  @Test
  public void testDefault() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/petClinic.json"));
    List<String> issues = ContextsChecker.hasIssues(contexts);
    Assert.assertThat(issues.size(), is(0));
  }
}
