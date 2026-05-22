---
layout: default
title: JSON Model Format
nav_order: 7
---

# JSON Model Format

GraphWalker models are defined in JSON files. A file contains one or more models, each with vertices (states) and edges (transitions).

## Root structure

```json
{
  "models": [ ... ],
  "name": "optional name",
  "seed": 12345
}
```

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `models` | array | Yes | Array of model objects |
| `name` | string | No | Name for the model collection |
| `seed` | integer | No | Random seed for reproducible execution |

---

## Model

Each model is a directed graph with its own generator, start element, and set of vertices and edges.

```json
{
  "name": "LoginTest",
  "id": "853429e2-0528-48b9-97b3-7725eafbb8b5",
  "generator": "random(edge_coverage(100))",
  "startElementId": "e0",
  "actions": ["x = 0;"],
  "requirements": ["REQ001"],
  "properties": { "key": "value" },
  "vertices": [ ... ],
  "edges": [ ... ],
  "predefinedPathEdgeIds": ["e0", "e1", "e2"]
}
```

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `name` | string | No | Human-readable model name |
| `id` | string | No | Unique identifier |
| `generator` | string | No | Generator and stop condition DSL string (e.g., `"random(edge_coverage(100))"`) |
| `startElementId` | string | No | ID of the element to start execution from |
| `actions` | array of strings | No | Model-level initialization scripts, executed before traversal begins |
| `requirements` | array of strings | No | Requirement IDs associated with the model |
| `properties` | object | No | Arbitrary key-value metadata |
| `vertices` | array | No | Vertex (state) definitions |
| `edges` | array | No | Edge (transition) definitions |
| `predefinedPathEdgeIds` | array of strings | No | Ordered edge IDs for the `predefined_path` generator |

---

## Vertex

A vertex represents a state in the model.

```json
{
  "id": "n0",
  "name": "v_LoginPage",
  "sharedState": "LOGGED_OUT",
  "actions": ["validatePage();"],
  "requirements": ["REQ_UI_001"],
  "properties": { "x": 100, "y": 200 }
}
```

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `id` | string | Yes | Unique identifier within the model |
| `name` | string | No | Human-readable name. Convention: prefix with `v_` |
| `sharedState` | string | No | Shared state identifier for multi-model coordination. Vertices in different models with the same `sharedState` act as portals. |
| `actions` | array of strings | No | Scripts executed when this vertex is visited |
| `requirements` | array of strings | No | Requirement IDs satisfied by visiting this vertex |
| `properties` | object | No | Custom metadata (often used for UI layout coordinates) |

---

## Edge

An edge represents a transition between two vertices.

```json
{
  "id": "e1",
  "name": "e_SubmitLogin",
  "sourceVertexId": "n0",
  "targetVertexId": "n1",
  "guard": "!loggedIn",
  "actions": ["loggedIn = true;"],
  "requirements": ["REQ_AUTH_001"],
  "properties": {},
  "weight": 0.8,
  "dependency": 100
}
```

| Field | Type | Required | Description |
|-------|------|:--------:|-------------|
| `id` | string | Yes | Unique identifier within the model |
| `name` | string | No | Human-readable name. Convention: prefix with `e_` |
| `sourceVertexId` | string | No | ID of the source vertex. If omitted, the edge has no source (start edge). |
| `targetVertexId` | string | Yes | ID of the target vertex |
| `guard` | string | No | Boolean expression that must be true for the edge to be traversable |
| `actions` | array of strings | No | Scripts executed when this edge is traversed |
| `requirements` | array of strings | No | Requirement IDs satisfied by traversing this edge |
| `properties` | object | No | Custom metadata |
| `weight` | number | No | Selection probability for `weighted_random` generator (0.0 to 1.0) |
| `dependency` | integer | No | Dependency priority for `dependency_edge_coverage` condition (0 to 100) |

### Start edges

An edge without a `sourceVertexId` is a **start edge**. It represents the initial transition into the model. The `startElementId` in the model typically points to a start edge.

