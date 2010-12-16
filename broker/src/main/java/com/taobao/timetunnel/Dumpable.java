package com.taobao.timetunnel;

/**
 * @{link Dumpable}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-28
 * 
 */
public interface Dumpable<Message> {
  /**
   * @param appendable
   */
  void dumpTo(Appendable<Message> appendable);

}
