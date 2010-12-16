package com.taobao.timetunnel.tunnel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Coordinators}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-14
 * 
 */
public final class Coordinators {
  private Coordinators() {}

  /**
   * Create a new new {@link Coordinator} with valume.
   * 
   * @param valume for coordination.
   * @return a new {@link Coordinator} with valume.
   */
  public static Coordinator coordinator(final int valume) {
    return new InnerCoordinator(valume);
  }

  /**
   * {@link InnerCoordinator}
   */
  private final static class InnerCoordinator implements Coordinator {

    public InnerCoordinator(final int valume) {
      this.valume = valume;
    }

    @Override
    public Track track(final String name) {
      return new InnerTrack(name);
    }

    private void arrived(final InnerTrack track) {
      arriveds.incrementAndGet();
      startNextRoundIfAllTrackArrived();
    }

    private void decrement() {
      tracks.decrementAndGet();
      startNextRoundIfAllTrackArrived();
    }

    private boolean flag() {
      return flag.get();
    }

    private void increment() {
      tracks.incrementAndGet();
    }

    private void reverseFlag() {
      flag.set(!flag.get());
    }

    private void startNextRoundIfAllTrackArrived() {
      final int currentArriveds = arriveds.get();
      if (currentArriveds < tracks.get()) return;
      if (!arriveds.compareAndSet(currentArriveds, 0)) return;
      reverseFlag();
    }

    private final int valume;
    private final AtomicInteger arriveds = new AtomicInteger();
    private final AtomicInteger tracks = new AtomicInteger();
    private final AtomicBoolean flag = new AtomicBoolean();

    /**
     * {@link InnerTrack}
     */
    private final class InnerTrack implements Track {

      public InnerTrack(final String name) {
        this.name = name;
        init();
        increment();
      }

      @Override
      public synchronized void destory() {
        if (destoryed) return;
        destoryed = true;
        decrement();
      }

      @Override
      public synchronized boolean move() {
        if (destoryed) throw new IllegalStateException("Can't move a destoryed track");
        return state.move(this);
      }

      @Override
      public synchronized String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("InnerTrack [name=")
               .append(name)
               .append(", flag=")
               .append(flag)
               .append(", steps=")
               .append(steps)
               .append(", state=")
               .append(state)
               .append(", destoryed=")
               .append(destoryed)
               .append("]");
        return builder.toString();
      }

      private boolean pause() {
        arrived(this);
        state = State.WAITTING;
        return false;
      }

      private boolean canResume() {
        return flag != flag();
      }

      private boolean forward() {
        steps++;
        return true;
      }

      private boolean await() {
        return false;
      }

      private void init() {
        steps = 0;
        flag = flag();
        state = State.RUNNING;
      }

      private boolean isNotArrived() {
        return steps < valume;
      }

      private boolean resume() {
        init();
        return false;
      }

      private final String name;

      private int steps;
      private State state;
      private boolean destoryed;
      private boolean flag;

    }

    /**
     * {@link State}
     */
    private enum State {
      RUNNING {
        @Override
        boolean move(final InnerTrack track) {
          return track.isNotArrived() ? track.forward() : track.pause();
        }
      },
      WAITTING {
        @Override
        boolean move(final InnerTrack track) {
          return track.canResume() ? track.resume() : track.await();
        }
      };

      abstract boolean move(InnerTrack track);
    }

  }
}
