package org.graphwalker.core.algorithm;

import java.util.*;

import org.graphwalker.core.machine.Context;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Path;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Vertex;

import java.util.HashMap;
import java.util.Map;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * Directed Chinese Postman Problem
 * Algorithm based on: Edmonds, J., Johnson, E.L. Matching, Euler tours and the Chinese postman, Mathematical
 * Programming (1973) 5: 88. doi:10.1007/BF01580113
 * @author Onur Enginer
 */
public class ChinesePostmanProblem implements Algorithm {

  private final Context context;
  private final Map<RuntimeVertex, PolarityCounter> polarities;
  private final Model model;
  RuntimeVertex neg[], pos[]; // unbalanced vertices

  public ChinesePostmanProblem(Context context) {
    this.context = context;
    int N = context.getModel().getVertices().size();
    this.polarities = new HashMap<>(N);
    this.model = new Model(context.getModel());
    polarize();
  }

  public enum EulerianType {
    EULERIAN, SEMI_EULERIAN, NOT_EULERIAN
  }

  private void polarize() {
    for (RuntimeEdge edge : context.getModel().getEdges()) {
      getPolarityCounter(edge.getSourceVertex()).decrease();
      getPolarityCounter(edge.getTargetVertex()).increase();
    }
    for (RuntimeVertex vertex : context.getModel().getVertices()) {
      if (!polarities.get(vertex).hasPolarity()) {
        polarities.remove(vertex);
      }
    }
  }

  private PolarityCounter getPolarityCounter(RuntimeVertex vertex) {
    if (!polarities.containsKey(vertex)) {
      polarities.put(vertex, new PolarityCounter());
    }
    return polarities.get(vertex);
  }

  public Path<Element> getEulerPath(Element element) {
    Eulerize();
    return context.getAlgorithm(Fleury.class).getTrail(element);
  }

  public void Eulerize() {
    List<Edge> newEdges = new ArrayList<Edge>();
    int matrixSize = getMatrixSize();
    int[][] costMatrix = new int[matrixSize][matrixSize];
    VertexPair[][] pairMatrix = new VertexPair[matrixSize][matrixSize];
    findUnbalanced();
    Map<VertexPair, Path<Element>> shortestPaths = new HashMap<VertexPair, Path<Element>>();
    for (int i = 0; i < pos.length; i++) {
      RuntimeVertex p = pos[i];
      for (int j = 0; j < neg.length; j++) {
        RuntimeVertex n = neg[j];
        VertexPair newPair = new VertexPair(p, n);
        pairMatrix[i][j] = newPair;
        Path<Element> shortestPath = context.getAlgorithm(AStar.class).getShortestPath(p, n);
        shortestPaths.put(newPair, shortestPath);
      }
    }
    RuntimeVertex[] negPartition = new RuntimeVertex[matrixSize];
    RuntimeVertex[] posPartition = new RuntimeVertex[matrixSize];

    for (RuntimeVertex v : neg) {
      int index = 0;
      for (int i = 0; i < -polarities.get(v).polarity; i++) {
        negPartition[index++] = v;
      }
    }
    for (RuntimeVertex v : pos) {
      int index = 0;
      for (int i = 0; i < polarities.get(v).polarity; i++) {
        posPartition[index++] = v;
      }
    }
    for (int i = 0; i < matrixSize; i++) {
      for (int j = 0; j < matrixSize; j++) {
        int cost = (shortestPaths.get(pairMatrix[i][j]).size() - 1) / 2;
        costMatrix[i][j] = cost;
      }
    }

    HungarianMatchingAlgorithm ha = new HungarianMatchingAlgorithm(costMatrix);
    int[][] assignment = ha.findOptimalAssignment();

    List<Path<Element>> newPaths = new ArrayList<Path<Element>>();

    for (int i = 0; i < matrixSize; i++) {
      int posIndex = assignment[i][0];
      int negIndex = assignment[i][1];
      VertexPair vp = pairMatrix[posIndex][negIndex];
      newPaths.add(shortestPaths.get(vp));
    }

    for (Path<Element> path : newPaths) {
      newEdges.addAll(getEdges(path));
    }

    for (Edge edge : newEdges) {
      model.addEdge(edge);
    }
    
    context.setModel(model.build());
  }

