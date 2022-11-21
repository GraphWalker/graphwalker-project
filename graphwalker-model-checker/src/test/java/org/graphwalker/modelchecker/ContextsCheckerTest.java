package org.graphwalker.modelchecker;

import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-11-08.
 */
public class ContextsCheckerTest {

  @Test
  public void testDefault() throws IOException {
    List<Context> contexts = new JsonContextFactory().create(Paths.get("json/petClinic.json"));
    List<String> issues = ContextsChecker.hasIssues(contexts);
    assertThat(issues.size(), is(0));
  }
}
