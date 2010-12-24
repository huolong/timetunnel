package com.taobao.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * {@link Repository}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-4
 * 
 */
public abstract class Repository<K, V> {
  public Repository(final Factory<K, V> factory) {
    this.factory = factory;
  }

  /**
   * @param key
   * @return
   * @throws ExecutionException if lazy new instance has error.
   * @throws InterruptedException if current thread is interrupted during newing
   *           instance.
   */
  public final V getOrCreateIfNotExist(final K key) throws InterruptedException, ExecutionException {
    final Future<V> future = map.get(key);
    if (future != null) return future.get();

    final FutureTask<V> newCreator = newLazyCreator(key);
    final FutureTask<V> oldCreator = map.putIfAbsent(key, newCreator);

    if (oldCreator == null) {
      newCreator.run();
      return newCreator.get();
    }
    oldCreator.run();
    return oldCreator.get();
  }

  /**
   * @param key
   * @return
   */
  public final V uncheckedGetOrCreateIfNoExist(final K key) {
    try {
      return getOrCreateIfNotExist(key);
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    } catch (final ExecutionException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      throw new RuntimeException(cause);
    }
  }

  private final FutureTask<V> newLazyCreator(final K key) {
    return new FutureTask<V>(new Callable<V>() {

      @Override
      public V call() throws Exception {
        return factory.newInstance(key);
      }
    });
  }

  private final Factory<K, V> factory;

  protected final ConcurrentHashMap<K, FutureTask<V>> map =
    new ConcurrentHashMap<K, FutureTask<V>>();

  public static interface Factory<K, V> {
    V newInstance(K key);
  }

}
