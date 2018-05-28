module org.graphwalker.dsl {
  exports org.graphwalker.dsl.antlr;
  exports org.graphwalker.dsl.antlr.dot;
  exports org.graphwalker.dsl.dot;
  exports org.graphwalker.dsl.antlr.generator;
  exports org.graphwalker.dsl.antlr.yed;
  exports org.graphwalker.dsl.yed;

  requires org.graphwalker.core;
  requires antlr4.runtime;
}
