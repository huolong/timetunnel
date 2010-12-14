package com.taobao.timetunnel.reliable;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.Dumpable;
import com.taobao.timetunnel.session.Session;

/**
 * {@link Reliable}
 * 
 * <pre>
 *      | <- message 3 -> | <- message 2 -> | <- message 1  -> |  
 * -------------------------------------------------------------->
 *       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
 * -------------------------------------------------------------->
 *      | <-  copies   -> | <-            dumps             -> |  
 *  
 *  size = 3;
 * 
 * </pre>
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-12
 * 
 */
public interface Reliable<Message> extends Disposable, Dumpable<Message> {
  /**
   * Copy in message from other.
   * 
   * @param session
   * @param message
   */
  void copy(Session session, Message message);

  /**
   * Dump in message from other.
   * 
   * @param session
   * @param message
   */
  void dump(Session session, Message message);

  /**
   * Remove recovers first, and then appends.
   * 
   * @param session
   * @param size
   */
  void trim(Session session, int size);
}
