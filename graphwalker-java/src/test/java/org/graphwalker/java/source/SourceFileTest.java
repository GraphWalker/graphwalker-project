package org.graphwalker.java.source;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SourceFileTest {

  @Test
  public void createSourceFileWithModelName() throws IOException {
    SourceFile sourceFile = new SourceFile("MyModel", Paths.get("/model.json"));
    assertThat(sourceFile.getOutputPath(), is(Paths.get("/MyModel.java")));
    assertThat(sourceFile.getInputPath(), is(Paths.get("/model.json")));
    assertThat(sourceFile.getRelativePath(), is(Paths.get("model.json")));
    assertThat(sourceFile.getPackageName(), is(""));
    assertThat(sourceFile.getClassName(), is("MyModel"));
  }

  @Test
  public void createSourceFileWithOutputPath() throws IOException {
    SourceFile sourceFile = new SourceFile(Paths.get("/model.json"), Paths.get("/"), Paths.get("/output"));
    assertThat(sourceFile.getOutputPath(), is(Paths.get("/output/model.java")));
    assertThat(sourceFile.getInputPath(), is(Paths.get("/model.json")));
    assertThat(sourceFile.getRelativePath(), is(Paths.get("model.json")));
    assertThat(sourceFile.getPackageName(), is(""));
    assertThat(sourceFile.getClassName(), is("model"));
  }

  @Test
  public void createSourceFileWithModelNameAndOutputPath() throws IOException {
    SourceFile sourceFile = new SourceFile("MyModel", Paths.get("/model.json"), Paths.get("/"), Paths.get("/output"));
    assertThat(sourceFile.getOutputPath(), is(Paths.get("/output/MyModel.java")));
    assertThat(sourceFile.getInputPath(), is(Paths.get("/model.json")));
    assertThat(sourceFile.getRelativePath(), is(Paths.get("model.json")));
    assertThat(sourceFile.getPackageName(), is(""));
    assertThat(sourceFile.getClassName(), is("MyModel"));
  }

  @Test
  public void createSourceFileWithNestedFolders() throws IOException {
    SourceFile sourceFile = new SourceFile("MyModel", Paths.get("/company/path/model.json"), Paths.get("/"), Paths.get("/output"));
    assertThat(sourceFile.getOutputPath(), is(Paths.get("/output/company/path/MyModel.java")));
    assertThat(sourceFile.getInputPath(), is(Paths.get("/company/path/model.json")));
    assertThat(sourceFile.getRelativePath(), is(Paths.get("company/path/model.json")));
    assertThat(sourceFile.getPackageName(), is("company.path"));
    assertThat(sourceFile.getClassName(), is("MyModel"));
  }
}
