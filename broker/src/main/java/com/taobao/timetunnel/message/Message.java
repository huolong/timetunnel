package com.taobao.timetunnel.message;

import java.util.Set;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.session.Session;

/**
 * @{link Message}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
public interface Message<Content> extends Disposable, Freezable<Content> {

  Category category();

  Content content();

  long created();

  boolean isExpired();

  boolean isUseless();

  Session publisher();

  void readBy(Session subscriber);

  Set<String> subscribers();

}
