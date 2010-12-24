package com.taobao.timetunnel.reliable;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import com.taobao.timetunnel.Appendable;
import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.DisposableRepository;
import com.taobao.timetunnel.Dumpable;
import com.taobao.timetunnel.message.ByteBufferMessageFactory;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.message.MessageFactory;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.swap.ByteBufferFreezers;
import com.taobao.util.Repository.Factory;

/**
 * {@link ReliableFactory}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public class ByteBufferMessageReliables implements Disposable, Dumpable<ByteBuffer> {

  public ByteBufferMessageReliables(final File home, final int chunkCapacity, final int chunkBuffer) {
    freezers = new ByteBufferFreezers(home, chunkCapacity, chunkBuffer);
  }

  @Override
  public void dispose() {
    reliables.dispose();
    freezers.dispose();
  }

  @Override
  public void dumpTo(final Appendable<ByteBuffer> appendable) {
    reliables.dumpTo(appendable);
  }

  public Reliable<ByteBuffer> reliable(final Category category) {
    try {
      return reliables.getOrCreateIfNotExist(category);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final Factory<Category, Reliable<ByteBuffer>> factory =
    new Factory<Category, Reliable<ByteBuffer>>() {

      @Override
      public Reliable<ByteBuffer> newInstance(final Category category) {
        return new ByteBufferMessageReliable(category);
      }
    };

  private final Reliables reliables = new Reliables();
  private final ByteBufferFreezers freezers;

  /**
   * @{link ByteBufferMessageReliable}
   */
  private final class ByteBufferMessageReliable implements Reliable<ByteBuffer> {

    ByteBufferMessageReliable(final Category category) {
      this.category = category;
    }

    @Override
    public void copy(final Session session, final ByteBuffer content) {
      offer(message(content, session), copies);
    }

    @Override
    public void dispose() {
      dumps.clear();
      copies.clear();
    }

    @Override
    public void dump(final Session session, final ByteBuffer content) {
      offer(message(content, session), dumps);
    }

    @Override
    public void dumpTo(final Appendable<ByteBuffer> appendable) {
      dump(dumps, appendable);
      dump(copies, appendable);
    }

    @Override
    public void trim(final Session session, final int size) {
      try {
        for (int i = 0; i < size; i++) {
          if (!dumps.isEmpty()) {
            dumps.poll().dispose();
            continue;
          }
          if (!copies.isEmpty()) {
            copies.poll().dispose();
            continue;
          }
          break;
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    private void dump(final Queue<Message<ByteBuffer>> queue,
                      final com.taobao.timetunnel.Appendable<ByteBuffer> appendable) {
      while (!queue.isEmpty()) {
        final Message<ByteBuffer> message = queue.poll();
        if (message.isExpired() || message.isUseless()) continue;
        ByteBuffer content = message.content();
        appendable.append(category, message.publisher(), content);
      }
    }

    private Message<ByteBuffer> message(final ByteBuffer content, final Session publisher) {
      return messageFactory.createBy(content, category, publisher);
    }

    private void offer(final Message<ByteBuffer> message, final Queue<Message<ByteBuffer>> queue) {
      message.freezeBy(freezers.freezer(category));
      queue.offer(message);
    }

    private final MessageFactory<ByteBuffer> messageFactory = new ByteBufferMessageFactory();
    private final Queue<Message<ByteBuffer>> copies =
      new ConcurrentLinkedQueue<Message<ByteBuffer>>();
    private final Queue<Message<ByteBuffer>> dumps =
      new ConcurrentLinkedQueue<Message<ByteBuffer>>();

    private final Category category;

  }

  private final class Reliables extends DisposableRepository<Category, Reliable<ByteBuffer>>
      implements Dumpable<ByteBuffer> {

    public Reliables() {
      super(factory);
    }

    @Override
    public void dumpTo(final Appendable<ByteBuffer> appendable) {
      try {
        for (final FutureTask<Reliable<ByteBuffer>> task : map.values()) {
          task.get().dumpTo(appendable);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }

    }

  }

}
