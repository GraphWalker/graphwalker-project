# Domain specific language (DSL) for GraphWalker

This describes the DSL for the [graphwalker-cli], and what the rules are when a model is created using the [yEd] model editor.


## The DSL is a directed graph

The objective of the DSL, is to express the expected behavior of the system under test. To do that we use a [directed graph], in which a vertex (or a node) represents some desired state, and the edges represents whatever actions we need to do in order to achive that desired state.

For example, let's take a web site that needs authentication before we can access the sites content. Using a directed graph, it might look like this:

![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/example1.jpg "Simple example 1")

#### Vertex
A vertex represents a state, and in any implementing code/test, this is where you'll find the assertions. It is here where the [oracles] resides.

In [yEd] a vertex is called node, normally depicted as a box.

GraphWalker does not care what colors a vertex has.

#### Edge
Represents the transition from one vertex to another. It is whatever action is need to be made in order to reach the next state. It could be selection some menu choice, clickin a button, or making a SOAP API call.

In [yEd] an edge is also called edge.

GraphWalker only accepts on-way directed edges (arrows). 

GraphWalker does not care what colors or thickness an edge has..


## The Rules
This section will talke about the modelling rules. But please note that these rules are specific for the graphwalker-cli and the [yEdModelFactory]. They mostly the same as for GraphWalker 2, with some exceptions.

### Start vertex
![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/StartVertex.png "Start Vertex")

* There must be 1 (and only 1) vertex with the name: ***Start*** in a model.
* There can only be 1 out-edge from the Start vertex.
 
### Name
A name is the first word, on the first line in a label for an edge or vertex.

### Label
A label is all the text associated to and edge or a vertex.

### Guards
Guards is a mechanism only associated to edges. Their role are the same as an if-statement, and makes an edge eligible or not for being walked.

The guard is a conditional expression enclosed between square brackets:
```
[loggedIn == true]
``` 
The above means that if the attribute loggedIn equals to true, the edge is walkable.

### Action
This is java script code that we want to execute in the model. Each statement must be ended with a semicolon.
```
/loggedIn=false; rememberMe=true;
``` 
The purpose of the action code, is to serve as data to the guards.

In the model, the action is placed after a forward slash.

#### Example
![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/GuardAndActions.png "Guards and Actions")

This example illustrates how actions and guards work.
* Lets start with the out-edge from the Start vertex:
```javaScript
e_Init/validLogin=false;rememberMe=false;
``` 
The name of the edge is ***e_Init***, followed by a forward slash, denoting that text from that point until end-of-line is [action] code. The action initializes 2 attributes: ***validLogin*** and ***rememberMe***.

* When we have walked the edge above, we arrive at the ***v_ClientNotRunning*** vertex. This vertex has 2 out-edges, which both have guards. Since both ***validLogin*** and ***rememberMe*** are at this point initialized to false, only 1 edge is accessible for walking: the edge ***e_Start*** that has the vertex ***v_LoginPrompted*** as destination.

* Now lets say that we have traversed the edges ***e_ToggleRememberMe*** and ***e_ValidPremiumCredentials*** and arrive again at the vertex ***v_ClientNotRunning***, we would now expect GraphWalker to select the other ***e_Start*** that has the vertex ***v_Browse*** as destination.

This illustrates how we can direct and control flows through a graph, if we need to do that.

### Keywords
Keywords are used in the models to increase functionality and usability.

* ***Start*** - This is used in a vertex to denote the Start vertex. See: [Start vertex](#Start vertex). Only one Start vertex per model.

* ***BLOCKED*** - A vertex or an edge containing this keyword, will be exclude when a path is generated. If it's an edge, it will simply be removed from the graph. If it's a vertex, the vertex will be removed with its in- and out-edges.

* ***SHARED*** - This keyword is only for vertices. It means that GraphWalker can jump out of the current model, to any other model which shares the same name. The syntax is:
```
SHARED:SOME_NAME
``` 
See: [Multiple models](#Multiple models)

* ***INIT*** - Only a vertex can have this keyword. When using data in a model, the data needs to be initialized. That is what this keyword does. The syntax is:
```
INIT:loggedIn=false; rememberMe=true;
``` 
Only one INIT keyword per model.


### Multiple models

GrapwWalker can work with several models in one session. It means that when generating a path, GraphWalker can choose to jump out of one model into another one. This is very handy when separating different functionaly into several models. For example. Lets say you have a system that you want to test, and you would need to login to do that. Then it might make sense to create a single model handling the login functionality, and other models to handle whatever else you want to test. The login model would then be reused for ever other test scenario.

#### It's not the same thing as flattening
When flatting models, several models are merged into on single model, which then is being traversed by GraphWalker. This is not the case here. GraphWalker is executing every model in it's own context. The scope of the data in the models are not shared between them.

#### SHARED:SOME_NAME
The mechanism that controls the jumping between the models is the keyword SHARED. Let's look at an example. Consider these 4 models:

![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/ModelA.png "Model A")
![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/ModelB.png "Model B")
![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/ModelC.png "Model C")
![alt text](https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/doc/img/ModelD.png "Model D")

All models are loaded into GraphWalker, and the first model (Model A) is where the path generation is started. Using graphwalker-cli, the command line could look something like this:

```sh
gw3 offline -m src/test/resources/graphml/shared_state/Model_A.graphml "random(edge_coverage(100))" -m src/test/resources/graphml/shared_state/Model_B.graphml "random(edge_coverage(100))" -m src/test/resources/graphml/shared_state/Model_C.graphml "random(edge_coverage(100))" -m src/test/resources/graphml/shared_state/Model_D.graphml "random(edge_coverage(100))"
``` 

When the path generation reaches the vertex ***v_B*** in Model A, it has to consider the keyword ***SHARED:B***.. This tells GraphWalker to search all other models for the same keyword using the same name: ***B***. In our case, there is only one, and it's in Model B. Now GraphWalker makes a descision whether to jump out of Model A, into the vertex ***v_B*** in Model B, or to stay in Model A. This descision is based on random.

Also, if the path generation is executing in Model B, and it reaches the vertex ***v_B***, GraphWalker can jump out of Model B, back to vertex ***v_B*** in Model A.

[graphwalker-cli]:https://github.com/GraphWalker/graphwalker-cli
[yEd]:http://www.yworks.com/en/products_yed_about.html
[directed graph]:http://en.wikipedia.org/wiki/Directed_graph
[oracles]:http://en.wikipedia.org/wiki/Oracle_(software_testing)
[yEdModelFactory]:https://github.com/GraphWalker/graphwalker-io/blob/master/src/main/java/org/graphwalker/io/factory/yEdModelFactory.java
