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
cp target/graphwalker-cli-3.0.0-SNAPSHOT.jar gw.jar
```

Running it!
--------------

```sh
java -jar gw.jar 
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

Command-line syntax
---------------------
You need to give the cli an sub-command. These are the sub-command:
  - **offline**


Sub-command: offline
---------------------
Will create a test sequence generated from a model, and write it to the terminal.

The syntax is:
```sh
offline -m model.graphml "GENERATOR(STOP_CONDITION)"
```
where GENERATOR can be:
  - **offline** - Navigate through the model in a completly random manor. Also called "Drunkard’s walk", or "Random walk". This algorithm selects an out-edge from a vertex by random, and repeats the process in the next vertex.

  - **a_star** - Will calulate and generate the shortest path through the model. This algorithm is not recommended to use, because for larger models, and using data in the model (EFSM), it will take a considerable time to calculate.

You can concatenate multiple generators. For example:
```sh
offline -m model.graphml "a_star(edge_coverge(100)) random(time_duration(900))"
```

and where STOP_CONDITION can be:
  - **edge_coverage(N)** - The stop criteria is a percentage number (N). When, during execution, the percentage of traversed edges is reached, the test is stopped. If an edge is traversed more than one time, it still counts as 1, when calculating the percentage coverage.

  - **vertex_coverage(N)** - The stop criteria is a percentage number (N). When, during execution, the percentage of traversed states is reached, the test is stopped. If vertex is traversed more than one time, it still counts as 1, when calculating the percentage coverage.

  - **reached_vertex(name)** - The stop criteria is a named vertex (name). When, during execution, the vertex is reached, the test is stopped.

  - **reached_edge(name)** - The stop criteria is a named edge (name). When, during execution, the edge is reached, the test is stopped.

  - **time_duration(N)** - The stop criteria is a time, representing the number of seconds (N) that the test generator is allowed to execute.

Stop conditions can be combinational, using logical AND OR, or && ||. For example:
```sh
offline -m model.graphml "random(edge_coverge(100) || time_duration(900))"
```
Let's try it
---------------------
Let's try it. First of all we need a model to work with. Fetch one from our repo: [UC01_GW3.graphml], save it to disk.
```sh
java -jar gw.jar offline -m UC01_GW3.graphml "random(edge_coverage(100))"
v_BrowserStopped
e_StartBrowser
v_BrowserStarted
e_EnterBaseURL
v_BaseURL
e_SearchBook
v_SearchResult
e_ShoppingCart
v_ShoppingCart
e_SearchBook
v_SearchResult
e_ClickBook
v_BookInformation
e_AddBookToCart
v_OtherBoughtBooks
e_SearchBook
:
:
```


[UC01_GW3.graphml]:https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/src/test/resources/graphml/UC01_GW3.graphml
