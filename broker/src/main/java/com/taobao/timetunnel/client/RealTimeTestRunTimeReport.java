package com.taobao.timetunnel.client;

import static java.text.MessageFormat.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.text.MessageFormat;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.util.Events;
import com.taobao.util.Repository;
import com.taobao.util.SystemTime;

/**
 * {@link RealTimeTestRunTimeReport}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-2
 * 
 */
final class RealTimeTestRunTimeReport implements TestRuntimeReport {

  /**
   * @param standard millis
   */
  RealTimeTestRunTimeReport(final long standard, final int printPeriod) {
    partition = new Partition(standard, 10);
    Events.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        System.out.println(RealTimeTestRunTimeReport.this);
      }
    }, printPeriod, TimeUnit.SECONDS);
  }

  @Override
  public Counter counterOf(final Exception e) {
    String key = null;
    if (e instanceof Failure) {
      key = ((Failure) e).reason;
    }else {
      key = e.getClass().getSimpleName();
    }
    if (!failureCounters.exist(key)) e.printStackTrace();
    return failureCounters.uncheckedGetOrCreateIfNoExist(key);
  }

  @Override
  public Counter counterOfSuccess() {
    return successCounter;
  }

  @Override
  public void hit(final long elaspe) {
    partition.count(NANOSECONDS.toMillis(elaspe));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(MessageFormat.format("{0, date} {0, time}", SystemTime.current()))
      .append(LINE_DELIMITER);
    sb.append(partition).append(LINE_DELIMITER);
    sb.append("Counters : ").append(LINE_DELIMITER);
    if (failureCounters.isNotEmpty()) sb.append(failureCounters).append(LINE_DELIMITER);
    sb.append(successCounter).append(LINE_DELIMITER);
    sb.append(messagesCounter).append(LINE_DELIMITER);
    return sb.toString();
  }

  private static final String LINE_DELIMITER = "\n";

  private final FailureCounters failureCounters = new FailureCounters();
  private final InnerCounter successCounter = new InnerCounter("success");
  private final InnerCounter messagesCounter = new InnerCounter("messages");
  private final Partition partition;

  /**
   * {@link FailureCounters}
   */
  private final class FailureCounters extends Repository<String, Counter> {

    public FailureCounters() {
      super(new Factory<String, Counter>() {

        @Override
        public Counter newInstance(final String reason) {
          return new InnerCounter(reason);
        }
      });
    }

    public boolean exist(final String key) {
      return map.contains(key);
    }

    public boolean isNotEmpty() {
      return !map.isEmpty();
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      try {
        for (final FutureTask<Counter> task : map.values()) {
          sb.append(task.get()).append(LINE_DELIMITER);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
      return sb.toString();
    }
  }

  /**
   * {@link InnerCounter}
   */
  private final class InnerCounter implements Counter {

    public InnerCounter(final String name) {
      this.name = name;
      count = new AtomicInteger();
    }

    @Override
    public void increment() {
      count.incrementAndGet();
    }

    @Override
    public String toString() {
      return MessageFormat.format("\t{0} appeared {1} times", name, count);
    }

    private final String name;
    private final AtomicInteger count;

    @Override
    public void add(int size) {
      count.addAndGet(size);
    }

  }

  /**
   * {@link Partition}
   */
  private static class Partition {
    public Partition(final long max, final int division) {
      this.max = max;
      this.division = division;
      ps = new AtomicInteger[division + 1];
      for (int i = 0; i < ps.length; i++) {
        ps[i] = new AtomicInteger();
      }
    }

    public void count(final long num) {
      if (num > max) {
        ps[division].incrementAndGet();
        return;
      }

      final long s = max / 10;
      int x = division + 1;
      int i = 0;
      int q = (x - i) / 2;

      for (;;) {
        if (q * s > num) {
          if ((q - 1) * s <= num) {
            q = q - 1;
            break;
          }
          x = q;
          q -= (x - i) / 2 + 1;
        } else {
          if ((q + 1) * s >= num) break;
          i = q;
          q += (x - i) / 2 + 1;
        }
      }
      ps[q].incrementAndGet();
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("Partisions : \n");
      for (int i = 0; i < ps.length - 1; i++) {
        sb.append(format(pattern, i * max / 10, (i + 1) * max / 10, ps[i].get()));
      }
      sb.append(format(pattern, max, "!!!!", ps[10].get()));
      return sb.toString();
    }

    private final int division;
    private final AtomicInteger[] ps;
    private final long max;
    private final String pattern = "\t{0}~{1} \t\t{2}\n";
  }

  @Override
  public Counter counterOfMessage() {
    return messagesCounter;
  }

}
