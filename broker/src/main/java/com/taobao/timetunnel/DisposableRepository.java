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
    boolean interrupted = false;
    for (final FutureTask<T> futureTask : map.values()) {
      try {
        futureTask.get().dispose();
      } catch (final InterruptedException e) {
        interrupted = true;
      } catch (final Exception e) {
        continue;
      }
    }
    map.clear();
    if (interrupted) Thread.currentThread().interrupt();
  }

}
