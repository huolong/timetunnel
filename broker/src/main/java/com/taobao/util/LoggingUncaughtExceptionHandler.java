package com.taobao.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @{LoggingUncaughtExceptionHandler
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-25
 * 
 */
public final class LoggingUncaughtExceptionHandler implements UncaughtExceptionHandler {

  private final static Logger LOGGER = LoggerFactory.getLogger("ThreadUncaughtException");

  public final static UncaughtExceptionHandler SINGLETON = new LoggingUncaughtExceptionHandler();

  private LoggingUncaughtExceptionHandler() {}

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    if (e instanceof RuntimeException && e.getCause() != null) {
      if (e.getCause() instanceof InterruptedException) Thread.currentThread().interrupt();
      LOGGER.error(t.getName(), e.getCause());
      return;
    }
    LOGGER.error(t.getName(), e);
  }
}
