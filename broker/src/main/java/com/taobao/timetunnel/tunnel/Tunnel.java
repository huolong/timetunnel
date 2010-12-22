package com.taobao.timetunnel.tunnel;

import java.util.List;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.Dumpable;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;

/**
 * @{link Tunnel}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
public interface Tunnel<Message> extends Disposable, Dumpable<Message> {
  List<Message> ackAndGet(Session session);

  Category category();

  void post(Session session, Message message);
}
