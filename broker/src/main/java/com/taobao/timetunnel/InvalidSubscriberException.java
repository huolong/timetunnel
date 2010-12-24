package com.taobao.timetunnel;

/**
 * {@link InvalidSubscriberException}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-22
 * 
 */
@SuppressWarnings("serial")
public class InvalidSubscriberException extends RuntimeException {

  public InvalidSubscriberException() {
    super();
  }

  public InvalidSubscriberException(final String message) {
    super(message);
  }

  public InvalidSubscriberException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InvalidSubscriberException(final Throwable cause) {
    super(cause);
  }

}
