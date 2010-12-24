package com.taobao.timetunnel.message;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.swap.ByteBufferFreezers;
import com.taobao.timetunnel.swap.Freezer;
import com.taobao.util.Events;
import com.taobao.util.MemoryMonitor;

/**
 * {@link ByteBufferMessageCompactor}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-28
 * 
 */
public final class ByteBufferMessageCompactor implements MessageFactory<ByteBuffer> {

  public ByteBufferMessageCompactor(final MemoryMonitor monitor,
                                    final File home,
                                    final int chunkCapacity,
                                    final int chunkBuffer) {
    this.monitor = monitor;
    freezers = new ByteBufferFreezers(home, chunkCapacity, chunkCapacity);
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

      @Override
      public void run() {
        freezers.dispose();
      }
    }, "dispose-ByteBufferMessageCompactor-freezer"));
  }

  @Override
  public Message<ByteBuffer> createBy(final ByteBuffer content,
                                      final Category category,
                                      final Session publisher) {
    if (monitor.isShort() && compacting.compareAndSet(false, true)) Events.enqueue(compactTask);
    final Message<ByteBuffer> message = FACTORY.createBy(content, category, publisher);
    queue.add(message);
    return uselessAware(message);
  }

  private Runnable removeTask(final Message<ByteBuffer> message) {
    return new Runnable() {

      @Override
      public void run() {
        queue.remove(message);
      }
    };
  }

  private Message<ByteBuffer> uselessAware(final Message<ByteBuffer> message) {
    return new UselessAwareMessageDecorator(message);
  }

  private static final MessageFactory<ByteBuffer> FACTORY = new ByteBufferMessageFactory();

  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferMessageCompactor.class);
  private final MemoryMonitor monitor;
  private final ByteBufferFreezers freezers;
  private final Runnable compactTask = new CompactTask();
  private final AtomicBoolean compacting = new AtomicBoolean();
  private final Queue<Message<ByteBuffer>> queue = new ConcurrentLinkedQueue<Message<ByteBuffer>>();

  /**
   * {@link CompactTask}
   */
  private final class CompactTask implements Runnable {

    @Override
    public void run() {
      LOGGER.debug("Start compact messages because memory is in short ({}).", monitor);
      int compacted = 0;

      for (;; compacted++) {
        final Message<ByteBuffer> message = queue.poll();
        if (message == null || monitor.isAbundant()) break;
        if (message.isExpired() || message.isUseless()) message.dispose();
        else message.freezeBy(freezers.freezer(message.category()));
      }

      compacting.compareAndSet(true, false);

      LOGGER.debug("Compacted {} messages and {}.", compacted, monitor);
    }
  }

  /**
   * {@link UselessAwareMessageDecorator}
   */
  private final class UselessAwareMessageDecorator implements Message<ByteBuffer> {
    private UselessAwareMessageDecorator(final Message<ByteBuffer> message) {
      this.message = message;
    }

    @Override
    public Category category() {
      return message.category();
    }

    @Override
    public ByteBuffer content() {
      return message.content();
    }

    @Override
    public long created() {
      return message.created();
    }

    @Override
    public void dispose() {
      message.dispose();
    }

    @Override
    public void freezeBy(final Freezer<ByteBuffer> freezer) {
      message.freezeBy(freezer);
    }

    @Override
    public boolean isExpired() {
      return message.isExpired();
    }

    @Override
    public boolean isUseless() {
      return message.isUseless();
    }

    @Override
    public Session publisher() {
      return message.publisher();
    }

    @Override
    public void readBy(final Session subscriber) {
      message.readBy(subscriber);
      if (isUseless()) Events.enqueue(removeTask(message));
    }

    @Override
    public Set<String> subscribers() {
      return message.subscribers();
    }

    @Override
    public String toString() {
      return message.toString();
    }

    private final Message<ByteBuffer> message;
  }

}
