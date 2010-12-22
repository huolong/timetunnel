package com.taobao.timetunnel.reliable;

import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;

/**
 * {@link ReliableService}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public interface ReliableService<Message> {

  /**
   * @param category
   * @param session
   * @param message
   */
  void copy(final Category category, final Session session, final Message message);

  /**
   * @param category
   * @param session
   * @param message
   */
  void dump(final Category category, final Session session, final Message message);

  /**
   * @param category
   * @param session
   * @param size
   */
  void trim(final Category category, final Session session, final int size);

}
