---
layout: default
title: Generators
nav_order: 5
---

# Path Generators

A generator is the algorithm that decides which edge to take at each step of the graph traversal. Each generator (except `new_york_street_sweeper`) is paired with a [stop condition](stop-conditions) that determines when to stop.

## DSL syntax

Generators are specified as strings in the format:

```
generator_name(stop_condition)
```

Generator names are case-insensitive. Both `snake_case` and `camelCase` forms are accepted. Multiple generators can be chained by separating them with whitespace.

---

## random

Randomly walks the graph by selecting uniformly from available outgoing edges at each vertex.

**Syntax:** `random(condition)` or `random_path(condition)` or `randompath(condition)`

**Algorithm:**
1. At a vertex: collect all outgoing edges that pass their guards. Pick one uniformly at random.
2. At an edge: move to the target vertex.

**Characteristics:**
- Simple and fast
- No bias toward unvisited elements
- May revisit the same areas many times before reaching full coverage
- Good for smoke testing and exploring common paths

**Example:**
```
random(edge_coverage(100))
random(length(200))
random(vertex_coverage(80) or time_duration(60))
```

---

## quick_random

A smarter random walk that steers toward unvisited areas using shortest-path computation.

**Syntax:** `quick_random(condition)` or `quick_random_path(condition)` or `quickrandompath(condition)`

**Algorithm:**
1. On first step: collect all elements in the model, shuffle them.
2. Maintain a target element. If the target is reached or not yet set, pick the least-visited element from the shuffled list.
3. Use Floyd-Warshall shortest-path distances and A* navigation to move toward the target.
4. Remove visited elements from the target list.

**Characteristics:**
- Significantly faster at achieving full coverage than pure random
- Biased toward unvisited elements
- Uses pre-computed shortest paths (Floyd-Warshall) for efficient navigation
- Good default choice for coverage-based testing

**Example:**
```
quick_random(edge_coverage(100))
quick_random(vertex_coverage(100))
```

---

## weighted_random

Random walk where edges have weights that control selection probability.

**Syntax:** `weighted_random(condition)` or `weighted_random_path(condition)` or `weightedrandompath(condition)`

**Algorithm:**
1. At a vertex: collect available outgoing edges with their weights.
2. Edges with weight > 0 are selected with probability proportional to their weight.
3. Edges with weight 0 share the remaining probability equally.
4. At an edge: select uniformly from available next elements.

**Weight rules:**
- Weights are defined on edges in the model (the `weight` field, values 0.0 to 1.0)
- The sum of all outgoing edge weights from a vertex must not exceed 1.0
- Edges with weight 0 get equal shares of `(1.0 - sum_of_weights)`
- If all weights are 0, all edges are equally likely

**Example:**
If a vertex has three outgoing edges with weights 0.7, 0.2, and 0:
- Edge 1 is selected 70% of the time
- Edge 2 is selected 20% of the time
- Edge 3 is selected 10% of the time (the remaining probability)

```
weighted_random(edge_coverage(100))
weighted_random(length(500))
```

---

## a_star

Directed pathfinding that navigates toward a specific target element using the A* algorithm.

**Syntax:** `a_star(condition)` or `astar(condition)` or `astarpath(condition)`

**Algorithm:**
1. Extract target elements from the stop condition (works with `reached_vertex`, `reached_edge`, `reached_shared_state`).
2. Use Floyd-Warshall pre-computed distances to find the nearest target.
3. At each step, take the next step on the shortest path toward that target.

**Characteristics:**
- Deterministic shortest path to the target
- Requires a stop condition that defines target elements
- Ideal for "navigate to a specific state" scenarios
- Often used in combined generators: random exploration followed by directed navigation

**Example:**
```
a_star(reached_vertex(v_Checkout))
a_star(reached_edge(e_SubmitOrder))
a_star(reached_shared_state(LOGGED_IN))
```

---

## shortest_all_paths

Computes an Eulerian path that traverses every edge exactly once (if the graph permits), then follows it deterministically.

