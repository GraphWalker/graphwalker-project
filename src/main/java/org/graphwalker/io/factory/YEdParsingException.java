package org.graphwalker.io.factory;

/**
 * Created by krikar on 8/20/14.
 */
public class YEdParsingException extends RuntimeException {

  public YEdParsingException(Throwable throwable) {
    super(throwable);
  }

  public YEdParsingException(String message) {
    super(message);
  }
}
