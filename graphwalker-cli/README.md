[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-cli/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-cli)
GraphWalker CLI
===============

Documentation at https://github.com/GraphWalker/graphwalker-project/wiki/Command-Line-Tool

## How to build the standalone jar of graphwalker-cli

```bash
git clone https://github.com/GraphWalker/graphwalker-project.git
cd graphwalker-project
mvn package -pl graphwalker-cli -am
```

The jar is in: `graphwalker-cli/target/graphwalker-cli-<VERSION>.jar`.

Run it like:

```bash
java -jar graphwalker-cli/target/graphwalker-cli-<VERSION>.jar
```
