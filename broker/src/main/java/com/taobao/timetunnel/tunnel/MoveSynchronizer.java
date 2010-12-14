package com.taobao.timetunnel.tunnel;

/**
 * {@link MoveSynchronizer}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-3
 * 
 */
public interface MoveSynchronizer<T> {

  Tracker tracker(T value);

  /**
   * {@link Tracker}
   */
  public interface Tracker {
    /**
     * Discard this tracker.
     * 
     * <b>Caution</b> : discarded tracker can not next.
     */
    void discard();

    /**
     * Track moves, it will block when synchronizing.
     * 
     * @throws InterruptedException
     * @throws {@link IllegalStateException} if tracker has been discarded.
     */
    void next() throws InterruptedException;
  }
}
