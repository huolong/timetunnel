package com.taobao.timetunnel.tunnel;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.Appendable;
import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.DisposableRepository;
import com.taobao.timetunnel.Dumpable;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.message.MessageFactory;
import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.tunnel.MoveSynchronizer.Tracker;
import com.taobao.util.Events;
import com.taobao.util.Repository;
import com.taobao.util.Repository.Factory;

/**
 * {@link ByteBufferMessageTunnels}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-30
 * 
 */
public class ByteBufferMessageTunnels implements Disposable, Dumpable<ByteBuffer> {

  public ByteBufferMessageTunnels(final TrimListener listener,
                                  final int syncPoint,
                                  final MessageFactory<ByteBuffer> messageFactory) {
    super();
    this.listener = listener;
    this.syncPoint = syncPoint;
    this.messageFactory = messageFactory;
  }

  @Override
  public void dispose() {
    if (!disposed.compareAndSet(false, true)) return;
    tunnels.dispose();
  }

  @Override
  public void dumpTo(final Appendable<ByteBuffer> appendable) {
    tunnels.dumpTo(appendable);
  }

  public Tunnel<ByteBuffer> tunnel(final Category category) {
    if (disposed.get())
      throw new IllegalStateException("Should not get tunnel after tunnels disposed.");
    try {
      return tunnels.getOrCreateIfNotExist(category);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferMessageTunnels.class);

  private final Factory<Category, Tunnel<ByteBuffer>> factory =
    new Factory<Category, Tunnel<ByteBuffer>>() {

      @Override
      public Tunnel<ByteBuffer> newInstance(final Category category) {
        return new ByteBufferMessageTunnel(category);
      }
    };

  private final AtomicBoolean disposed = new AtomicBoolean();
  private final Tunnels tunnels = new Tunnels();
  private final MessageFactory<ByteBuffer> messageFactory;
  private final TrimListener listener;
  private final int syncPoint;

  /**
   * {@link ByteBufferMessageTunnel}
   */
  private final class ByteBufferMessageTunnel implements Tunnel<ByteBuffer>, Watchable<ByteBuffer> {

    public ByteBufferMessageTunnel(final Category category) {
      this.category = category;
      feed = new ConcurrentFeed<ByteBuffer>(category.name());
      watchers = new ByteBufferMessageWatchers(this);
      LOGGER.debug("{} created.", this);
    }

    @Override
    public List<ByteBuffer> ackAndGet(final Session session) {
      return watchers.watcher(session).ackAndGet();
    }

    @Override
    public Category category() {
      return category;
    }

    @Override
    public void dispose() {
      feed.dispose();
      groups.dispose();
      watchers.dispose();
      LOGGER.debug("{} disposed.", this);
    }

    @Override
    public void dumpTo(final Appendable<ByteBuffer> appendable) {
      feed.dumpTo(appendable);
    }

    @Override
    public Group<ByteBuffer> groupOf(final Watcher<ByteBuffer> watcher) {
      return watchGroup(watcher.session());
    }

    @Override
    public void onAvaliable(final Watcher<ByteBuffer> watcher) {
      final Session session = watcher.session();
      watcher.onMessageReceived(watchGroup(session).cursorOf(session));
    }

    @Override
    public void onHasUselessMessages(final Watcher<ByteBuffer> watcher) {
      if (trimming.compareAndSet(false, true)) Events.submit(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          LOGGER.debug("Start to trim {}.", feed);
          final int trim = feed.trim();
          LOGGER.debug("Trimed {} messages of {}.", trim, feed);
          listener.onTrim(category, watcher.session(), trim);
          trimming.compareAndSet(true, false);
          return null;
        }
      });
    }

    @Override
    public void post(final Session session, final ByteBuffer message) {
      feed.post(message(session, message));
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("ByteBufferMessageTunnel [category=").append(category).append("]");
      return builder.toString();
    }

    private Message<ByteBuffer> message(final Session session, final ByteBuffer message) {
      return messageFactory.createBy(message, category, session);
    }

    private WatcherGroup watchGroup(final Session session) {
      try {
        return groups.getOrCreateIfNotExist(session.stringValueOf(Attribute.subscriber));
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    private final Groups groups = new Groups();
    private final Category category;
    private final Feed<ByteBuffer> feed;
    private final ByteBufferMessageWatchers watchers;
    private final AtomicBoolean trimming = new AtomicBoolean();

    /**
     * {@link Groups}
     */
    private final class Groups extends DisposableRepository<String, WatcherGroup> {

      public Groups() {
        super(new Factory<String, WatcherGroup>() {

          @Override
          public WatcherGroup newInstance(final String key) {
            return new WatcherGroup(key);
          }
        });
      }

    }

    /**
     * {@link WatcherGroup}
     */
    private final class WatcherGroup implements Group<ByteBuffer> {

      public WatcherGroup(final String key) {
        this.key = key;
        reflux = new ConcurrentLinkedQueue<Message<ByteBuffer>>();
        moveSynchronizer = MoveSynchronizers.moveSynchronizer(syncPoint);
      }

      public Cursor<Message<ByteBuffer>> cursorOf(final Session session) {
        try {
          return cursors.getOrCreateIfNotExist(session);
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void dispose() {
        cursors.dispose();
        reflux.clear();
      }

      @Override
      public void reclaim(final Session session, final List<Message<ByteBuffer>> reflux) {
        cursors.remove(session);
        this.reflux.addAll(reflux);
      }

      private final String key;
      private final Queue<Message<ByteBuffer>> reflux;
      private final Cursors cursors = new Cursors();
      private final MoveSynchronizer<Session> moveSynchronizer;

      /**
       * {@link Cursors}
       */
      private final class Cursors extends Repository<Session, InnerCursor> implements Disposable {

        public Cursors() {
          super(new Factory<Session, InnerCursor>() {

            @Override
            public InnerCursor newInstance(final Session session) {
              final InnerCursor cursor = new InnerCursor(session);
              return cursor;
            }
          });
        }

        @Override
        public void dispose() {
          map.clear();
        }

        void remove(final Session session) {
          FutureTask<InnerCursor> task = map.remove(session);
          try {
            if (task != null) task.get().tracker.discard();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }

      }

      /**
       * {@link InnerCursor}
       */
      private final class InnerCursor implements Cursor<Message<ByteBuffer>> {
        private final Tracker tracker;

        public InnerCursor(final Session session) {
          this.session = session;
          tracker = moveSynchronizer.tracker(session);
        }

        @Override
        public synchronized Message<ByteBuffer> next() {
          try {
            tracker.next();
          } catch (InterruptedException e) {
            LOGGER.error("Interrupt tracker of {}", session);
            Thread.currentThread().interrupt();
          }
          Message<ByteBuffer> message = reflux.poll();
          if (message == null) message = feed.cursorOf(key).next();
          if (message == null) return message;
          return message;
        }

        private final Session session;
      }

    }

  }

  /**
   * {@link Tunnels}
   */
  private final class Tunnels extends DisposableRepository<Category, Tunnel<ByteBuffer>> implements
      Dumpable<ByteBuffer> {

    public Tunnels() {
      super(factory);
    }

    @Override
    public void dumpTo(final Appendable<ByteBuffer> appendable) {
      try {
        for (final FutureTask<Tunnel<ByteBuffer>> futureTask : map.values()) {
          futureTask.get().dumpTo(appendable);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

  }
}
