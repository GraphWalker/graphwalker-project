[![Java CI with Maven](https://github.com/GraphWalker/graphwalker-project/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/GraphWalker/graphwalker-project/actions/workflows/maven.yml)
[![CodeQL](https://github.com/GraphWalker/graphwalker-project/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/GraphWalker/graphwalker-project/actions/workflows/codeql-analysis.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project)

# GraphWalker

Documentation on http://graphwalker.org/

## Get the source code and build all modules

```bash
git clone https://github.com/GraphWalker/graphwalker-project.git
cd graphwalker-project
mvn install
```

## Build the stand command-line tool

```bash
mvn package -pl graphwalker-cli -am
```

The jar is in:
```bash
graphwalker-cli/target/graphwalker-cli-<VERSION>.jar
```

Run it like:
```bash
java -jar graphwalker-cli/target/graphwalker-cli-<VERSION>.jar
```

## Build Studio

```bash
mvn package -pl graphwalker-studio -am
```

The jar is in:
```bash
graphwalker-studio/target/
```
 
Run it like:
```bash
java -jar graphwalker-studio/target/graphwalker-studio-<VERSION>.jar
```

===================

Predefined Path
===================
This fork of the GraphWalker project enables the user to define an edge sequence in the input graph, along which the machine should execute.

## Graph input format

Currently a predefined path can only be specified in JSON GW3 input graphs.

To define an edge sequence, the model has to contain an array element called *predefinedPathEdgeIds* containing the edge IDs in the sequence.

The generator and stop condition has to be specified in the *generator* element.

Example:

```JSON
{
    "models": [
        {
            "generator": "predefined_path(predefined_path)",
            ...
            "edges": [
                { "id": "e0", ... },
                { "id": "e1", ... },
                { "id": "e2", ... }
            ],
            "predefinedPathEdgeIds": [ "e0", "e1", "e2", "e0" ]
        }
    ]
}
```