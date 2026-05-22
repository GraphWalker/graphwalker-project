---
layout: default
title: WebSocket API
nav_order: 9
---

# WebSocket API

Start the WebSocket server with:

```bash
graphwalker online -s WEBSOCKET -p 8887 -m model.json "random(edge_coverage(100))"
```

Connect with any WebSocket client to `ws://localhost:8887/graphwalker`.

All messages are JSON objects. Commands from the client include a `"command"` field. Responses include `"success": true` or `"success": false` with a `"message"` field on failure.

---

## Commands

### start

Create a new execution session.

**Request:**

```json
{
  "command": "start",
  "gw": { "models": [ ... ] },
  "seed": 12345,
  "globalData": "x = 0; y = 0;",
  "name": "My Test Session"
}
```

| Field | Required | Description |
|-------|:--------:|-------------|
| `gw` | Yes | Model definition (same format as the JSON model file) |
| `seed` | No | Random seed for deterministic execution |
| `globalData` | No | Initialization script for global variables |
| `name` | No | Session name (defaults to the first model's name) |

**Response:**

```json
{
  "command": "start",
  "success": true,
  "seed": 12345,
  "sessionId": "session-1"
}
```

---

### getNext

Advance the execution by one step.

**Request:**

```json
{ "command": "getNext" }
```

**Response:**

```json
{
  "command": "visitedElement",
  "success": true,
  "modelId": "model-id",
  "elementId": "e1",
  "name": "e_Login",
  "visitedCount": 1,
  "totalCount": 12,
  "stopConditionFulfillment": 0.45,
  "data": "loggedIn=false"
}
```

| Field | Description |
|-------|-------------|
| `modelId` | ID of the model containing the element |
| `elementId` | ID of the visited element |
| `name` | Name of the visited element |
| `visitedCount` | How many times this element has been visited |
| `totalCount` | Total elements in the model |
| `stopConditionFulfillment` | Progress toward the stop condition (0.0 to 1.0) |
| `data` | Current variable values |

If the session is paused, `getNext` blocks until the session is resumed or stepped.

---

### hasNext

Check whether more steps are available.

**Request:**

```json
{ "command": "hasNext" }
```

**Response:**

```json
{
  "command": "hasNext",
  "success": true,
  "hasNext": true
}
```

---

### getData

Get current execution variables.

**Request:**

```json
{ "command": "getData" }
```

**Response:**

```json
{
  "command": "getData",
  "success": true,
  "data": "loggedIn=true; count=5"
}
```

---

### setData

Modify execution variables.

**Request:**

```json
{
  "command": "setData",
  "action": "loggedIn = true;"
}
```

**Response:**

```json
{
  "command": "setData",
  "success": true
}
```

---

### getModel

Retrieve the loaded model definition.

**Request:**

```json
{ "command": "getModel" }
```

**Response:**

```json
{
  "command": "getModel",
  "success": true,
  "models": "{ \"models\": [ ... ] }"
}
```

The `models` field is a JSON string (not a parsed object).

---

### updateAllElements

Get visit counts for all elements.

**Request:**

```json
{ "command": "updateAllElements" }
```

**Response:**

```json
{
  "command": "updateAllElements",
  "success": true,
  "elements": [
    { "modelId": "model-1", "elementId": "n0", "visitedCount": 3 },
    { "modelId": "model-1", "elementId": "e0", "visitedCount": 2 }
  ]
}
```

---

### check

Validate a model without loading it.

**Request:**

```json
{
  "command": "check",
  "gw": { "models": [ ... ] }
}
```

**Response:**

```json
{
  "command": "check",
  "success": true,
  "issues": []
}
```

If there are issues:

```json
{
  "command": "check",
  "success": true,
  "issues": ["Name of vertex cannot be null or empty"]
}
```

---

### convertGraphml

Convert a GraphML model to JSON.

**Request:**

```json
{
  "command": "convertGraphml",
  "graphml": "<graphml xmlns='...'> ... </graphml>"
}
```

**Response:**

```json
{
  "command": "convertGraphml",
  "success": true,
  "models": "{ \"models\": [ ... ] }"
}
```

---

## Session management

The WebSocket API supports multiple concurrent sessions. These commands control session execution and observation.

### listSessions

List all active sessions.

**Request:**

```json
{ "command": "listSessions" }
```

**Response:**

```json
{
  "command": "sessions",
  "success": true,
  "sessions": [
    { "id": "session-1", "name": "Login Test" },
    { "id": "session-2", "name": "Payment Flow" }
  ]
}
```

---

### subscribeSession

Subscribe to execution events from another session.

**Request:**

```json
{
  "command": "subscribeSession",
  "sessionId": "session-1"
}
```

**Response:**

```json
{
  "command": "subscribeSession",
  "success": true,
  "sessionId": "session-1",
  "name": "Login Test",
  "models": { "models": [ ... ] },
  "elements": [
    { "modelId": "model-1", "elementId": "n0", "visitedCount": 2 }
  ],
  "seed": 12345,
  "paused": false
}
```

After subscribing, you receive broadcast events (see below) whenever the session advances.

---

### unsubscribeSession

Stop receiving events from a subscribed session.

**Request:**

```json
{ "command": "unsubscribeSession" }
```

**Response:**

```json
{
  "command": "unsubscribeSession",
  "success": true
}
```

---

### pauseSession

Pause an active session. Subsequent `getNext` calls block until resumed.

**Request:**

```json
{
  "command": "pauseSession",
  "sessionId": "session-1"
}
```

**Response:**

```json
{
  "command": "pauseSession",
  "success": true
}
```

---

### resumeSession

Resume a paused session.

**Request:**

```json
{
  "command": "resumeSession",
  "sessionId": "session-1"
}
```

**Response:**

```json
{
  "command": "resumeSession",
  "success": true
}
```

---

### stepSession

Execute exactly one step, then pause again.

**Request:**

```json
{
  "command": "stepSession",
  "sessionId": "session-1"
}
```

**Response:**

```json
{
  "command": "stepSession",
  "success": true
}
```

---

### setDelay

Set a delay (in milliseconds) between execution steps. Useful for watching execution in real time.

**Request:**

```json
{
  "command": "setDelay",
  "sessionId": "session-1",
  "value": 500
}
```

**Response:**

```json
{
  "command": "setDelay",
  "success": true
}
```

---

### setBreakpoints

Set breakpoints on specific elements. When execution reaches a breakpoint, the session automatically pauses.

**Request:**

```json
{
  "command": "setBreakpoints",
  "sessionId": "session-1",
  "breakpoints": ["model-1,n0", "model-1,e2"]
}
```

Breakpoint format: `"modelId,elementId"`.

**Response:**

```json
{
  "command": "setBreakpoints",
  "success": true
}
```

---

## Broadcast events

These are unsolicited messages sent to subscribed clients.

### visitedElement

Sent when a subscribed session traverses an element.

```json
{
  "command": "visitedElement",
  "modelId": "model-1",
  "elementId": "e1",
  "name": "e_Login",
  "visitedCount": 1,
  "totalCount": 12,
  "stopConditionFulfillment": 0.45,
  "data": "loggedIn=false"
}
```

### sessionCreated

Sent when a new session is created (to clients that called `listSessions`).

```json
{
  "command": "sessionCreated",
  "sessionId": "session-2",
  "name": "New Session"
}
```

### sessionEnded

Sent when a session terminates.

```json
{
  "command": "sessionEnded",
  "sessionId": "session-1"
}
```

### sessionPaused

Sent when a session is paused (manually or by breakpoint).

```json
{
  "command": "sessionPaused",
  "sessionId": "session-1"
}
```

When triggered by a breakpoint:

```json
{
  "command": "sessionPaused",
  "sessionId": "session-1",
  "reason": "breakpoint",
  "modelId": "model-1",
  "elementId": "n0"
}
```

### sessionResumed

Sent when a session resumes.

```json
{
  "command": "sessionResumed",
  "sessionId": "session-1"
}
```
