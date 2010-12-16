package com.taobao.timetunnel.tunnel;

import java.util.List;

import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;

/**
 * {@link TunnelService}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public interface TunnelService<Message> {

  /**
   * @param category
   * @param client
   * @return
   */
  List<Message> ackAndGet(final Category category, final Session session);

  /**
   * @param category
   * @param client
   * @param message
   */
  void post(final Category category, final Session session, final Message message);

}
