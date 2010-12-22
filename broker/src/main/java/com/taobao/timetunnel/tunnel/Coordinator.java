package com.taobao.timetunnel.tunnel;

/**
 * {@link Coordinator} balance movings of {@link Track}s.
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-14
 * 
 */
public interface Coordinator {
  /**
   * @param name
   * @return a new {@link Track} with name.
   */
  Track track(String name);

  /**
   * {@link Track} record movings.
   */
  public interface Track {
    /**
     * Destory {@link Track} from {@link Coordinator}.
     */
    void destory();

    /**
     * Move one step.
     * 
     * @return false means current {@link Track} need wait for balance, else
     *         true.
     * @throws IllegalStateException if track destoryed.
     */
    boolean move();
  }
}
