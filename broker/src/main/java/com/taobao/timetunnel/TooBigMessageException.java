package com.taobao.timetunnel;

/**
 * {@link TooBigMessageException}
 * @author  <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
@SuppressWarnings("serial")
public class TooBigMessageException extends RuntimeException {

  public TooBigMessageException() {
    super();
  }

  public TooBigMessageException(final String message) {
    super(message);
  }

  public TooBigMessageException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public TooBigMessageException(final Throwable cause) {
    super(cause);
  }

}
