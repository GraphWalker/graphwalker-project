module org.graphwalker.java {
  exports org.graphwalker.java.test;
  exports org.graphwalker.java.source;

  requires org.graphwalker.core;
  requires json;
  requires reflections;
  requires org.slf4j;
  requires java.xml.bind;
  requires javaparser;
  requires org.graphwalker.io;
  requires org.apache.commons.io;
  requires org.graphwalker.dsl;
  requires gson;
}
