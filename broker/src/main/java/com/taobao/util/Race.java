package com.taobao.util;

import static com.taobao.util.NamedThreadCallable.named;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link Race}
 * 
 * @author <a href=mailto:zhong.lunfu@gmail.com>zhongl</a>
 * @created 2010-11-11
 * 
 */
public final class Race {

  private Race() {}

  public static final void hint(final boolean barried) {
    Race.barried = barried;
  }

  public static final void hint(final long timeout, final TimeUnit unit) {
    Race.timeout = timeout;
    Race.unit = unit;
  }

  public static final <T> List<Future<T>> parallel(final Callable<T> task, final int num) throws Exception {
    final ExecutorService executor = newFixedThreadPool(num);
    final List<Future<T>> futures = new ArrayList<Future<T>>(num);

    if (barried) barrier = new CyclicBarrier(num);

    final String name = task.toString();
    for (int i = 0; i < num; i++) {
      final Callable<T> callable = barried ? barried(task) : task;
      futures.add(executor.submit(named(callable, name + "-" + i)));
    }
    try {
      get(futures);
      return futures;
    } finally {
      shutdown(executor);
    }
  }

  public static final <T> List<Future<T>> run(final Callable<T>... tasks) throws Exception {
    final int size = tasks.length;
    final ExecutorService executor = newFixedThreadPool(size);
    final List<Future<T>> futures = new ArrayList<Future<T>>(size);
    
    if (barried) barrier = new CyclicBarrier(size);

    for (int i = 0; i < size; i++) {
      final String name = tasks[i].toString();
      final Callable<T> callable = barried ? barried(tasks[i]) : tasks[i];
      futures.add(executor.submit(named(callable, name)));
    }

    try {
      get(futures);
      return futures;
    } finally {
      shutdown(executor);
    }
  }

  private static <T> Callable<T> barried(final Callable<T> task) {
    return new Callable<T>() {

      @Override
      public T call() throws Exception {
        barrier.await();
        return task.call();
      }
    };
  }

  private static <T> void get(final List<Future<T>> futures) throws InterruptedException,
                                                            ExecutionException {
    final int size = futures.size();
    final BitSet bitSet = new BitSet(size);
    for (int i = 0; bitSet.cardinality() < size; i++) {
      i = i % size;
      try {
        futures.get(i).get(timeout, unit);
      } catch (final TimeoutException e) {
        continue;
      }
      bitSet.set(i);
    }
  }

  private static final void shutdown(final ExecutorService executor) {
    executor.shutdownNow();
  }

  private static boolean barried;

  private static CyclicBarrier barrier;

  private static long timeout = 100L;

  private static TimeUnit unit = TimeUnit.MILLISECONDS;

}
