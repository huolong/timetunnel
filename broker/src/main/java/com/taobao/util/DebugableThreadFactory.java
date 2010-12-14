package com.taobao.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NamedThreadFactory
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-8-18
 * 
 */
public class DebugableThreadFactory implements ThreadFactory {

  public DebugableThreadFactory(final String name) {
    final SecurityManager s = System.getSecurityManager();
    group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    namePrefix = name + "-thread-";
  }

  @Override
  public Thread newThread(final Runnable r) {
    final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
    if (t.isDaemon()) t.setDaemon(false);
    if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
    t.setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.SINGLETON);
    return t;
  }

  private final ThreadGroup group;

  private final String namePrefix;

  private final AtomicInteger threadNumber = new AtomicInteger(1);
}
