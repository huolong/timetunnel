package com.taobao.timetunnel.tunnel;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.taobao.timetunnel.tunnel.MoveSynchronizer.Tracker;
import com.taobao.util.Race;

/**
 * {@link MoveSynchronizerTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-3
 * 
 */
public class MoveSynchronizerTest {
  @Test
  @SuppressWarnings("unchecked")
  public void shouldSynchronizedWalk() throws Exception {
    final int value = 10;
    final MoveSynchronizer<String> synchronizer = MoveSynchronizers.moveSynchronizer(value);

    final List<Future<Integer>> futures =
      Race.run(new Person(synchronizer.tracker("0")),
               new Person(synchronizer.tracker("1")),
               new Callable<Integer>() {

                 @Override
                 public Integer call() throws Exception {
                   Thread.sleep(100L);
                   running.compareAndSet(true, false);
                   return 0;
                 }
               });
    final int steps0 = futures.get(0).get();
    final int steps1 = futures.get(1).get();
    assertThat(Math.abs(steps1 - steps0), lessThanOrEqualTo(value));
  }

  public final AtomicBoolean running = new AtomicBoolean(true);

  /**
   * {@link Person}
   */
  private final class Person implements Callable<Integer> {
    public Person(final Tracker move) {
      this.move = move;
    }

    @Override
    public Integer call() throws Exception {
      int steps = 0;
      while (running.get()) {
        walk();
        steps++;
      }
      move.discard();
      return steps;
    }

    void walk() {
      try {
        move.next();
      } catch (final InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
      }
    }

    private final Tracker move;
  }
}
