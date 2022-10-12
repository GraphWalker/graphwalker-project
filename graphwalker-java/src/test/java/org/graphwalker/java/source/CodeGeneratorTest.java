package org.graphwalker.java.source;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CodeGeneratorTest {

@Test
public void testOnlyRegenerateWhenSourceIsModified() throws IOException, URISyntaxException {
    String tmpdir = System.getProperty("java.io.tmpdir");
    Path inputDir = Files.createTempDirectory(Paths.get(tmpdir), "input").toAbsolutePath();
    Path outputDir = Files.createTempDirectory(Paths.get(tmpdir), "output").toAbsolutePath();

    // sample file
    URL resource = getClass().getClassLoader().getResource("org/graphwalker/java/path with space/MyModel.graphml");
    File testFile = new File(resource.toURI());

    Files.copy(testFile.toPath(), inputDir.resolve(testFile.getName()), StandardCopyOption.REPLACE_EXISTING);
    FileTime sourceLastModified = Files.getLastModifiedTime(inputDir.resolve(testFile.getName()));

    File[] inputFiles = Arrays.stream(inputDir.toFile().listFiles())
      .toArray(File[]::new);

    assertEquals(1, inputFiles.length);

    // run codegenerator  for each input files in inputDir
    CodeGenerator.generate(inputDir, outputDir);

    // source modified time should not be altered
    FileTime sourceLastModifiedDelta = Files.getLastModifiedTime(inputDir.resolve(testFile.getName()));
    assertEquals(sourceLastModified.toMillis(), sourceLastModifiedDelta.toMillis());

    // glob the output files, find the one with the same name as the input file
    File[] outputFiles = Arrays.stream(outputDir.toFile().listFiles())
      .filter(f -> f.getName().equals(testFile.getName().replace(".graphml", ".java")))
      .toArray(File[]::new);

    assertEquals(1, outputFiles.length);
    assertEquals("MyModel.java", outputFiles[0].getName());

    // note lst modified time is in milliseconds
    long lastModified = outputFiles[0].lastModified();

    // sleep 5 seconds to make sure the last modified time is different
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // regenerate the file
    CodeGenerator.generate(inputDir, outputDir);

    // check that the file was not regenerated
    outputFiles = Arrays.stream(outputDir.toFile().listFiles())
      .filter(f -> f.getName().equals(testFile.getName().replace(".graphml", ".java")))
      .toArray(File[]::new);
    assertEquals(1, outputFiles.length);
    assertEquals(lastModified, outputFiles[0].lastModified());
  }
}
