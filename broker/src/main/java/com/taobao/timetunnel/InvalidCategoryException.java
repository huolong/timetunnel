package com.taobao.timetunnel;

/**
 * {@link InvalidCategoryException}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
@SuppressWarnings("serial")
public class InvalidCategoryException extends RuntimeException {

  public InvalidCategoryException() {
    super();
  }

  public InvalidCategoryException(final String message) {
    super(message);
  }

  public InvalidCategoryException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InvalidCategoryException(final Throwable cause) {
    super(cause);
  }

}
