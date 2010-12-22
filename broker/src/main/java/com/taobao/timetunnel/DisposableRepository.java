package com.taobao.timetunnel;

import java.util.concurrent.FutureTask;

import com.taobao.util.Repository;

/**
 * {@link DisposableRepository}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-16
 * 
 */
public abstract class DisposableRepository<K, T extends Disposable> extends Repository<K, T>
    implements Disposable {

  public DisposableRepository(final Factory<K, T> factory) {
    super(factory);
  }

  @Override
  public void dispose() {
    try {
      for (final FutureTask<T> futureTask : map.values()) {
        futureTask.get().dispose();
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    } finally {
      map.clear();
    }
  }

}
