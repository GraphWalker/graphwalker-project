GraphWalker CLI [![Build Status](https://travis-ci.org/GraphWalker/graphwalker-cli.svg?branch=master)](https://travis-ci.org/GraphWalker/graphwalker-cli)
======
The GraphWalker Command Line Interface.

This module runs GraphWalker as an command-line tool in a terminal. With GraphWalker CLI, you will be able:
  - Verify and check your models.
  - Run any generator and stop conditions
  - Combine with AND and OR multiple stop conditions for a generator
  - Concatenate (chain) multiple generators
  - Use multiple models
  - Create test sequences (on the standard output in the terminal)

You won't be able to:
  - Run actual tests.

Building
--------------

```sh
git clone https://github.com/GraphWalker/graphwalker-cli graphwalker-cli
cd graphwalker-cli
mvn package
```

Running it!
--------------

```sh
java -jar target/graphwalker-cli-3.0.0-SNAPSHOT.jar 
Usage: java -jar graphwalker.jar [options] [command] [command options]
  Options:
    --help, -h
       Prints help text
       Default: false
    --version, -v
       Prints the version of graphwalker
       Default: false
  Commands:
    offline      Generate a test sequence offline. The sequence is printed to the standard output
      Usage: offline [options]
        Options:
        * --model, -m
             The model, as a graphml file followed by generator with stop
             condition. The format is GENERATOR(STOP_CONDITION) See HTML DOC
             Default: []

```
