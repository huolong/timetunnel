package com.taobao.timetunnel.tunnel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link MoveSynchronizers}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-3
 * 
 */
public final class MoveSynchronizers {
  /**
   * @param valume of synchronized error.
   * @return {@link MoveSynchronizer}
   */
  public static <T> MoveSynchronizer<T> moveSynchronizer(final int valume) {
    return new Synchronizer<T>(valume);
  }

  private static final class Synchronizer<T> implements MoveSynchronizer<T> {
    private Synchronizer(final int valume) {
      this.valume = valume;
      lock = new ReentrantLock();
      condition = lock.newCondition();
    }

    @Override
    public Tracker tracker(final T name) {
      lock.lock();
      try {
        ++trackers;
      } finally {
        lock.unlock();
      }

      return new Tracker() {

        @Override
        public void discard() {
          if (!discarded.compareAndSet(false, true)) return;

          lock.lock();
          try {
            if (awaittingTrackers < --trackers) return;
            resetAndSignalNextRound();
          } finally {
            lock.unlock();
          }
        }

        @Override
        public void next() throws InterruptedException {
          if (discarded.get())
            throw new IllegalStateException("Can't next move after interrupted.");

          if (count.incrementAndGet() < valume) return;

          lock.lock();
          try {
            if (allTracksReachValume()) resetAndSignalNextRound();
            else waitForNextRound();
            count.set(0);
          } finally {
            lock.unlock();
          }
        }

        private boolean allTracksReachValume() {
          return ++awaittingTrackers >= trackers;
        }

        private void resetAndSignalNextRound() {
          awaittingTrackers = 0;
          signalAllState = !signalAllState;
          condition.signalAll();
        }

        private void waitForNextRound() throws InterruptedException {
          final boolean state = signalAllState;
          while (state == signalAllState)
            condition.await();
        }

        private final AtomicInteger count = new AtomicInteger();
        private final AtomicBoolean discarded = new AtomicBoolean();
      };
    }

    private final Lock lock;
    private final int valume;
    private final Condition condition;

    private int trackers = 0;
    private int awaittingTrackers = 0;
    private boolean signalAllState = false;
  }

}
