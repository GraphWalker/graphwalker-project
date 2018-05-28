module org.graphwalker.maven.plugin {
  exports org.graphwalker.maven.plugin;

  requires org.graphwalker.java;
  requires org.graphwalker.core;
  requires org.graphwalker.io;
  requires maven.plugin.annotations;
  requires maven.model;
  requires maven.plugin.api;
  requires maven.core;
  requires org.slf4j;
  requires plexus.utils;
  requires java.xml;
  requires jdk.unsupported;
}
