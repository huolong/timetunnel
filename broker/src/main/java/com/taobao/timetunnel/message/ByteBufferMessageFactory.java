package com.taobao.timetunnel.message;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.swap.Freezer;
import com.taobao.timetunnel.swap.Point;
import com.taobao.util.SystemTime;

/**
 * @{link DefaultMessageFactory}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-28
 * 
 */
public final class ByteBufferMessageFactory implements MessageFactory<ByteBuffer> {

  @Override
  public Message<ByteBuffer> createBy(final ByteBuffer content,
                                      final Category category,
                                      final Session publisher) {
    return new ByteBufferMessage(content, category, publisher);
  }

  /**
   * {@link ByteBufferMessage}
   */
  private static final class ByteBufferMessage implements Message<ByteBuffer> {
    public ByteBufferMessage(final ByteBuffer content,
                             final Category category,
                             final Session publisher) {
      this.content = content;
      this.category = category;
      this.publisher = publisher;
      created = SystemTime.current();
      readers = Collections.synchronizedSet(new HashSet<String>());
      if (LOGGER.isDebugEnabled()) contentDigest = md5(content());
      LOGGER.debug("{} created.", this);
    }

    @Override
    public Category category() {
      return category;
    }

    @Override
    public ByteBuffer content() {
      lock.readLock().lock();
      try {
        if (disposed) throw new IllegalStateException(this + " has been disposed.");
        if (isExpired()) throw new IllegalStateException(this + " has been expired.");
        if (isUseless()) throw new IllegalStateException(this + " has been useless.");
        if (content != null) return content.duplicate();
        if (point != null) return point.get().duplicate();

        /*
         * This case should not appear.
         */
        throw new IllegalStateException(this + " has not been initialized???");
      } finally {
        lock.readLock().unlock();
      }
    }

    @Override
    public long created() {
      return created;
    }

    @Override
    public void dispose() {
      lock.writeLock().lock();
      try {
        if (disposed) return;
        content = null;
        if (point != null) {
          point.clear();
          point = null;
        }
        disposed = true;
        LOGGER.debug("{} disposed.", this);
      } finally {
        lock.writeLock().unlock();
      }
    }

    @Override
    public void freezeBy(final Freezer<ByteBuffer> freezer) {
      lock.writeLock().lock();
      try {
        if (disposed || content == null) return; // disposed or frozen.
        point = freezer.freeze(content);
        content = null;
      } finally {
        lock.writeLock().unlock();
      }
    }

    @Override
    public boolean isExpired() {
      return category.isMessageExpiredAfter(created);
    }

    @Override
    public boolean isUseless() {
      return category.isMessageUselessReadBy(readers);
    }

    @Override
    public Session publisher() {
      return publisher;
    }

    @Override
    public void readBy(final Session subscriber) {
      final String sub = subscriber.stringValueOf(Attribute.subscriber);
      LOGGER.debug("{} read by {}", this, sub);
      readers.add(sub);
    }

    @Override
    public Set<String> subscribers() {
      return Collections.unmodifiableSet(readers);
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder.append("ByteBufferMessage [category=")
             .append(category())
             .append(", content=")
             .append(contentDigest)
             .append(", created=")
             .append(created())
             .append(", isExpired=")
             .append(isExpired())
             .append(", isUseless=")
             .append(isUseless())
             .append(", publisher=")
             .append(publisher())
             .append(", subscribers=")
             .append(subscribers())
             .append("]");
      return builder.toString();
    }

    private static String md5(final ByteBuffer content) {
      final byte[] bytes = new byte[content.remaining()];
      content.duplicate().get(bytes);
      return DigestUtils.md5Hex(bytes);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferMessage.class);

    private final Category category;
    private final Session publisher;
    private final long created;
    private final Set<String> readers;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private ByteBuffer content;
    private Point<ByteBuffer> point;
    private String contentDigest = "";
    private boolean disposed = false;
  }
}
