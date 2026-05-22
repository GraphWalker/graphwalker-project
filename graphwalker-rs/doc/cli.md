---
layout: default
title: CLI Reference
nav_order: 4
---

# CLI Reference

```
graphwalker [--debug] <subcommand>
```

## Global flags

| Flag | Description |
|------|-------------|
| `--debug` | Enable debug logging |
| `--help` | Show help |
| `--version` | Show version |

---

## offline

Generate a test path from a model and print it to stdout.

```
graphwalker offline [OPTIONS]
```

### Options

| Flag | Short | Long | Description | Default |
|------|-------|------|-------------|---------|
| Model + generator | `-m` | `--model` | Model file and generator as a pair: `-m <file> <generator>`. Can be repeated. | |
| Model with embedded generator | `-g` | `--gw` | JSON model file with generator embedded in the `"generator"` field. | |
| Verbose | `-o` | `--verbose` | Include execution data (variables) in each output line. | off |
| Unvisited | | `--unvisited` | Include unvisited element counts and names in each output line. | off |
| Start element | `-e` | `--start-element` | Override the start element by name. | from model |
| Seed | `-s` | `--seed` | Random seed. `0` means use a random seed. | `0` |

Either `--model` or `--gw` is required. They are mutually exclusive.

### Output format

One JSON object per line:

```json
{"currentElementName":"e_SomeEdge"}
{"currentElementName":"v_SomeVertex"}
```

With `--verbose`:

```json
{"currentElementName":"e_SomeEdge","data":"x=1; loggedIn=true"}
```

With `--unvisited`:

```json
{"currentElementName":"e_SomeEdge","numberOfElements":10,"numberOfUnvisitedElements":7,"unvisitedElements":[{"elementName":"e_Other"},{"elementName":"v_Other"}]}
```

With both `--verbose` and `--unvisited`, each unvisited element also includes its `elementId`.

### Examples

```bash
# Use embedded generator from JSON file
graphwalker offline -g model.json

# Specify generator on command line
graphwalker offline -m model.json "random(edge_coverage(100))"

# Multiple models with different generators
graphwalker offline -m model1.json "random(edge_coverage(100))" \
                    -m model2.json "a_star(reached_vertex(v_End))"

# Deterministic with seed
graphwalker offline -g model.json -s 42

# Custom start element
graphwalker offline -g model.json -e v_LoginPage
```

---

## online

Start a REST or WebSocket service for interactive test generation.

```
graphwalker online [OPTIONS]
```

### Options

| Flag | Short | Long | Description | Default |
|------|-------|------|-------------|---------|
| Service type | `-s` | `--service` | `RESTFUL` or `WEBSOCKET` | `WEBSOCKET` |
| Port | `-p` | `--port` | Port to listen on | `8887` |
| Model + generator | `-m` | `--model` | Model file and generator pair. Can be repeated. | |
| Start element | `-e` | `--start-element` | Override start element by name. | from model |
| Seed | | `--seed` | Random seed. `0` means random. | `0` |

### Examples

```bash
# REST API on port 8080
graphwalker online -s RESTFUL -p 8080 -m model.json "random(edge_coverage(100))"

# WebSocket on default port
graphwalker online -m model.json "random(edge_coverage(100))"

# With fixed seed
graphwalker online -s RESTFUL -p 8080 --seed 42 -m model.json "random(edge_coverage(100))"
```

---

## check

Validate model files and print statistics.

```
graphwalker check [OPTIONS]
```

### Options

| Flag | Short | Long | Description |
|------|-------|------|-------------|
| Model + generator | `-m` | `--model` | Model file and generator pair. Can be repeated. |
| Model with embedded generator | `-g` | `--gw` | JSON file with embedded generator. |

Either `--model` or `--gw` is required.

### Output

If no issues are found:

```
No issues found with the model(s).

Statistics:
  Model: LoginTest
    Unique edges:    4
    Unique vertices: 3
    Edge instances:    4
    Vertex instances:  3
```

If issues are found, they are printed one per line and the command exits with a non-zero status:

```
Name of vertex cannot be null or empty
Vertex 'v_Orphan' has no edges
2 issue(s) found
```

For multiple models, a grand total is printed after per-model statistics:

```
Statistics:
  Model: ModelA
    Unique edges:    2
    Unique vertices: 2
    Edge instances:    2
    Vertex instances:  2
  Model: ModelB
    Unique edges:    3
    Unique vertices: 2
    Edge instances:    3
    Vertex instances:  2
  Total:
    Unique edges:    5
    Unique vertices: 4
    Edge instances:    5
    Vertex instances:  4
```

"Unique" means distinct element names within each model. Edges with the same name in different models are counted separately.

---

## methods

List all unique method names (vertex and edge names) from the model.

```
graphwalker methods -m <file> [<file>...]
```

### Options

| Flag | Short | Long | Description |
|------|-------|------|-------------|
| Model files | `-m` | `--model` | One or more model files. Required. |

### Output

Sorted, deduplicated list of names, one per line:

```
e_AnotherAction
e_FirstAction
e_SomeOtherAction
v_VerifySomeAction
v_VerifySomeOtherAction
```

---

## requirements

List all unique requirement keys from the model.

```
graphwalker requirements -m <file> [<file>...]
```

### Options

| Flag | Short | Long | Description |
|------|-------|------|-------------|
| Model files | `-m` | `--model` | One or more model files. Required. |

### Output

Sorted, deduplicated list of requirement keys, one per line:

```
REQ001
REQ002
REQ003
```

---

## convert

Convert a model file to another format.

```
graphwalker convert -i <input-file> [-f <format>]
```

### Options

| Flag | Short | Long | Description | Default |
|------|-------|------|-------------|---------|
| Input file | `-i` | `--input` | Path to the input model file. Required. | |
| Output format | `-f` | `--format` | Output format. Currently only `json` is supported. | `json` |

Outputs the converted model to stdout. Supports input formats: JSON, GraphML.

### Examples

```bash
# Convert GraphML to JSON
graphwalker convert -i model.graphml -f json > model.json

# Default format is JSON
graphwalker convert -i model.graphml > model.json
```

---

## source

Generate source code from a model using a template.

```
graphwalker source -i <model-file> <template-file>
```

### Options

| Flag | Short | Long | Description |
|------|-------|------|-------------|
| Input files | `-i` | `--input` | Two arguments: model file path and template file path. Required. |

### Template format

The template file uses three sections delimited by special markers:

{% raw %}
```
HEADER<{{
// This code appears once at the top
public class MyTest {
}}>HEADER
BODY
    public void {LABEL}() {
        // test step
    }
FOOTER<{{
}
}}>FOOTER
```
{% endraw %}

- {% raw %}`HEADER<{{ ... }}>HEADER`{% endraw %} &mdash; printed once at the beginning
- {% raw %}`BODY` &mdash; the `{LABEL}` placeholder{% endraw %} is replaced with each method name from the model
- {% raw %}`FOOTER<{{ ... }}>FOOTER`{% endraw %} &mdash; printed once at the end

### Example

```bash
graphwalker source -i model.json template.txt
```
