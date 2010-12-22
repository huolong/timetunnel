package com.taobao.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @{link Events}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-26
 * 
 */
public final class Events {

  static {
    EXECUTOR =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                                   new DebugableThreadFactory("events-executor", true));
    SCHEDULER =
      Executors.newSingleThreadScheduledExecutor(new DebugableThreadFactory("events-scheduler",
                                                                            true));
    final Thread shutdownEvents = new Thread(new Runnable() {

      @Override
      public void run() {
        dispose();
      }
    }, "shutdown-events");
    shutdownEvents.setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.SINGLETON);
    Runtime.getRuntime().addShutdownHook(shutdownEvents);
  }

  private Events() {}

  public static void dispose() {
    try {
      do {
        SCHEDULER.shutdownNow();
      } while (!SCHEDULER.awaitTermination(500L, TimeUnit.MILLISECONDS));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    try {
      do {
        EXECUTOR.shutdownNow();
      } while (!EXECUTOR.awaitTermination(500L, TimeUnit.MILLISECONDS));
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * @see ExecutorService#execute(Runnable)
   */
  public static void enqueue(final Runnable task) {
    EXECUTOR.execute(task);
  }

  // public static <T> Future<T> submit(final Callable<T> task) {
  // return EXECUTOR.submit(task);
  // }

  /**
   * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long,
   *      TimeUnit)
   */
  public static ScheduledFuture<?> scheduleAtFixedRate(final Runnable command,
                                                       final long period,
                                                       final TimeUnit unit) {
    return SCHEDULER.scheduleAtFixedRate(event(command), period, period, unit);
  }

  private static Runnable event(final Runnable command) {
    return new Runnable() {

      @Override
      public void run() {
        EXECUTOR.execute(command);
      }
    };
  }

  private final static ExecutorService EXECUTOR;
  private final static ScheduledExecutorService SCHEDULER;
}