**Syntax:** `shortest_all_paths(condition)` or `shortestallpaths(condition)`

**Algorithm:**
1. On first step: compute an Eulerian trail from the current position.
2. Follow the pre-computed trail step by step.

**Characteristics:**
- Deterministic: same graph always produces the same path
- Visits every edge exactly once (if the graph is Eulerian or semi-Eulerian)
- Fails if the graph does not have an Eulerian path
- Efficient: computes the path once, then just follows it

**Requirements:**
- The graph must be connected
- The graph must be Eulerian (all vertices have equal in-degree and out-degree) or semi-Eulerian (exactly two vertices have unequal degrees)

**Example:**
```
shortest_all_paths(edge_coverage(100))
```

---

## predefined_path

Follows a predetermined sequence of edges defined in the model.

**Syntax:** `predefined_path(condition)` or `predefinedpath(condition)`

**Algorithm:**
1. Read the `predefinedPathEdgeIds` array from the model.
2. At each vertex: take the next edge from the predefined sequence.
3. At each edge: move to the target vertex.
4. Stop when the sequence is exhausted.

**Characteristics:**
- Fully deterministic: the path is defined in the model
- No randomness at all
- Useful for regression testing specific known paths
- Fails if the next predefined edge is not available from the current vertex

**Example:**
```
predefined_path(predefined_path)
predefined_path(length(20))
```

---

## new_york_street_sweeper

Computes the optimal route that visits every edge at least once (the Directed Chinese Postman Problem) and follows it.

**Syntax:** `new_york_street_sweeper()` or `newyorkstreetsweeper()`

This generator takes **no stop condition** and **no parameters**. It terminates when the computed path is exhausted.

**Algorithm:**
1. Check that the graph is strongly connected.
2. Compute vertex polarities (out-degree minus in-degree).
3. If all polarities are zero, the graph is Eulerian &mdash; compute the Euler circuit directly.
4. Otherwise, find the minimum-cost set of edges to duplicate using BFS shortest paths and the Hungarian algorithm (min-cost bipartite matching).
5. Augment the graph with the duplicate edges.
6. Compute an Euler circuit on the augmented graph using Hierholzer's algorithm.

**Characteristics:**
- **Optimal**: minimizes the number of duplicate edge traversals
- **Deterministic**: same graph always produces the same path
- **Covers every edge** at least once (some may be traversed more than once if the graph is not Eulerian)
- **Ignores guards and actions**: this generator skips action execution and ignores edge guards, treating the graph as a pure structure
- Requires the graph to be **strongly connected**
- Emits warnings to stderr for edges with guards, actions, non-unit weights, or missing source vertices

**Example:**
```
new_york_street_sweeper()
```

---

## Combined generators

Multiple generators can be chained in a single string, separated by whitespace. They run sequentially: the first generator runs until its stop condition is fulfilled, then the second takes over from wherever the first stopped, and so on.

**Syntax:**
```
generator1(condition1) generator2(condition2) [generator3(condition3) ...]
```

**Example:**
```
random(length(20)) quick_random(edge_coverage(100)) a_star(reached_vertex(v_End))
```

This runs three phases:
1. Random walk for 20 steps (warm-up)
2. Quick random until all edges are covered
3. A* navigation to `v_End`

---

## Summary table

| Generator | Deterministic | Needs stop condition | Respects guards | Special behavior |
|-----------|:---:|:---:|:---:|---|
| `random` | No | Yes | Yes | |
| `quick_random` | No | Yes | Yes | Steers toward unvisited elements |
| `weighted_random` | No | Yes | Yes | Uses edge weights for selection |
| `a_star` | Yes | Yes (with target) | Yes | Shortest path to target |
| `shortest_all_paths` | Yes | Yes | Yes | Requires Eulerian graph |
| `predefined_path` | Yes | Yes | Yes | Path defined in model |
| `new_york_street_sweeper` | Yes | No | No | Skips actions, optimal edge coverage |
