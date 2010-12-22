package com.taobao.timetunnel;

import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;

/**
 * @{link Appendable}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-28
 * 
 */
public interface Appendable<Message> {
  void append(Category category, Session session, Message message);
}
