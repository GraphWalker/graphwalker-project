package org.graphwalker.io.factory;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by krikar on 8/20/14.
 */
public class DescriptiveErrorListener extends BaseErrorListener {
  private static final Logger logger = LoggerFactory.getLogger(DescriptiveErrorListener.class);
  public static DescriptiveErrorListener INSTANCE = new DescriptiveErrorListener();

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                          int line, int charPositionInLine,
                          String msg, RecognitionException e)
  {
    logger.error(msg);
    throw new YEdParsingException(msg);
  }
}