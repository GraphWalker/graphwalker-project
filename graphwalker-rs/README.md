# GraphWalker

GraphWalker is a model-based testing tool. It reads models in the shape of directed graphs and generates test paths from them. Tests are modeled as graphs where **vertices** represent states and **edges** represent transitions between states. GraphWalker traverses these graphs using configurable path generators and stop conditions, producing sequences of steps that can drive automated test execution.

GraphWalker supports guards (boolean conditions on edges), actions (scripts that run on traversal), requirements tracking, weighted edges, shared states across multiple models, and several path generation algorithms ranging from pure random walks to optimal Chinese Postman routes.

## Prerequisites

- **Rust** (1.70 or later) &mdash; install via [rustup](https://rustup.rs/)
- **Git**
- **Node.js and npm** (only needed if building the Studio frontend from source)

## Building

```bash
# Clone the repository
git clone https://github.com/GraphWalker/graphwalker-project.git
cd graphwalker-project/graphwalker-rs

# Build all crates (debug)
cargo build

# Build optimized release binaries
cargo build --release

# Run tests
cargo test
```

The main binaries are:

| Binary | Location | Description |
|--------|----------|-------------|
| `graphwalker` | `target/release/graphwalker` | CLI tool for offline/online test generation |
| `graphwalker-studio` | `target/release/graphwalker-studio` | Web-based visual model editor and test runner |

## Quick start

```bash
# Check a model for issues
graphwalker check -g model.json

# Generate a test path offline
graphwalker offline -m model.json "random(edge_coverage(100))"

# Start a REST API server
graphwalker online -s RESTFUL -p 8080 -m model.json "random(edge_coverage(100))"

# Start the visual Studio
graphwalker-studio
```

## Documentation

Full documentation is available at [graphwalker.github.io/graphwalker-rs](https://graphwalker.github.io/graphwalker-rs/).

- [What is Model-Based Testing](https://graphwalker.github.io/graphwalker-rs/model-based-testing)
- [Getting Started](https://graphwalker.github.io/graphwalker-rs/getting-started)
- [CLI Reference](https://graphwalker.github.io/graphwalker-rs/cli)
- [Generators and Stop Conditions](https://graphwalker.github.io/graphwalker-rs/generators)
- [JSON Model Format](https://graphwalker.github.io/graphwalker-rs/json-format)
- [REST API](https://graphwalker.github.io/graphwalker-rs/rest-api)
- [WebSocket API](https://graphwalker.github.io/graphwalker-rs/websocket-api)
- [GraphWalker Studio](https://graphwalker.github.io/graphwalker-rs/studio)

## License

MIT
