module org.graphwalker.core {
  exports org.graphwalker.core.machine;
  exports org.graphwalker.core.generator;
  exports org.graphwalker.core.model;
  exports org.graphwalker.core.condition;
  exports org.graphwalker.core.common;
  exports org.graphwalker.core.event;
  exports org.graphwalker.core.statistics;

  requires org.slf4j;
  requires java.scripting;
}
