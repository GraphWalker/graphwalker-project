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

### Building and installing


```sh
git clone https://github.com/GraphWalker/graphwalker-cli graphwalker-cli
cd graphwalker-cli
mvn package
cp target/graphwalker-cli-3.0.0-SNAPSHOT.jar gw.jar

# Create an alias for the gw3 command
sudo echo "#!/bin/bash\n\njava -jar <PATH TO CLI>/graphwalker-cli/target/graphwalker-cli-3.0.0-SNAPSHOT.jar "$@"" > /usr/local/bin/gw3
sudo chmod +x /usr/local/bin/gw3
```

### Running it!

```sh
%> gw3
Usage: java -jar graphwalker.jar [options] [command] [command options]
  Options:
    --debug, -d
       Sets the log level: OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL. Default is
       OFF
       Default: OFF
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
          --unvisited, -u
             Will also print the remaining unvisited elements in the model.
             Default: false
          --verbose, -o
             Will print more details to stdout
             Default: false

    online      Generate a test sequence offline. The sequence is fetched using different techniques, like RestAPI
      Usage: online [options]
        Options:
        * --model, -m
             The model, as a graphml file followed by generator with stop
             condition. The format is GENERATOR(STOP_CONDITION) See HTML DOC
             Default: []
          --restful, -r
             Starts as a Restful API service.
             Default: true

    methods      Generates a list of unique names of vertices and edges in the model.
      Usage: methods [options]
        Options:
        * --model, -m
             The model, as a graphml file.
             Default: <empty string>

    requirements      Generates a list of unique names of the requirements found in the model.
      Usage: requirements [options]
        Options:
        * --model, -m
             The model, as a graphml file.
             Default: <empty string>

```

### Command-line syntax
You need to give the cli an sub-command. These are the sub-command:
  - **offline**
  - **online**
  - **methods**
  - **requirements**


### Sub-command: offline
Will create a test sequence generated from a model, and write it to the terminal.

The syntax is:
```sh
%> gw3 offline -m model.graphml "GENERATOR(STOP_CONDITION)"
```
where GENERATOR can be:
- **random** - Navigate through the model in a completly random manor. Also called "Drunkard’s walk", or "Random walk". This algorithm selects an out-edge from a vertex by random, and repeats the process in the next vertex.

- **quick_random** - Tries to run the shortest path through a model, but in a fast fashion. This is how the algorithm works:
   1. Choose an edge not yet visited by random.
   1. Select the shortest path to that edge using the A Star algorithm
   1. Walk that path, and mark all those edges being executed as visited.
   1. When reaching the selected edge in step 1, start all over, repeating steps 1->4.
   
   The algorithm works well an very large models, and generates reasonably short sequences. The downside is when useed in conjunction with ESFM, the algorithm can choose a path which is blocked by a guard.

- **a_star** - Will calulate and generate the shortest path to a designated vertex in the model.
- **shortest_all_paths** - Will calulate and generate the shortest path through the model. This algorithm is not recommended to use, because for larger models, and using data in the model (EFSM), it will take a considerable time to calculate.

It's mandatory to give the generator a stop condition. This is done by adding it within paranthesis after the generator name, like:
```sh
a_star(reached_vertex(v_LoggedIn))
```
You can concatenate multiple generators, like:
```sh
%> gw3 offline -m model.graphml "a_star(edge_coverge(100)) random(time_duration(900))"
```

and where STOP_CONDITION can be:
  - **edge_coverage(N)** - The stop criteria is a percentage number (N) between 1 and 100. When, during execution, the percentage of traversed edges is reached, the test is stopped. If an edge is traversed more than one time, it still counts as 1, when calculating the percentage coverage.

  - **vertex_coverage(N)** - The stop criteria is a percentage number (N) between 1 and 100. When, during execution, the percentage of traversed states is reached, the test is stopped. If vertex is traversed more than one time, it still counts as 1, when calculating the percentage coverage.

  - **reached_vertex(name)** - The stop criteria is a named vertex (name). When, during execution, the vertex is reached, the test is stopped. The named vertex must exist in the model

  - **reached_edge(name)** - The stop criteria is a named edge (name). When, during execution, the edge is reached, the test is stopped. The named edge must exist in the model

  - **time_duration(N)** - The stop criteria is a time, representing the number of seconds (N) that the test generator is allowed to execute. The number (N) must be larger than 0.

  - **never** - This special condition will never halt the generator..

Stop conditions can be combinational, using logical AND OR, or && ||. For example:
```sh
%> gw3 offline -m model.graphml "random(edge_coverge(100) || time_duration(900))"
%> gw3 offline -m model.graphml "random(edge_coverge(100) OR time_duration(900))"
%> gw3 offline -m model.graphml "random(edge_coverge(100) && time_duration(900))"
%> gw3 offline -m model.graphml "random(edge_coverge(100) AND time_duration(900))"
%> gw3 offline -m model.graphml "random(reached_vertex(e_SomeEdge) and edge_coverage(100)) random((reached_vertex(e_SomeEdge) and reached_edge(e_SomeEdge)) || time(5000))""
```

### Let's try it
Let's try it. First of all we need a model to work with. Fetch one from our repo: [Login.graphml], save it to disk.
```sh
%> gw3 offline -m Login.graphml "random(edge_coverage(100))"
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
```
The generated path will look different evry time we run it, since we are usning a random generator.


### Sub-command: methods
Generates a list of unique names of vertices and edges in the model.

```sh
%> gw3 methods -m Login.graphml"
e_Close
e_Exit
e_Init
e_InvalidCredentials
e_Logout
e_StartClient
e_ToggleRememberMe
e_ValidPremiumCredentials
v_Browse
v_ClientNotRunning
v_LoginPrompted
[```

### Sub-command: requirements
Generates a list of unique requirements found in the model.



[Login.graphml]:https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/src/test/resources/graphml/shared_state/Login.graphml
