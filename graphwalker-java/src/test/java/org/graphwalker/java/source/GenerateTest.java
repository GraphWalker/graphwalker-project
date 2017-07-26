package org.graphwalker.java.source;

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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.graphwalker.java.utils.OccurrencesOfString.occurrencesOfString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class GenerateTest {

  @Test
  public void generate() throws IOException {
    List<String> sources = new CodeGenerator().generate("/org/graphwalker/java/annotation/MyModel.graphml");
    Assert.assertThat(sources.size(), is(1));
    Assert.assertThat(sources.get(0), containsString("edge12"));
    Assert.assertThat(sources.get(0), containsString("vertex2"));
    Assert.assertThat(sources.get(0), not(containsString("SHARED")));
  }

  @Test
  public void generatePathWithSpace() throws IOException {
    List<String> sources = new CodeGenerator().generate("/org/graphwalker/java/path with space/MyModel.graphml");
    Assert.assertThat(sources.size(), is(1));
    Assert.assertThat(sources.get(0), containsString("edge12"));
    Assert.assertThat(sources.get(0), containsString("vertex2"));
    Assert.assertThat(sources.get(0), not(containsString("SHARED")));
    Assert.assertThat(sources.get(0), containsString("package org.graphwalker.java.path_with_space;"));
  }

  @Test
  public void generateMultiModelFile() throws IOException {
    List<String> sources = new CodeGenerator().generate("/org/graphwalker/java/test/PetClinic.json");
    Assert.assertThat(sources.size(), is(5));

    Assert.assertThat(sources.get(0), containsString("public interface FindOwners {"));
    Assert.assertThat(sources.get(0), occurrencesOfString("@Vertex()", 3));
    Assert.assertThat(sources.get(0), occurrencesOfString("@Edge()", 3));
    Assert.assertThat(sources.get(0), stringContainsInOrder(Arrays.asList(
      "void e_AddOwner();",
      "void v_FindOwners();",
      "void e_Search();",
      "void e_FindOwners();",
      "void v_NewOwner();"
    )));

    Assert.assertThat(sources.get(1), containsString("public interface NewOwner {"));
    Assert.assertThat(sources.get(1), occurrencesOfString("@Vertex()", 3));
    Assert.assertThat(sources.get(1), occurrencesOfString("@Edge()", 2));
    Assert.assertThat(sources.get(1), stringContainsInOrder(Arrays.asList(
      "void v_OwnerInformation();",
      "void e_CorrectData();",
      "void e_IncorrectData();",
      "void v_IncorrectData();",
      "void v_NewOwner();"
    )));

    Assert.assertThat(sources.get(2), containsString("public interface OwnerInformation {"));
    Assert.assertThat(sources.get(2), occurrencesOfString("@Vertex()", 5));
    Assert.assertThat(sources.get(2), occurrencesOfString("@Edge()", 9));
    Assert.assertThat(sources.get(2), stringContainsInOrder(Arrays.asList(
      "void v_OwnerInformation();",
      "void e_UpdatePet();",
      "void v_FindOwners();",
      "void e_EditPet();",
      "void e_AddNewPet();",
      "void e_AddVisit();",
      "void e_FindOwners();",
      "void e_AddPetSuccessfully();",
      "void v_NewPet();",
      "void e_VisitAddedSuccessfully();",
      "void v_NewVisit();",
      "void v_Pet();",
      "void e_AddPetFailed();",
      "void e_VisitAddedFailed();"
    )));

    Assert.assertThat(sources.get(3), containsString("public interface PetClinic {"));
    Assert.assertThat(sources.get(3), occurrencesOfString("@Vertex()", 3));
    Assert.assertThat(sources.get(3), occurrencesOfString("@Edge()", 4));
    Assert.assertThat(sources.get(3), stringContainsInOrder(Arrays.asList(
      "void v_FindOwners();",
      "void e_HomePage();",
      "void e_StartBrowser();",
      "void e_Veterinarians();",
      "void e_FindOwners();",
      "void v_HomePage();"
    )));

    Assert.assertThat(sources.get(4), containsString("public interface Veterinarians {"));
    Assert.assertThat(sources.get(4), occurrencesOfString("@Vertex()", 2));
    Assert.assertThat(sources.get(4), occurrencesOfString("@Edge()", 1));
    Assert.assertThat(sources.get(4), stringContainsInOrder(Arrays.asList(
      "void e_Search();",
      "void v_SearchResult();",
      "void v_Veterinarians();"
    )));
  }
}
