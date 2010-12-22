package com.taobao.timetunnel.tunnel;

import java.util.List;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.session.Session;

/**
 * @{link Watcher}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
interface Watcher<Content> extends Disposable {
  List<Content> ackAndGet();

  void onMessageReceived(Cursor<Message<Content>> cursor);

  Session session();
}
