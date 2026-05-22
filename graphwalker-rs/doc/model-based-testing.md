---
layout: default
title: Model-Based Testing
nav_order: 2
---

# Model-Based Testing

## What is Model-Based Testing?

Model-Based Testing (MBT) is a software testing approach where test cases are derived from a model that describes the behavior of the system under test. Instead of writing individual test cases by hand, you build a model of how the system works, and a tool automatically generates test sequences from that model.

The model is typically a **directed graph** (also called a finite state machine) where:

- **Vertices** (nodes) represent **states** of the system &mdash; observable conditions like "logged in", "shopping cart empty", or "error page displayed"
- **Edges** (transitions) represent **actions** that move the system from one state to another &mdash; things like "click login", "add item to cart", or "submit form"

A test sequence is a **path through the graph**: a series of alternating edges and vertices that represents a realistic usage scenario of the system.

## Why use MBT?

**Coverage guarantees.** Instead of hoping your manually written tests cover all the important paths, MBT tools can mathematically ensure that every edge, every vertex, or every requirement has been exercised.

**Reduced maintenance.** When the system changes, you update the model &mdash; a single graph &mdash; rather than dozens or hundreds of individual test scripts.

**Combinatorial exploration.** A model with 10 vertices and 20 edges can produce thousands of distinct test paths. MBT tools explore this space systematically, finding paths that a human tester might never think to try.

**Separation of concerns.** The model captures *what* to test. The generator decides *how* to traverse it. The test implementation maps steps to actual system interactions. Each can evolve independently.

## How GraphWalker implements MBT

GraphWalker takes a model (a directed graph defined in JSON or GraphML) and generates a sequence of steps by walking the graph. The walk is controlled by two things:

1. **A path generator** &mdash; the algorithm that decides which edge to take next. Options range from pure random selection to optimal mathematical routes.

2. **A stop condition** &mdash; the rule that decides when to stop walking. Options include coverage targets (e.g., "visit every edge"), reaching a specific element, time limits, or step counts.

### The execution loop

```
1. Start at the designated start element
2. While the stop condition is not fulfilled:
   a. If at a vertex: choose an outgoing edge (using the generator algorithm)
   b. If at an edge: move to the target vertex
   c. Execute any actions on the current element
   d. Check any guards on candidate edges
   e. Record the visit
3. Output the sequence of visited elements
```

### Guards and actions

Edges can have **guards** &mdash; boolean expressions that must evaluate to true for the edge to be available. This lets you model conditional behavior: "the logout button is only available when logged in."

Edges and vertices can have **actions** &mdash; scripts that execute when the element is visited. Actions modify variables in the execution context: `loggedIn = true` or `itemCount = itemCount + 1`. Guards can reference these variables, creating dynamic behavior that changes which paths are available as the walk progresses.

### Multi-model execution

Complex systems can be modeled as multiple graphs that share state through **shared state vertices**. When GraphWalker reaches a shared state vertex in one model, it can jump to the matching shared state vertex in another model, continuing the walk there. This lets you decompose a large system into manageable sub-models that coordinate through well-defined handoff points.

### Requirements tracking

Vertices and edges can be tagged with **requirement IDs**. As GraphWalker traverses the model, it tracks which requirements have been covered. The `requirement_coverage` stop condition can ensure that all requirements are exercised before the walk ends.

## Example

Consider testing a login flow. The model might look like this:

```
[Start] --e_OpenApp--> [v_LoginPage] --e_EnterCredentials--> [v_LoginPage]
                              |                                     |
                              +--e_ValidLogin--> [v_Dashboard] -----+
                              |                       |
                              +--e_InvalidLogin-> [v_LoginPage]
                              |
                              +--e_ForgotPassword--> [v_ResetPage]
```

Running `random(edge_coverage(100))` on this model produces a path that visits every edge at least once, exercising the valid login, invalid login, and forgot password flows in a single test sequence. The specific order varies with each run (unless you fix the random seed), but coverage is guaranteed.
