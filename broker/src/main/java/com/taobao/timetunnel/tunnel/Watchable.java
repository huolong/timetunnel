package com.taobao.timetunnel.tunnel;


/**
 * {@link Watchable}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-29
 * 
 */
interface Watchable<Content> {

  /**
   * @param watcher
   * @return
   */
  Group<Content> groupOf(Watcher<Content> watcher);

  /**
   * Invoke it if watcher is avaliable.
   * 
   * @param watcher
   */
  void onAvaliable(Watcher<Content> watcher);

  /**
   * Invoke it if has useless messages.
   */
  void onHasUselessMessages(Watcher<Content> watcher);
}
