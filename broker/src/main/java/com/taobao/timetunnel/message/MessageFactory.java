package com.taobao.timetunnel.message;

import com.taobao.timetunnel.session.Session;

/**
 * @{link MessageFactory}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
public interface MessageFactory<Content> {
  Message<Content> createBy(Content content, Category category, Session publisher);
}
