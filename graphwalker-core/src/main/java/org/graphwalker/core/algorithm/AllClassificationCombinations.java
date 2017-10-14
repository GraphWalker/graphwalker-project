package org.graphwalker.core.algorithm;

/*
 * #%L
 * GraphWalker Core
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

import org.graphwalker.core.model.Classification;
import org.graphwalker.core.model.ClassificationTree;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>All combinations from given classification tree</h1>
 * The algorithm will generate all possible combinations given the classification tree.
 * </p>
 *
 * @author Kristian karl
 */
public class AllClassificationCombinations implements Algorithm {

  private final ClassificationTree.RuntimeClassificationTree tree;
  private static List<List<Classification.RuntimeClassification>> combinations;

  public AllClassificationCombinations(ClassificationTree.RuntimeClassificationTree tree) {
    this.tree = tree;
  }

  /**
   * Returns list of bottom level classes
   */
  public List<Classification.RuntimeClassification> getClassifications() {
    List<Classification.RuntimeClassification> result = new ArrayList<>();
    getClassifications(result, tree.getRoot());
    return result;
  }

  private void getClassifications(List<Classification.RuntimeClassification> result,
                                  Classification.RuntimeClassification node) {
    List<Classification.RuntimeClassification> children = node.getClassifications();
    for (Classification.RuntimeClassification child : children) {
      if (child.getClassifications().size() > 0) {
        getClassifications(result, child);
      } else {
        if (!result.contains(node)) {
          result.add(node);
        }
      }
    }
  }


  /*
   * Will generate all combinations by the given classification tree.
   * The result is returned as list, where each row represent the combination.
   */
  public List<List<Classification.RuntimeClassification>> generate() {
    List<Classification.RuntimeClassification> classes = getClassifications();
    List<List<Classification.RuntimeClassification>> treeData = new ArrayList<>();
    for (Classification.RuntimeClassification cls : classes) {
      treeData.add(cls.getClassifications());
    }

    combinations = new ArrayList<>();
    getCombinations(treeData, 0, new ArrayList<>());
    return combinations;
  }

  private static void getCombinations(List<List<Classification.RuntimeClassification>> treeData,
                                      int n,
                                      ArrayList<Classification.RuntimeClassification> testCase) {
    if (n >= treeData.size()) {
      combinations.add(testCase);
      return;
    }
    for (Classification.RuntimeClassification c : treeData.get(n)) {
      ArrayList<Classification.RuntimeClassification> foo = new ArrayList<>(testCase);
      foo.add(c);
      getCombinations(treeData, n + 1, foo);
    }
  }
}
