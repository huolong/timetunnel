package com.taobao.timetunnel.tunnel;

/**
 * @{link Cursor}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
interface Cursor<Message> {
  Message next();
}