  private int getMatrixSize() {
    int size = 0;

    for (PolarityCounter pc : polarities.values()) {
      size += Math.abs(pc.polarity);
    }
    return size / 2;
  }

  private Vertex getVertex(RuntimeVertex rv) {
    for (Vertex v : model.getVertices()) {
      if (v.getId().equals(rv.getId()))
        return v;
    }
    return null;
  }

  private List<Edge> getEdges(Path<Element> p) {
    List<Edge> edgeList = new ArrayList<Edge>();
    Object[] elementList = p.toArray();
    for (int i = 0; i < p.size(); i++) {
      if (i % 2 == 0)
        continue;

      RuntimeEdge e = (RuntimeEdge) elementList[i];
      Edge edge = new Edge();
      edge.setId(e.getId() + edge.hashCode());
      edge.setName(e.getName());
      edge.setSourceVertex(getVertex(e.getSourceVertex()));
      edge.setTargetVertex(getVertex(e.getTargetVertex()));
      edge.setGuard(e.getGuard());
      edge.setActions(e.getActions());
      edge.setRequirements(e.getRequirements());
      edge.setWeight(e.getWeight());
      edge.setProperties(e.getProperties());
      edgeList.add(edge);
    }
    return edgeList;
  }

  void findUnbalanced() {
    int nn = 0, np = 0;
    for (RuntimeVertex v : polarities.keySet()) {
      if (polarities.get(v).polarity < 0)
        nn++;
      else if (polarities.get(v).polarity > 0)
        np++;
    }
    neg = new RuntimeVertex[nn];
    pos = new RuntimeVertex[np];
    nn = np = 0;
    for (RuntimeVertex v : polarities.keySet()) { 
      if (polarities.get(v).polarity < 0)
        neg[nn++] = v;
      else if (polarities.get(v).polarity > 0)
        pos[np++] = v;
    }
  }

  class PolarityCounter {

    private int polarity = 0;

    public void increase() {
      polarity += 1;
    }

    public void decrease() {
      polarity -= 1;
    }

    public boolean hasPolarity() {
      return 0 != getPolarity();
    }

    public int getPolarity() {
      return polarity;
    }
  }

/*
Hungarian Matching Algorithm: https://en.wikipedia.org/wiki/Hungarian_algorithm

MIT License

Copyright (c) 2018 aalmi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
  public class HungarianMatchingAlgorithm {

    int[][] matrix; 
    int[] squareInRow, squareInCol, isRowCovered, isColCovered, starredZeroesInRow;

    public HungarianMatchingAlgorithm(int[][] matrix) {

      this.matrix = matrix;
      squareInRow = new int[matrix.length]; 
      squareInCol = new int[matrix[0].length]; 

      isRowCovered = new int[matrix.length]; 
      isColCovered = new int[matrix[0].length]; 
      starredZeroesInRow = new int[matrix.length]; 
      Arrays.fill(starredZeroesInRow, -1);
      Arrays.fill(squareInRow, -1);
      Arrays.fill(squareInCol, -1);
    }

    /**
     * @return Optimal matching
     */
    public int[][] findOptimalAssignment() {
      reduceMatrix(); 
      markSquares(); 
      coverCols(); 

      while (!allColumnsAreCovered()) {
        int[] mainZero = markZeroes();
        while (mainZero == null) { 
          findMinUncovered();
          mainZero = markZeroes();
        }
        if (squareInRow[mainZero[0]] == -1) {
          createSquareZeroChain(mainZero);
          coverCols(); 
        } else {
          isRowCovered[mainZero[0]] = 1; 
          isColCovered[squareInRow[mainZero[0]]] = 0; 
          findMinUncovered();
        }
      }

      int[][] optimalAssignment = new int[matrix.length][];
      for (int i = 0; i < squareInCol.length; i++) {
        optimalAssignment[i] = new int[] { i, squareInCol[i] };
      }
      return optimalAssignment;
    }

    private boolean allColumnsAreCovered() {
      for (int i : isColCovered) {
        if (i == 0) {
          return false;
        }
      }
      return true;
    }

