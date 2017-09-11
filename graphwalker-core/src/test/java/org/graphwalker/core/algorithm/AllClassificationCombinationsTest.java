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

import static org.junit.Assert.assertNotNull;

import org.graphwalker.core.model.Classification;
import org.graphwalker.core.model.ClassificationTree;
import org.junit.Test;

/**
 * @author Kristian Karl
 */
public class AllClassificationCombinationsTest {

  @Test
  public void simpleTree() throws Exception {
    Classification branchA = new Classification().setName("Branch A");
    branchA.addClassification(new Classification().setName("Leaf 1"));
    branchA.addClassification(new Classification().setName("Leaf 2"));
    branchA.addClassification(new Classification().setName("Leaf 3"));

    Classification branchB = new Classification().setName("Branch B");
    branchB.addClassification(new Classification().setName("Leaf 4"));
    branchB.addClassification(new Classification().setName("Leaf 5"));
    branchB.addClassification(new Classification().setName("Leaf 6"));

    Classification branchC = new Classification().setName("Branch C");
    branchC.addClassification(new Classification().setName("Leaf 7"));
    branchC.addClassification(new Classification().setName("Leaf 8"));
    branchC.addClassification(new Classification().setName("Leaf 9"));

    ClassificationTree tree = new ClassificationTree();
    tree.addClassification(branchA);
    tree.addClassification(branchB);
    tree.addClassification(branchC);

    AllClassificationCombinations allCombinations = new AllClassificationCombinations(tree.build());
    assertNotNull(allCombinations);

    allCombinations.generate();
  }

  @Test
  public void timeSheetEntry() throws Exception {
        /*
         * Populate tre according to the time sheet entry example at:
         * https://mattarcherblog.wordpress.com/2012/03/07/software-test-case-design-with-the-classification-tree-technique-sample-book-chapter/
         */
    Classification time = new Classification().setName("Time");
    Classification hours = new Classification().setName("Hours");
    Classification negativeHours = new Classification().setName("Negative hours");
    Classification zeroAndPositiveHours = new Classification().setName("Zero and positive hours");
    Classification minutes = new Classification().setName("Minutes");
    Classification lowerBoundry = new Classification().setName("Lower boundry");
    Classification justBelowLowerBoundry = new Classification().setName("Just below lower boundry");
    Classification justAboveLowerBoundry = new Classification().setName("Just above lower boundry");
    Classification upperBoundry = new Classification().setName("Upper boundry");
    Classification justBelowUpperBoundry = new Classification().setName("Just below upper boundry");
    Classification justAboveUpperBoundry = new Classification().setName("Just above upper boundry");
    Classification costCode = new Classification().setName("Cost code");
    Classification anyExistingCode = new Classification().setName("Any existing code");
    Classification anyNonExistentCode = new Classification().setName("Any non-existent code");

    ClassificationTree tree = new ClassificationTree();
    tree.addClassification(time);

    time.addClassification(hours);
    time.addClassification(minutes);

    minutes.addClassification(lowerBoundry);
    minutes.addClassification(upperBoundry);

    lowerBoundry.addClassification(justBelowLowerBoundry);
    lowerBoundry.addClassification(justAboveLowerBoundry);

    upperBoundry.addClassification(justBelowUpperBoundry);
    upperBoundry.addClassification(justAboveUpperBoundry);

    tree.addClassification(costCode);

    costCode.addClassification(anyExistingCode);
    costCode.addClassification(anyNonExistentCode);

    AllClassificationCombinations allCombinations = new AllClassificationCombinations(tree.build());
    assertNotNull(allCombinations);

    //List<List<Classification>> result = allCombinations.generate();
  }
}
