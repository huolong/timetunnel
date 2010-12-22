package com.taobao.timetunnel.swap;

/**
 * {@link Point} is a reference of T.
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
public interface Point<T> {
  T get();

  void clear();
}
