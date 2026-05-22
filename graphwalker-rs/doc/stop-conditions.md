---
layout: default
title: Stop Conditions
nav_order: 6
---

# Stop Conditions

A stop condition determines when a generator should stop traversing the graph. Most conditions require execution to be at a **vertex** to be considered fulfilled &mdash; this prevents stopping mid-transition.

## DSL syntax

Stop conditions are specified inside generator parentheses:

```
generator(condition)
```

Conditions can be combined with `and`/`&&` (all must be met) or `or`/`||` (any must be met):

```
generator(condition1 and condition2)
generator(condition1 or condition2)
```

Condition names are case-insensitive. Both `snake_case` and `camelCase` forms are accepted.

---

## edge_coverage

Stop when a percentage of all edges have been visited at least once.

**Syntax:** `edge_coverage(percent)` or `edgecoverage(percent)`

**Parameter:** integer 0&ndash;100

**Fulfilled when:** `(visited_edges / total_edges) >= (percent / 100)` and execution is at a vertex.

**Example:**
```
random(edge_coverage(100))    -- visit every edge
random(edge_coverage(50))     -- visit at least half
```

---

## vertex_coverage

Stop when a percentage of all vertices have been visited at least once.

**Syntax:** `vertex_coverage(percent)` or `vertexcoverage(percent)`

**Parameter:** integer 0&ndash;100

**Fulfilled when:** `(visited_vertices / total_vertices) >= (percent / 100)` and execution is at a vertex.

**Example:**
```
quick_random(vertex_coverage(100))
random(vertex_coverage(80))
```

---

## reached_vertex

Stop when a specific vertex (by name) has been visited.

**Syntax:** `reached_vertex(name)` or `reachedvertex(name)`

**Parameter:** vertex name (string)

**Fulfilled when:** execution is at a vertex matching the given name. Once fulfilled, stays fulfilled even if execution moves away ("sticky").

**Note:** If multiple vertices share the same name, reaching any one of them is sufficient.

**Example:**
```
a_star(reached_vertex(v_Checkout))
random(reached_vertex(v_ErrorPage))
```

---

## reached_edge

Stop when a specific edge (by name) has been traversed.

**Syntax:** `reached_edge(name)` or `reachededge(name)`

**Parameter:** edge name (string)

**Fulfilled when:** execution is at an edge matching the given name. Once fulfilled, stays fulfilled ("sticky").

**Example:**
```
a_star(reached_edge(e_SubmitOrder))
random(reached_edge(e_Login))
```

---

## reached_shared_state

Stop when a vertex belonging to a specific shared state has been reached.

**Syntax:** `reached_shared_state(name)` or `reachedsharedstate(name)`

**Parameter:** shared state name (string)

**Fulfilled when:** execution is at a vertex with a matching `sharedState` value. Sticky.

**Example:**
```
a_star(reached_shared_state(LOGGED_IN))
```

---

## length

Stop after a specified number of total element visits.

**Syntax:** `length(count)`

**Parameter:** unsigned integer

**Fulfilled when:** the total number of visited elements (vertices + edges combined) reaches the count, and execution is at a vertex.

**Example:**
```
random(length(100))     -- stop after 100 steps
random(length(1000))    -- stop after 1000 steps
```

---

## time_duration

Stop after a specified duration of wall-clock time.

**Syntax:** `time_duration(seconds)` or `timeduration(seconds)`

**Parameter:** integer (seconds)

**Fulfilled when:** the elapsed time since the condition was created exceeds the specified duration, and execution is at a vertex.

**Example:**
```
random(time_duration(60))     -- run for 1 minute
random(time_duration(300))    -- run for 5 minutes
```

---

## requirement_coverage

Stop when a percentage of all requirements have been tested (passed or failed).

**Syntax:** `requirement_coverage(percent)` or `requirementcoverage(percent)`

**Parameter:** integer 0&ndash;100

**Fulfilled when:** `((passed + failed) / total_requirements) >= (percent / 100)` and execution is at a vertex. If no requirements are defined, the condition is always fulfilled.

**Example:**
```
random(requirement_coverage(100))
```

---

## dependency_edge_coverage

Stop when all edges with a dependency value at or above a threshold have been visited.

**Syntax:** `dependency_edge_coverage(threshold)` or `dependencyedgecoverage(threshold)`

**Parameter:** integer 0&ndash;100 (dependency threshold)

**Fulfilled when:** all edges with `dependency >= threshold/100` have been visited, and execution is at a vertex.

**Example:**
```
random(dependency_edge_coverage(80))    -- cover all edges with dependency >= 80%
```

---

## predefined_path

Stop when the predefined path has been fully traversed.

**Syntax:** `predefined_path` or `predefinedpath`

No parameters. Used with the `predefined_path` generator.

**Fulfilled when:** the predefined path index has reached the end of the sequence.

**Example:**
```
predefined_path(predefined_path)
```

---

## never

Never stops. Used for generators that manage their own termination (like `new_york_street_sweeper`) or as a component in combined conditions.

**Syntax:** `never`

No parameters.

**Example:**
```
random(never)    -- walk forever (until interrupted)
```

---

## Combining conditions

### AND (all must be met)

Use `and` or `&&`:

```
random(edge_coverage(100) and reached_vertex(v_End))
random(length(100) && vertex_coverage(80))
```

The condition is fulfilled when **all** sub-conditions are fulfilled simultaneously. Fulfillment progress is the average of all sub-conditions.

### OR (any must be met)

Use `or` or `||`:

```
random(edge_coverage(100) or time_duration(60))
random(reached_vertex(v_A) || reached_vertex(v_B))
```

The condition is fulfilled when **any** sub-condition is fulfilled. Fulfillment progress is the maximum of all sub-conditions.

---

## Summary table

| Condition | Parameter | Sticky | Description |
|-----------|-----------|:------:|-------------|
| `edge_coverage` | percent | No | Percentage of edges visited |
| `vertex_coverage` | percent | No | Percentage of vertices visited |
| `reached_vertex` | name | Yes | Specific vertex reached |
| `reached_edge` | name | Yes | Specific edge traversed |
| `reached_shared_state` | name | Yes | Shared state reached |
| `length` | count | No | Total element visits |
| `time_duration` | seconds | No | Wall-clock time elapsed |
| `requirement_coverage` | percent | No | Percentage of requirements tested |
| `dependency_edge_coverage` | threshold | No | High-dependency edges visited |
| `predefined_path` | none | No | Predefined path completed |
| `never` | none | No | Never fulfilled |
