GraphWalker Model Checker
===================

Documentation on http://graphwalker.org/

### The Model Checker

Analyzes Contexts, Models, Edges and Vertices, and returns a list of found issues. For example, a vertex without a name is not a valid vertex:

```java
 List<String> issues = VertexChecker.hasIssues(new Vertex().build());
```
The check above will return a list containing 1 string saying: _Name of vertex cannot be null_

Or like in the example below. All elements (edges and vertices) must have unique ids:
```java
Model model = new Model();
model.addVertex(new Vertex().setId("NOTUNIQUEID").setName("SomeName"));
model.addVertex(new Vertex().setId("NOTUNIQUEID").setName("SomeOtherName"));
List<String> issues = ModelChecker.hasIssues(model.build());
```
The check will return: _Id of the vertex is not unique: NOTUNIQUEID_

Another more elaborate check can be done on the a Context object:
```java
Vertex v1 = new Vertex().setName("v1").setId("v1");
Vertex v2 = new Vertex().setName("v2").setId("v2");
Vertex v3 = new Vertex().setName("v3").setId("v3");
Vertex v4 = new Vertex().setName("v4").setId("v4");

Model model = new Model();
model.addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2).setName("e1").setId("e1"));
model.addEdge(new Edge().setSourceVertex(v2).setTargetVertex(v3).setName("e2").setId("e2"));
model.addEdge(new Edge().setSourceVertex(v2).setTargetVertex(v4).setName("e3").setId("e3"));
model.addEdge(new Edge().setTargetVertex(v1).setName("e0").setId("e0"));

Context context = new JsonContext();
context.setModel(model.build()).setPathGenerator(new RandomPath(new EdgeCoverage(100)));
context.setNextElement(context.getModel().findElements("e0").get(0));

List<String> issues = ContextChecker.hasIssues(context);
```
The check will detect that the path generator in combination with the stop coverage and the design of the model will not work: _The model has multiple cul-de-sacs, and is requested to run using a random path generator and 100% edge coverage. That will not work._
