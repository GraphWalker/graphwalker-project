---
layout: default
title: REST API
nav_order: 8
---

# REST API

Start the REST server with:

```bash
graphwalker online -s RESTFUL -p 8080 -m model.json "random(edge_coverage(100))"
```

All endpoints are under the `/graphwalker` path prefix.

## Response format

Successful responses include `"result": "ok"`. Errors include `"result": "nok"` with an `"error"` message.

---

## POST /graphwalker/load

Load a model and initialize the execution engine.

**Request body:** JSON model definition (the full model file content).

**Response:**

```json
{
  "result": "ok",
  "seed": 7298345612
}
```

The returned `seed` can be used later to reproduce the same traversal.

**Error example:**

```json
{
  "result": "nok",
  "error": "Model has no generator specified"
}
```

---

## GET /graphwalker/hasNext

Check whether more steps are available.

**Response:**

```json
{
  "result": "ok",
  "hasNext": "true"
}
```

Returns `"false"` when the stop condition is fulfilled.

**Error (no model loaded):**

```json
{
  "result": "nok",
  "error": "No model(s) are loaded."
}
```

---

## GET /graphwalker/getNext

Advance to the next element and return it.

**Response:**

```json
{
  "result": "ok",
  "currentElementName": "e_Login",
  "currentElementID": "e1",
  "modelId": "853429e2-0528-48b9-97b3-7725eafbb8b5"
}
```

| Field | Description |
|-------|-------------|
| `currentElementName` | Name of the current element (vertex or edge) |
| `currentElementID` | ID of the current element |
| `modelId` | ID of the model containing the element |

---

## GET /graphwalker/getData

Get the current execution data (all variables).

**Response:**

```json
{
  "result": "ok",
  "data": "loggedIn=true; itemCount=3"
}
```

The `data` field is a string representation of all variables in the execution context.

---

## PUT /graphwalker/setData/{script}

Execute a script to modify execution data. The script is passed as a URL path parameter.

**Example request:**

```
PUT /graphwalker/setData/loggedIn%20%3D%20true%3B
```

(URL-decoded: `loggedIn = true;`)

**Response:**

```json
{
  "result": "ok"
}
```

---

## PUT /graphwalker/restart

Reset execution to the initial state. The model remains loaded but all visit counts, variables, and state are cleared.

**Response:**

```json
{
  "result": "ok"
}
```

---

## GET /graphwalker/getStatistics

Get coverage statistics for the current execution.

**Response:**

```json
{
  "result": "ok",
  "totalNumberOfVertices": 5,
  "totalNumberOfEdges": 8,
  "totalNumberOfVisitedVertices": 3,
  "totalNumberOfVisitedEdges": 6,
  "totalNumberOfUnvisitedVertices": 2,
  "totalNumberOfUnvisitedEdges": 2,
  "vertexCoverage": 60,
  "edgeCoverage": 75
}
```

| Field | Description |
|-------|-------------|
| `totalNumberOfVertices` | Total vertices in all models |
| `totalNumberOfEdges` | Total edges in all models |
| `totalNumberOfVisitedVertices` | Vertices visited at least once |
| `totalNumberOfVisitedEdges` | Edges visited at least once |
| `totalNumberOfUnvisitedVertices` | Vertices not yet visited |
| `totalNumberOfUnvisitedEdges` | Edges not yet visited |
| `vertexCoverage` | Percentage of vertices visited (0&ndash;100) |
| `edgeCoverage` | Percentage of edges visited (0&ndash;100) |

---

## Typical usage pattern

```bash
# 1. Load a model (or start the server with -m flag to pre-load)
curl -X POST http://localhost:8080/graphwalker/load -d @model.json

# 2. Loop: check and step
while curl -s http://localhost:8080/graphwalker/hasNext | grep -q '"true"'; do
  STEP=$(curl -s http://localhost:8080/graphwalker/getNext)
  echo "$STEP"
  # Execute the test step indicated by currentElementName
done

# 3. Check final coverage
curl -s http://localhost:8080/graphwalker/getStatistics
```

---

## Seed and determinism

When starting with `--seed <value>`, the same seed produces identical traversal paths. The seed is also returned by the `/load` endpoint, so you can capture it and replay later:

```bash
# Capture the seed
SEED=$(curl -s -X POST http://localhost:8080/graphwalker/load -d @model.json | jq -r '.seed')
echo "Seed: $SEED"

# Replay with the same seed later
graphwalker online -s RESTFUL --seed $SEED -m model.json "random(edge_coverage(100))"
```
