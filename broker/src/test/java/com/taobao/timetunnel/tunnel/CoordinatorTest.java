package com.taobao.timetunnel.tunnel;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.taobao.timetunnel.tunnel.Coordinator.Track;
import com.taobao.util.Race;

/**
 * {@link CoordinatorTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-3
 * 
 */
public class CoordinatorTest {
  @Test
  @SuppressWarnings("unchecked")
  public void shouldSynchronizedWalk() throws Exception {
    final int value = 10;
    final Coordinator coordinator = Coordinators.coordinator(value);

    final List<Future<Integer>> futures =
      Race.run(new Person(coordinator.track("0")),
               new Person(coordinator.track("1")),
               new DelayHaltCaller());
    final int steps0 = futures.get(0).get();
    final int steps1 = futures.get(1).get();
    assertThat(Math.abs(steps1 - steps0), lessThanOrEqualTo(value));
  }

  public final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * {@link DelayHaltCaller}
   */
  private final class DelayHaltCaller implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
      Thread.sleep(100L);
      running.compareAndSet(true, false);
      return 0;
    }
  }

  /**
   * {@link Person}
   */
  private final class Person implements Callable<Integer> {
    public Person(final Track track) {
      this.track = track;
    }

    @Override
    public Integer call() throws Exception {
      int steps = 0;
      while (running.get()) {
        if (canWalk()) steps++;
      }
      track.destory();
      return steps;
    }

    boolean canWalk() {
      return track.move();
    }

    private final Track track;
  }
}
