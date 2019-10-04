[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-maven-archetype/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-maven-archetype)
GraphWalker Maven Archetype
================

A GraphWalker maven archetype. To help create boilerplate GraphWalker maven projects.

To create a sample project:

~~~sh
%> mvn archetype:generate -B -DarchetypeGroupId=org.graphwalker -DarchetypeArtifactId=graphwalker-maven-archetype -DgroupId=com.company -DartifactId=myProject -DarchetypeVersion=LATESTmvn archetype:generate -B -DarchetypeGroupId=org.graphwalker -DarchetypeArtifactId=graphwalker-maven-archetype -DgroupId=com.company -DartifactId=myProject -DarchetypeVersion=LATEST
~~~

Then cd into the project:
~~~sh
%> cd myProject
~~~
Build and run the test:
~~~sh
%> mvn compile exec:java -Dexec.mainClass="com.company.Runner" 
~~~

