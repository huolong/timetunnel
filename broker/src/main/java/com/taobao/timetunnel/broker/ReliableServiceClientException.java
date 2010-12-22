package com.taobao.timetunnel.broker;

/**
 * {@link ReliableServiceClientException}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
@SuppressWarnings("serial")
public class ReliableServiceClientException extends RuntimeException {

  public ReliableServiceClientException() {
    super();
  }

  public ReliableServiceClientException(final String message) {
    super(message);
  }

  public ReliableServiceClientException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ReliableServiceClientException(final Throwable cause) {
    super(cause);
  }

}
