---
layout: default
title: GraphWalker Studio
nav_order: 10
---

# GraphWalker Studio

GraphWalker Studio is a web-based visual editor and test execution environment. It lets you create models graphically, run test generation with real-time visualization, and debug execution with breakpoints and step-through controls.

## Starting Studio

```bash
graphwalker-studio [OPTIONS]
```

| Option | Short | Long | Default | Description |
|--------|-------|------|---------|-------------|
| Browser port | `-b` | `--browser-port` | `9090` | HTTP port for the web UI |
| WebSocket port | `-w` | `--websocket-port` | `9999` | WebSocket port for the execution engine |
| Static directory | | `--static-dir` | `static` | Directory containing the frontend files |
| Debug logging | | `--debug` | off | Enable debug output |

After starting, open [http://localhost:9090](http://localhost:9090) in your browser.

---

## Architecture

Studio consists of two servers and a browser-based frontend:

```
Browser (React + Cytoscape.js)
    ↕ WebSocket (JSON messages)
graphwalker-studio backend
    ├── HTTP server (port 9090) — serves the web UI
    └── WebSocket server (port 9999) — handles execution commands
```

The HTTP server serves the static frontend files. The WebSocket server handles all model operations: validation, execution, session management, and real-time event broadcasting.

The frontend communicates with the backend exclusively through the [WebSocket API](websocket-api). All commands documented in the WebSocket API reference are available through Studio.

---

## Features

### Model editing

- **Create models** with vertices and edges using a visual graph editor
- **Edit properties** for any element: name, guards, actions, requirements, shared state, weight, dependency
- **Multiple models** in a single file with tabs for each model
- **Import** JSON and GraphML model files
- **Export** models as JSON

### Test execution

- **Play** to run the generator continuously
- **Pause** to stop execution at the current element
- **Step** to advance exactly one element at a time
- **Stop** to end the session

During execution, visited elements are highlighted in the graph. The current element is visually distinct from previously visited elements.

### Execution settings

- **Seed** &mdash; set a random seed for reproducible execution
- **Global data** &mdash; initialize variables before execution starts
- **Delay** &mdash; set a delay between steps for visual observation

### Breakpoints

Click on any vertex or edge to set a breakpoint. When execution reaches a breakpoint element, the session automatically pauses. This allows you to inspect the current state before continuing.

### Session observation

Multiple Studio instances (or WebSocket clients) can connect to the same backend. One session can be observed by another:

1. Client A starts a session (presses Play)
2. Client B opens Studio and sees the session in the session list
3. Client B subscribes to Client A's session
4. Client B sees real-time updates as Client A's execution progresses

This is useful for demonstrations, collaborative debugging, or monitoring test execution from multiple views.

### Model validation

Studio validates models in real time as you edit. Validation checks include:

- Vertices must have non-empty names
- Element IDs must be unique within a model
- Edges must have target vertices
- Vertex and edge names must not contain spaces
- Edge weights must be non-negative
- Self-loops on unnamed edges are flagged
- Models must have a start element or a shared state
- Models used with `random(edge_coverage(100))` flag cul-de-sac vertices (vertices with no outgoing edges)

---

## Workflow example

### 1. Create a model

Open Studio and create a new model. Add vertices for each state in your system and edges for each transition. Set names, guards, and actions on the elements.

### 2. Set the generator

Configure the generator string in the model properties. For example: `random(edge_coverage(100))`.

### 3. Validate

Studio shows validation issues in real time. Fix any issues before running.

### 4. Run

Press Play. Watch the execution traverse the graph. Visited elements change color. The status bar shows progress toward the stop condition.

### 5. Debug

If something unexpected happens:
- **Pause** execution
- **Set breakpoints** on elements of interest
- **Step** through one element at a time
- **Inspect data** to see current variable values

### 6. Export

Save the model as JSON for use with the CLI or integration into your test pipeline:

```bash
graphwalker offline -g exported-model.json
```

---

## Keyboard shortcuts

The Studio frontend supports standard keyboard interactions for graph editing. Refer to the Cytoscape.js documentation for graph manipulation controls (pan, zoom, select).

---

## Connecting programmatically

Since Studio uses the standard WebSocket API, you can connect to its WebSocket port from any client:

```javascript
const ws = new WebSocket("ws://localhost:9999/graphwalker");

ws.onopen = () => {
  ws.send(JSON.stringify({
    command: "start",
    gw: { models: [ /* your model */ ] },
    seed: 42
  }));
};

ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  console.log(msg);
};
```

This lets you build custom test runners, dashboards, or integrations that interact with the same execution engine that powers Studio.
