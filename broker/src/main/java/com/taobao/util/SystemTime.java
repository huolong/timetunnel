package com.taobao.util;

import java.util.concurrent.TimeUnit;

/**
 * @{link SystemTime}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-26
 * 
 */
public final class SystemTime {
  static {
    current = System.currentTimeMillis();
    final long period = Long.parseLong(System.getProperty("system.time.tick", "10"));
    Events.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        current = System.currentTimeMillis();
      }
    }, period, TimeUnit.MILLISECONDS);
  }

  private SystemTime() {}

  public static long current() {
    return current;
  }

  private volatile static long current;
}