### Guards

Guard expressions are evaluated as boolean conditions using the execution context's variables. Variables are set by edge and vertex actions.

Examples:
- `"loggedIn"` &mdash; true when the variable `loggedIn` is truthy
- `"!loggedIn"` &mdash; true when not logged in
- `"itemCount > 0"` &mdash; comparison
- `"loggedIn && itemCount > 0"` &mdash; compound condition

An edge with a guard that evaluates to false is unavailable for traversal. An empty or missing guard means the edge is always available.

### Actions

Actions are scripts that modify the execution context. They run when the element is visited.

Examples:
- `"loggedIn = true;"` &mdash; set a variable
- `"count = count + 1;"` &mdash; increment
- `"rememberMe = !rememberMe;"` &mdash; toggle

Multiple actions on an element are executed in order.

### Weights

Edge weights control selection probability in the `weighted_random` generator. The sum of weights on all outgoing edges from a vertex must not exceed 1.0. Edges with weight 0 share the remaining probability equally.

### Dependencies

Edge dependency values (0&ndash;100) are used by the `dependency_edge_coverage` stop condition. Edges with dependency at or above the threshold must be visited for the condition to be fulfilled.

---

## Multi-model files

A JSON file can contain multiple models. Models coordinate through **shared states**.

```json
{
  "models": [
    {
      "name": "LoginModel",
      "generator": "random(edge_coverage(100))",
      "startElementId": "e0",
      "vertices": [
        { "id": "n0", "name": "v_Start" },
        { "id": "n1", "name": "v_LoggedIn", "sharedState": "AUTHENTICATED" }
      ],
      "edges": [
        { "id": "e0", "name": "e_Init", "targetVertexId": "n0" },
        { "id": "e1", "name": "e_Login", "sourceVertexId": "n0", "targetVertexId": "n1" }
      ]
    },
    {
      "name": "DashboardModel",
      "generator": "random(edge_coverage(100))",
      "vertices": [
        { "id": "d0", "name": "v_Dashboard", "sharedState": "AUTHENTICATED" },
        { "id": "d1", "name": "v_Settings" }
      ],
      "edges": [
        { "id": "d_e0", "name": "e_OpenSettings", "sourceVertexId": "d0", "targetVertexId": "d1" },
        { "id": "d_e1", "name": "e_Back", "sourceVertexId": "d1", "targetVertexId": "d0" }
      ]
    }
  ]
}
```

When GraphWalker reaches `v_LoggedIn` in `LoginModel`, it can portal to `v_Dashboard` in `DashboardModel` because both have `sharedState: "AUTHENTICATED"`. This allows testing cross-cutting flows that span multiple subsystems.

---

## Predefined path

The `predefinedPathEdgeIds` field defines a specific traversal order:

```json
{
  "name": "RegressionTest",
  "generator": "predefined_path(predefined_path)",
  "startElementId": "n0",
  "vertices": [
    { "id": "n0", "name": "v_A" },
    { "id": "n1", "name": "v_B" }
  ],
  "edges": [
    { "id": "e0", "name": "e_AtoB", "sourceVertexId": "n0", "targetVertexId": "n1" },
    { "id": "e1", "name": "e_BtoA", "sourceVertexId": "n1", "targetVertexId": "n0" }
  ],
  "predefinedPathEdgeIds": ["e0", "e1", "e0"]
}
```

The traversal follows exactly: `v_A` &rarr; `e_AtoB` &rarr; `v_B` &rarr; `e_BtoA` &rarr; `v_A` &rarr; `e_AtoB` &rarr; `v_B`.

---

## Minimal example

The smallest valid model:

```json
{
  "models": [
    {
      "generator": "random(vertex_coverage(100))",
      "startElementId": "e0",
      "vertices": [
        { "id": "n0", "name": "v_Start" }
      ],
      "edges": [
        { "id": "e0", "name": "e_Init", "targetVertexId": "n0" }
      ]
    }
  ]
}
```
