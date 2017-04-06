package org.graphwalker.java.report;

/*
 * #%L
 * GraphWalker Java
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.graphwalker.java.test.Executor;
import org.graphwalker.java.test.TestExecutor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Nils Olsson
 */
public class ReportTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void writeReport() throws IOException {
    Date date = new Date();
    DateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
    Path reportName = Paths.get(testFolder.getRoot().toPath().toString(),
                                "TEST-GraphWalker-" + formatter.format(date) + ".xml");
    XMLReportGenerator xmlReportGenerator = new XMLReportGenerator(date, System.getProperties());

    List<Context> contexts = new JsonContextFactory().create(Paths.get("org/graphwalker/java/test/PetClinic.json"));
    Executor executor = new TestExecutor(contexts);
    xmlReportGenerator.writeReport(testFolder.getRoot().toPath().toFile(), executor);
    String xml = new String(Files.readAllBytes(reportName));
    assertThat(xml, hasXPath("/testsuites/@tests"));
    assertThat(xml, hasXPath("/testsuites/@failures"));
    assertThat(xml, hasXPath("/testsuites/@errors"));
  }
}