    private void reduceMatrix() {
      for (int i = 0; i < matrix.length; i++) {
        int currentRowMin = Integer.MAX_VALUE;
        for (int j = 0; j < matrix[i].length; j++) {
          if (matrix[i][j] < currentRowMin) {
            currentRowMin = matrix[i][j];
          }
        }
        for (int k = 0; k < matrix[i].length; k++) {
          matrix[i][k] -= currentRowMin;
        }
      }

      for (int i = 0; i < matrix[0].length; i++) {
        int currentColMin = Integer.MAX_VALUE;
        for (int j = 0; j < matrix.length; j++) {
          if (matrix[j][i] < currentColMin) {
            currentColMin = matrix[j][i];
          }
        }
        for (int k = 0; k < matrix.length; k++) {
          matrix[k][i] -= currentColMin;
        }
      }
    }

    private void markSquares() {
      int[] rowHasSquare = new int[matrix.length];
      int[] colHasSquare = new int[matrix[0].length];

      for (int i = 0; i < matrix.length; i++) {
        for (int j = 0; j < matrix.length; j++) {
          if (matrix[i][j] == 0 && rowHasSquare[i] == 0 && colHasSquare[j] == 0) {
            rowHasSquare[i] = 1;
            colHasSquare[j] = 1;
            squareInRow[i] = j; 
            squareInCol[j] = i; 
            continue; 
          }
        }
      }
    }


    private void coverCols() {
      for (int i = 0; i < squareInCol.length; i++) {
        isColCovered[i] = squareInCol[i] != -1 ? 1 : 0;
      }
    }

    private void findMinUncovered() {
      int minUncoveredValue = Integer.MAX_VALUE;
      for (int i = 0; i < matrix.length; i++) {
        if (isRowCovered[i] == 1) {
          continue;
        }
        for (int j = 0; j < matrix[0].length; j++) {
          if (isColCovered[j] == 0 && matrix[i][j] < minUncoveredValue) {
            minUncoveredValue = matrix[i][j];
          }
        }
      }

      if (minUncoveredValue > 0) {
        for (int i = 0; i < matrix.length; i++) {
          for (int j = 0; j < matrix[0].length; j++) {
            if (isRowCovered[i] == 1 && isColCovered[j] == 1) {
              matrix[i][j] += minUncoveredValue;
            } else if (isRowCovered[i] == 0 && isColCovered[j] == 0) {
              matrix[i][j] -= minUncoveredValue;
            }
          }
        }
      }
    }


    private int[] markZeroes() {
      for (int i = 0; i < matrix.length; i++) {
        if (isRowCovered[i] == 0) {
          for (int j = 0; j < matrix[i].length; j++) {
            if (matrix[i][j] == 0 && isColCovered[j] == 0) {
              starredZeroesInRow[i] = j; 
              return new int[] { i, j };
            }
          }
        }
      }
      return null;
    }

    private void createSquareZeroChain(int[] mainZero) {
      int i = mainZero[0];
      int j = mainZero[1];

      Set<int[]> chain = new LinkedHashSet<>();

      chain.add(mainZero);
      boolean found = false;
      do {
        if (squareInCol[j] != -1) {
          chain.add(new int[] { squareInCol[j], j });
          found = true;
        } else {
          found = false;
        }
        if (!found) {
          break;
        }

        i = squareInCol[j];
        j = starredZeroesInRow[i];

        if (j != -1) {
          chain.add(new int[] { i, j });
          found = true;
        } else {
          found = false;
        }

      } while (found);

      for (int[] zero : chain) {
        if (squareInCol[zero[1]] == zero[0]) {
          squareInCol[zero[1]] = -1;
          squareInRow[zero[0]] = -1;
        }
        if (starredZeroesInRow[zero[0]] == zero[1]) {
          squareInRow[zero[0]] = zero[1];
          squareInCol[zero[1]] = zero[0];
        }
      }

      Arrays.fill(starredZeroesInRow, -1);
      Arrays.fill(isRowCovered, 0);
      Arrays.fill(isColCovered, 0);
    }
  }

  public class VertexPair {
    RuntimeVertex first;
    RuntimeVertex second;

    public VertexPair(RuntimeVertex first, RuntimeVertex second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public boolean equals(Object o) {
      VertexPair vp = (VertexPair) o;
      return (this.first.equals(vp.first) && this.second.equals(vp.second));
    }
  }
}
