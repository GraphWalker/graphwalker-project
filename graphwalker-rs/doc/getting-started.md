---
layout: default
title: Getting Started
nav_order: 3
---

# Getting Started

This guide walks you through installing GraphWalker, creating a model, and generating your first test path.

## Installation

### Prerequisites

- [Rust](https://rustup.rs/) 1.70 or later
- Git

### Build from source

```bash
git clone https://github.com/GraphWalker/graphwalker-rs.git
cd graphwalker-rs
cargo build --release
```

The binaries are in `target/release/`:

```bash
# Add to your PATH (optional)
export PATH="$PWD/target/release:$PATH"
```

## Create a model

Create a file called `login.json`:

```json
{
  "models": [
    {
      "name": "LoginTest",
      "id": "login-model",
      "generator": "random(edge_coverage(100))",
      "startElementId": "e_init",
      "vertices": [
        { "id": "v_start", "name": "v_Start" },
        { "id": "v_login", "name": "v_LoginPage" },
        { "id": "v_app",   "name": "v_Application" }
      ],
      "edges": [
        {
          "id": "e_init",
          "name": "e_OpenBrowser",
          "targetVertexId": "v_start"
        },
        {
          "id": "e_navigate",
          "name": "e_NavigateToLogin",
          "sourceVertexId": "v_start",
          "targetVertexId": "v_login"
        },
        {
          "id": "e_login",
          "name": "e_SubmitCredentials",
          "sourceVertexId": "v_login",
          "targetVertexId": "v_app"
        },
        {
          "id": "e_logout",
          "name": "e_Logout",
          "sourceVertexId": "v_app",
          "targetVertexId": "v_login"
        }
      ]
    }
  ]
}
```

This model has three states (Start, LoginPage, Application) and four transitions.

## Validate the model

```bash
graphwalker check -g login.json
```

Expected output:

```
No issues found with the model(s).

Statistics:
  Model: LoginTest
    Unique edges:    4
    Unique vertices: 3
    Edge instances:    4
    Vertex instances:  3
```

## Generate a test path

### Offline mode

Generate a complete test path and print it:

```bash
graphwalker offline -g login.json
```

Output (one JSON object per line):

```json
{"currentElementName":"e_OpenBrowser"}
{"currentElementName":"v_Start"}
{"currentElementName":"e_NavigateToLogin"}
{"currentElementName":"v_LoginPage"}
{"currentElementName":"e_SubmitCredentials"}
{"currentElementName":"v_Application"}
{"currentElementName":"e_Logout"}
{"currentElementName":"v_LoginPage"}
```

The generator (`random(edge_coverage(100))`) is embedded in the JSON file. Every edge is visited at least once.

### Override the generator on the command line

```bash
graphwalker offline -m login.json "random(vertex_coverage(100))"
```

This uses the `-m` flag which takes a model file and a generator string as a pair.

### Deterministic output with a seed

```bash
graphwalker offline -g login.json -s 42
```

The same seed always produces the same path.

### Verbose output

```bash
graphwalker offline -g login.json -o
```

Includes execution data (variable values) in each step.

### Track unvisited elements

```bash
graphwalker offline -g login.json --unvisited
```

Each step includes counts of remaining unvisited elements.

## Use guards and actions

Update the model to add conditional behavior. Here the `e_SubmitCredentials` edge sets `loggedIn = true`, and a new `e_DirectAccess` edge has a guard that checks this variable:

```json
{
  "id": "e_login",
  "name": "e_SubmitCredentials",
  "sourceVertexId": "v_login",
  "targetVertexId": "v_app",
  "actions": ["loggedIn = true;"]
},
{
  "id": "e_direct",
  "name": "e_DirectAccess",
  "sourceVertexId": "v_start",
  "targetVertexId": "v_app",
  "guard": "loggedIn"
}
```

The `e_DirectAccess` edge is only available after `loggedIn` has been set to `true` by a previous traversal of `e_SubmitCredentials`.

## Start an online service

### REST API

```bash
graphwalker online -s RESTFUL -p 8080 -m login.json "random(edge_coverage(100))"
```

Then interact via HTTP:

```bash
curl http://localhost:8080/graphwalker/hasNext
# {"result":"ok","hasNext":"true"}

curl http://localhost:8080/graphwalker/getNext
# {"result":"ok","currentElementName":"e_OpenBrowser","currentElementID":"e_init","modelId":"login-model"}
```

See [REST API](rest-api) for the full reference.

### WebSocket API

```bash
graphwalker online -s WEBSOCKET -p 8887 -m login.json "random(edge_coverage(100))"
```

Connect with any WebSocket client and send JSON commands. See [WebSocket API](websocket-api) for details.

## Use GraphWalker Studio

Launch the visual editor:

```bash
graphwalker-studio
```

Open [http://localhost:9090](http://localhost:9090) in your browser. You can:

- Create and edit models visually
- Import JSON and GraphML files
- Run test generation with play/pause/step controls
- Set breakpoints on elements
- Watch execution in real time

See [GraphWalker Studio](studio) for the full guide.

## Next steps

- [Generators](generators) &mdash; learn about all the path generation algorithms
- [Stop Conditions](stop-conditions) &mdash; understand when and how traversal stops
- [JSON Model Format](json-format) &mdash; full specification for model files
- [CLI Reference](cli) &mdash; all commands, flags, and options
