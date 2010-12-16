package com.taobao.timetunnel.swap;

import com.taobao.timetunnel.Disposable;

/**
 * {@link Freezer}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-12
 * 
 */
public interface Freezer<T> extends Disposable {
  Point<T> freeze(T object);
}
