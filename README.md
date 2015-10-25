GraphWalker [![Build Status](https://travis-ci.org/GraphWalker/graphwalker-project.svg?branch=master)](https://travis-ci.org/GraphWalker/graphwalker-project) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project)
===================

Documentation on http://graphwalker.org/

### How to build

Build and install GraphWalker modules locally on your machine

```bash
git clone https://github.com/GraphWalker/graphwalker-project.git
cd graphwalker-project
mvn install
```

### Build standalone command-line tool

```bash
mvn package -pl graphwalker-cli -am
```

The jar is in:
```bash
graphwalker-cli/target/graphwalker-cli-3.4.0-SNAPSHOT.jar
```
