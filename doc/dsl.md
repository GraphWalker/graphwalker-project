# Domain specific language (DSL) for GraphWalker 3

This describes the DSL for the [graphwalker-cli], and what the rules are when a model is created using the [yEd] model editor.


## The DSL is a directed graph

The objective of the DSL, is to express the expected behavior of the system under test. To do that we use a [directed graph], in which a vertex (or a node) represents some desired state, and the edges represents whatever actions we need to do in order to achive that desired state.

For example, let's take a web site that needs authentication before we can access the sites content. Using a directed graph, it might look like this:

![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/example1.jpg "Simple example 1")

#### Vertex
A vertex represents a state, and in any implementing code/test, this is where you'll find the assertions. It is here where the [oracles] resides.

#### Edge
Represents the transition from one vertex to another. It is whatever action is need to be made in order to reach the next state. It could be selection some menu choice, clickin a button, or making a SOAP API call.

[graphwalker-cli]:https://github.com/GraphWalker/graphwalker-cli
[yEd]:http://www.yworks.com/en/products_yed_about.html
[directed graph]:http://en.wikipedia.org/wiki/Directed_graph
[oracles]:http://en.wikipedia.org/wiki/Oracle_(software_testing)

## The Rules

