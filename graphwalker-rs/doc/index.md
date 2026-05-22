---
layout: default
title: Home
nav_order: 1
---

# GraphWalker

GraphWalker is a model-based testing tool. It reads models in the shape of directed graphs and generates test paths from them.

Tests are modeled as graphs where **vertices** represent states and **edges** represent transitions. GraphWalker traverses these graphs using configurable path generators and stop conditions, producing sequences of steps that drive automated test execution.

---

## Features

- **Multiple path generators** &mdash; random, quick random, weighted random, A*, shortest all paths, predefined path, and New York Street Sweeper (Chinese Postman)
- **Flexible stop conditions** &mdash; edge/vertex coverage, reached element, time duration, length, requirement coverage, and combinations with AND/OR
- **Guards and actions** &mdash; edges can have boolean guards and script actions that modify execution state
- **Multi-model support** &mdash; coordinate across models using shared states
- **Requirements tracking** &mdash; tag elements with requirement IDs and track coverage
- **Multiple interfaces** &mdash; CLI (offline batch), REST API, WebSocket API, and a visual Studio editor
- **Deterministic replay** &mdash; seed-based random generation for reproducible test paths

## Getting started

See the [Getting Started](getting-started) guide for a walkthrough of creating a model and generating your first test path.

## Contents

| Page | Description |
|------|-------------|
| [Model-Based Testing](model-based-testing) | What MBT is and how GraphWalker implements it |
| [Getting Started](getting-started) | Tutorial: install, create a model, run it |
| [CLI Reference](cli) | All CLI subcommands, flags, and options |
| [Generators](generators) | Path generation algorithms |
| [Stop Conditions](stop-conditions) | When to stop generating |
| [JSON Model Format](json-format) | Complete specification of the model file format |
| [REST API](rest-api) | HTTP API for online test execution |
| [WebSocket API](websocket-api) | WebSocket protocol for real-time interaction |
| [GraphWalker Studio](studio) | Visual model editor and test runner |
