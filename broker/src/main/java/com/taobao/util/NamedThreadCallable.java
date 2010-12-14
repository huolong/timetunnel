package com.taobao.util;

import static java.lang.Thread.currentThread;

import java.util.concurrent.Callable;

/**
 * {@link NamedThreadCallable} for debug.
 * 
 * @author <a href=mailto:zhong.lunfu@gmail.com>zhongl</a>
 * @created 2010-11-12
 * 
 */
public final class NamedThreadCallable<V> implements Callable<V> {

  public static final <V> Callable<V> named(final Callable<V> callable, final String name) {
    return new NamedThreadCallable<V>(callable, name);
  }

  private NamedThreadCallable(final Callable<V> callable, final String name) {
    this.callable = callable;
    this.name = name;
  }

  @Override
  public V call() throws Exception {
    final String old = currentThread().getName();
    currentThread().setName(name);
    final V result = callable.call();
    currentThread().setName(old);
    return result;
  }

  private final Callable<V> callable;

  private final String name;

}
