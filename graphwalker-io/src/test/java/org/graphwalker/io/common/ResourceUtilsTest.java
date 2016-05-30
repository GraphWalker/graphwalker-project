package org.graphwalker.io.common;

/*
 * #%L
 * GraphWalker Input/Output
 * %%
 * Copyright (C) 2005 - 2015 GraphWalker
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

import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.json.JsonContextFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * Created by krikar on 2015-11-04.
 */
public class ResourceUtilsTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void getResourceAsFile_asFile() throws IOException {
    File file = ResourceUtils.getResourceAsFile(testFolder.newFile("getResourceAsFile_asFile").toString());
    Assert.assertNotNull(file);
    Assert.assertTrue(file.exists());
    Assert.assertTrue(file.canRead());
  }

  @Test
  public void getResourceAsFile_asResource() {
    File file = ResourceUtils.getResourceAsFile("json/example.json");
    Assert.assertNotNull(file);
    Assert.assertTrue(file.exists());
    Assert.assertTrue(file.canRead());
  }

  @Test(expected = ResourceNotFoundException.class)
  public void getResourceAsFileNotFound() {
    ResourceUtils.getResourceAsFile("json/kKJhdKJHJKhDGd.json");
  }

  @Test
  public void getResourceAsStream() {
    InputStream stream = ResourceUtils.getResourceAsStream("json/example.json");
    Assert.assertNotNull(stream);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void getResourceAsStreamNotFound() {
    ResourceUtils.getResourceAsStream("json/kKJhdKJHJKhDGd.json");
  }
}
