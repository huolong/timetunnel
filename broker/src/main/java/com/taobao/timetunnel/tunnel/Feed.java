package com.taobao.timetunnel.tunnel;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.Dumpable;
import com.taobao.timetunnel.message.Message;

/**
 * @{link Feed}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
interface Feed<Content> extends Disposable, Dumpable<Content> {
  Cursor<Message<Content>> cursorOf(Object key);

  void post(Message<Content> message);

  int size();

  int trim();
}
