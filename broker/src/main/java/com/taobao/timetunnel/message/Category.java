package com.taobao.timetunnel.message;

import java.util.Set;

/**
 * @{link Category}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-27
 * 
 */
public interface Category {
  boolean isInvaildSubscriber(String key);

  boolean isMessageExpiredAfter(long created);

  boolean isMessageUselessReadBy(Set<String> readers);

  String name();
}
