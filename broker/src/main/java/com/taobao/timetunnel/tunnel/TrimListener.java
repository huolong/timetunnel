package com.taobao.timetunnel.tunnel;

import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;

/**
 * {@link TrimListener}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-1
 * 
 */
public interface TrimListener {

  void onTrim(Category category, Session session, int size);

}
