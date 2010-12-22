package com.taobao.timetunnel.tunnel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.Appendable;
import com.taobao.timetunnel.Disposable;
import com.taobao.timetunnel.DisposableRepository;
import com.taobao.timetunnel.message.Message;

/**
 * {@link ConcurrentFeed} based on <a
 * href="http://www.cs.rochester.edu/u/michael/PODC96.html"> Simple, Fast, and
 * Practical Non-Blocking and Blocking Concurrent Queue Algorithms</a> by Maged
 * M. Michael and Michael L. Scott.
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-30
 * 
 */
@SuppressWarnings("rawtypes")
public class ConcurrentFeed<Content> implements Feed<Content> {

  public ConcurrentFeed(final String category) {
    this.category = category;
    head = tail = new Node<Content>(null, null);
    LOGGER.debug("{} created.", this);
  }

  @Override
  public Cursor<Message<Content>> cursorOf(final Object key) {
    try {
      return cursors.getOrCreateIfNotExist(key);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void dispose() {
    if (!disposed.compareAndSet(false, true)) return;
    cursors.dispose();
    checkHead();
    for (;;) {
      final Node<Content> h = head, t = tail, n = h.next;
      final Message<Content> message = h.message;
      if (h == head) { // head is unchanged
        if (h == t) { // queue is empty or tail falling behind
          if (n == null) break; // queue is empty
          casTail(t, n); // tail falling behind, so advance it
        } else {
          message.dispose();
          if (casHead(h, n)) size.decrementAndGet();
          continue;
        }
      }
    }
    LOGGER.debug("{} disposed.", this);
  }

  @Override
  public synchronized void dumpTo(final Appendable<Content> appendable) {
    if (disposed.get()) throw new IllegalStateException("Should not dump feed after it diposed.");
    checkHead();
    int count = 0;
    for (Node<Content> h = head; h != null; h = h.next) {
      final Message<Content> message = h.message;
      if (shouldTrim(message)) continue;
      appendable.append(message.category(), message.publisher(), message.content());
      count++;
    }
    LOGGER.debug("{} dump to {} {} messages.", new Object[] { this, appendable, count });
  }

  @Override
  public void post(final Message<Content> message) {
    if (disposed.get())
      throw new IllegalStateException("Should not post to feed after it diposed.");

    checkNotNull(message);

    final Node<Content> newNode = new Node<Content>(null, message);
    LOGGER.debug("{} has a new post {}.", this, message);
    for (;;) {
      final Node<Content> t = tail, n = t.next;
      if (t == tail) { // tail is unchanged
        if (n == null) { // tail is last node.
          if (t.casNext(null, newNode)) { // append new node
            casTail(t, newNode); // make tail point to new node
            size.incrementAndGet();
            return;
          }
        } else {
          casTail(t, n); // make tail point to tail.next.
        }
      }

    }
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("ConcurrentFeed [category=")
           .append(category)
           .append(", size=")
           .append(size())
           .append("]");
    return builder.toString();
  }

  @Override
  public int trim() {
    checkHead();
    int trim = 0;
    for (;;) {
      final Node<Content> h = head, t = tail, n = h.next;
      final Message<Content> message = h.message;
      if (h == head) { // head is unchanged
        if (h == t) { // queue is empty or tail falling behind
          if (n == null) break; // queue is empty
          casTail(t, n); // tail falling behind, so advance it
        } else {
          if (shouldTrim(message)) {
            message.dispose();
            if (casHead(h, n)) trim++;
            continue;
          } else {
            break;
          }
        }
      }
    }
    size.addAndGet(-trim);
    LOGGER.debug("{} trim {} messages.", this, trim);
    return trim;
  }

  private boolean casHead(final Node<Content> expect, final Node<Content> update) {
    return HEAD_UPDATER.compareAndSet(this, expect, update);
  }

  private boolean casTail(final Node<Content> expect, final Node<Content> update) {
    return TAIL_UPDATER.compareAndSet(this, expect, update);
  }

  private void checkHead() {
    final Node<Content> h = head;
    final Node<Content> n = head.next;
    if (h.message == null && casHead(h, n)) size.decrementAndGet();
  }

  private static void checkNotNull(final Message<?> message) {
    if (message == null) throw new NullPointerException("Message should not be null.");
  }

  private static boolean shouldTrim(final Message<?> message) {
    return message.isExpired() || message.isUseless();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentFeed.class);

  private static final AtomicReferenceFieldUpdater<ConcurrentFeed, Node> HEAD_UPDATER =
    AtomicReferenceFieldUpdater.newUpdater(ConcurrentFeed.class, Node.class, "head");

  private static final AtomicReferenceFieldUpdater<ConcurrentFeed, Node> TAIL_UPDATER =
    AtomicReferenceFieldUpdater.newUpdater(ConcurrentFeed.class, Node.class, "tail");

  private volatile Node<Content> head;
  private volatile Node<Content> tail;

  private final AtomicBoolean disposed = new AtomicBoolean();
  private final AtomicInteger size = new AtomicInteger();
  private final String category;
  private final Cursors cursors = new Cursors();

  /**
   * {@link Cursors}
   */
  private final class Cursors extends DisposableRepository<Object, InnerCursor> {

    public Cursors() {
      super(new Factory<Object, InnerCursor>() {

        @Override
        public InnerCursor newInstance(final Object key) {
          return new InnerCursor();
        }
      });
    }

  }

  /**
   * {@link InnerCursor}
   */
  private final class InnerCursor implements Cursor<Message<Content>>, Disposable {

    public InnerCursor() {
      begin = head;
    }

    @Override
    public synchronized void dispose() {
      begin = null;
    }

    @Override
    public synchronized Message<Content> next() {
      final Node<Content> next = begin.next;
      if (next == null) return null;
      begin = next;
      return begin.message;
    }

    private Node<Content> begin;

  }

  /**
   * {@link Node}
   */
  private static class Node<Content> {
    public Node(final Node<Content> next, final Message<Content> message) {
      this.next = next;
      this.message = message;
    }

    public boolean casNext(final Node<Content> expect, final Node<Content> update) {
      return NEXT_UPDATER.compareAndSet(this, expect, update);
    }

    volatile Node<Content> next;

    final Message<Content> message;

    private static final AtomicReferenceFieldUpdater<Node, Node> NEXT_UPDATER =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");
  }

}
