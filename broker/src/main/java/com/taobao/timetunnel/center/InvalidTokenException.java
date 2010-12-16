package com.taobao.timetunnel.center;

/**
 * {@link InvalidTokenException}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
@SuppressWarnings("serial")
public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException() {
    super();
  }

  public InvalidTokenException(final String message) {
    super(message);
  }

  public InvalidTokenException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InvalidTokenException(final Throwable cause) {
    super(cause);
  }
}
