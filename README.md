[![Build Status](https://travis-ci.org/GraphWalker/graphwalker-project.svg?branch=master)](https://travis-ci.org/GraphWalker/graphwalker-project) [![Build status](https://ci.appveyor.com/api/projects/status/s0410i90aldxcbh5/branch/master?svg=true)](https://ci.appveyor.com/project/KristianKarl/graphwalker-project/branch/master) [![codecov.io](https://codecov.io/github/GraphWalker/graphwalker-project/coverage.svg?branch=master)](https://codecov.io/github/GraphWalker/graphwalker-project?branch=master) <a href="https://scan.coverity.com/projects/graphwalker-graphwalker-project"><img alt="Coverity Scan Build Status" src="https://scan.coverity.com/projects/8246/badge.svg"/></a> [![Stories in Ready](https://badge.waffle.io/GraphWalker/graphwalker-project.png?label=ready&title=Ready)](https://waffle.io/GraphWalker/graphwalker-project) [![Stories in Progress](https://badge.waffle.io/GraphWalker/graphwalker-project.png?label=In%20Progress&title=In%20Progress)](https://waffle.io/GraphWalker/graphwalker-project)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-project)
GraphWalker
===================

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
graphwalker-cli/target/graphwalker-cli-4.0.0-SNAPSHOT.jar
```

Run it like:
```bash
java -jar graphwalker-cli/target/graphwalker-cli-4.0.0-SNAPSHOT.jar
```

## Build Studio

* Install [npm](http://blog.npmjs.org/post/85484771375/how-to-install-npm)
* Run following commands: 
* `cd graphwalker-studio/src/main/webapp`
* `sudo npm install -g`
* `sudo npm install webpack -g`
* `webpack`

```bash
mvn package -pl graphwalker-studio -am
```

The jar is in:
```bash
graphwalker-studio/target/graphwalker-cli-3.2.1.jar
```

Run it like:
```bash
java -jar graphwalker-studio/target/graphwalker-cli-4.0.0-SNAPSHOT.jar
```
