package com.taobao.timetunnel.tunnel;

import java.nio.ByteBuffer;
import java.util.List;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.session.Session;

/**
 * @{link Group}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-29
 * 
 */
interface Group<Content> extends Disposable {

  void reclaim(Session session, List<Message<ByteBuffer>> reflux);
}
