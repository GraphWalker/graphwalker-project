[![Java CI with Maven](https://github.com/GraphWalker/graphwalker-project/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/GraphWalker/graphwalker-project/actions/workflows/maven.yml)
[![CodeQL](https://github.com/GraphWalker/graphwalker-project/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/GraphWalker/graphwalker-project/actions/workflows/codeql-analysis.yml)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project)

# GraphWalker

Documentation on http://graphwalker.org/

## GraphWalker survey

We created a questionnaire which we would love our users to fill in.
The purpose is to help us understand how GraphWalker is used.

Please go to [GraphWalker Survey](https://goo.gl/forms/rvHWHHsWIsh4Hw9y1)

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
