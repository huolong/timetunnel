package com.taobao.timetunnel.tunnel;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.DisposableRepository;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.session.Session.InvalidListener;
import com.taobao.util.Repository.Factory;

/**
 * {@link ByteBufferMessageWatchers}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-29
 * 
 */
final class ByteBufferMessageWatchers implements Disposable {
  public ByteBufferMessageWatchers(final Watchable<ByteBuffer> watchable) {
    this.watchable = watchable;
  }

  @Override
  public void dispose() {
    watchers.dispose();
  }

  public Watcher<ByteBuffer> watcher(final Session session) {
    try {
      return watchers.getOrCreateIfNotExist(session);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void dump(final Queue<Message<ByteBuffer>> queue,
                           final List<Message<ByteBuffer>> reflux) {
    while (!queue.isEmpty())
      reflux.add(queue.poll());
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferMessageWatchers.class);

  /** ByteBufferMessageWatcher factory */
  private final Factory<Session, Watcher<ByteBuffer>> factory =
    new Factory<Session, Watcher<ByteBuffer>>() {

      @Override
      public Watcher<ByteBuffer> newInstance(final Session session) {
        return new ByteBufferMessageWatcher(session);
      }
    };

  private final Watchers watchers = new Watchers();
  private final Watchable<ByteBuffer> watchable;

  /**
   * @{link ByteBufferMessageWatcher}
   */
  private final class ByteBufferMessageWatcher implements Watcher<ByteBuffer>, InvalidListener {

    public ByteBufferMessageWatcher(final Session session) {
      this.session = session;
      capacity = session.intValueOf(Attribute.receiveWindowSize);
      waittings = new LinkedList<Message<ByteBuffer>>();
      pendings = new LinkedList<Message<ByteBuffer>>();
      group = watchable.groupOf(this);
      watchable.onAvaliable(this);
      session.add(this);
      LOGGER.debug("{} created.", this);
    }

    @Override
    public synchronized List<ByteBuffer> ackAndGet() {
      if (disposed) return Collections.emptyList();
      ack();
      return get();
    }

    @Override
    public synchronized void dispose() {
      if (disposed) return;
      watchers.remove(session);
      session.remove(this);
      group.reclaim(session, reflux());
      disposed = true;
      LOGGER.debug("{} disposed.", this);
    }

    @Override
    public void onInvalid() {
      dispose();
    }

    @Override
    public synchronized void onMessageReceived(final Cursor<Message<ByteBuffer>> cursor) {
      if (disposed) return;
      final int begin = size;
      while (size < capacity) {
        final Message<ByteBuffer> message = cursor.next();
        if (message == null) break;
        if (message.isExpired() || message.isUseless()) continue;
        waittings.add(message);
        size++;
      }
      final int end = size;
      LOGGER.debug("{} wait {} messages.", this, end - begin);
    }

    @Override
    public Session session() {
      return session;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("ByteBufferMessageWatcher [session=")
             .append(session)
             .append(", pendings=")
             .append(pendings.size())
             .append(", waittings=")
             .append(waittings.size())
             .append(", size=")
             .append(size)
             .append("]");
      return builder.toString();
    }

    private void ack() {
      final int begin = size;
      while (!pendings.isEmpty()) {
        final Message<ByteBuffer> message = pendings.poll();
        message.readBy(session);
        if (message.isUseless()) watchable.onHasUselessMessages(this);
        size--;
      }
      final int end = size;
      LOGGER.debug("{} acked {} messages.", this, begin - end);
      watchable.onAvaliable(this);
    }

    private List<ByteBuffer> get() {
      final List<Message<ByteBuffer>> sendings = waittings;
      waittings = new LinkedList<Message<ByteBuffer>>();
      pendings.addAll(sendings);
      LOGGER.debug("{} got and pendent {} messages.", this, sendings.size());
      return new AbstractList<ByteBuffer>() {

        @Override
        public ByteBuffer get(final int index) {
          final Message<ByteBuffer> message = sendings.get(index);
          try {
            ByteBuffer content = message.content();
            return content;
          } catch (final Exception e) {
            /* message disposed or expired. */
            LOGGER.error("Unexpect Error in getting messages", e);
            return null;
          }
        }

        @Override
        public int size() {
          return sendings.size();
        }
      };
    }

    private List<Message<ByteBuffer>> reflux() {
      final List<Message<ByteBuffer>> reflux = new LinkedList<Message<ByteBuffer>>();
      dump(pendings, reflux);
      dump(waittings, reflux);
      return reflux;
    }

    private int size = 0;
    private boolean disposed;
    private LinkedList<Message<ByteBuffer>> waittings;

    private final LinkedList<Message<ByteBuffer>> pendings;
    private final Session session;
    private final int capacity;
    private final Group<ByteBuffer> group;

  }

  /**
   * @{link Watchers}
   */
  private final class Watchers extends DisposableRepository<Session, Watcher<ByteBuffer>> {

    public Watchers() {
      super(factory);
    }

    void remove(final Session session) {
      map.remove(session);
    }

  }

}
