package com.taobao.timetunnel.message;

import com.taobao.timetunnel.swap.Freezer;

/**
 * {@link Freezable}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-12
 * 
 */
public interface Freezable<T> {
  /**
   * Should be freeze by only one {@link Freezer}.
   * 
   * @param freezer
   */
  void freezeBy(Freezer<T> freezer);

}
