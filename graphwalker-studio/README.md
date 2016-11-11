[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-studio/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-studio)
GraphWalker Studio
==================

## Build Studio

* Install [npm](http://blog.npmjs.org/post/85484771375/how-to-install-npm)
* Run following commands: 
* `cd src/main/webapp`
* `npm install`
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
