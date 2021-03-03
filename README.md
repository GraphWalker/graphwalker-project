[![Build Status](https://travis-ci.org/GraphWalker/graphwalker-project.svg?branch=master)](https://travis-ci.org/GraphWalker/graphwalker-project)
[![Build status](https://ci.appveyor.com/api/projects/status/s0410i90aldxcbh5/branch/master?svg=true)](https://ci.appveyor.com/project/KristianKarl/graphwalker-project/branch/master) 
[![codecov.io](https://codecov.io/github/GraphWalker/graphwalker-project/coverage.svg?branch=master)](https://codecov.io/github/GraphWalker/graphwalker-project?branch=master) <a href="https://scan.coverity.com/projects/graphwalker-graphwalker-project"><img alt="Coverity Scan Build Status" src="https://scan.coverity.com/projects/8246/badge.svg"/></a>
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
