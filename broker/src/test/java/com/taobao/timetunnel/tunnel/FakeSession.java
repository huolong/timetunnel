package com.taobao.timetunnel.tunnel;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.session.Type;

/**
 * {@link FakeSession}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-1
 * 
 */
final class FakeSession implements Session {

  private FakeSession(final Type type,
                      final String id,
                      final int to,
                      final String group,
                      final int recvwinsize) {
    super();
    this.type = type;
    this.id = id;
    this.to = to;
    this.group = group;
    this.recvwinsize = recvwinsize;
  }

  @Override
  public void add(final InvalidListener listener) {
    listeners.add(listener);
  }

  @Override
  public boolean booleanValueOf(final Attribute attribute) {
    return false;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public int intValueOf(final Attribute attribute) {
    switch (attribute) {
      case receiveWindowSize:
        return recvwinsize;
      case timeout:
        return to;
      default:
        return 0;
    }
  }

  public void invalid() {
    invalid = true;
    for (final InvalidListener listener : listeners) {
      listener.onInvalid();
    }
  }

  @Override
  public boolean isInvalid() {
    return invalid;
  }

  @Override
  public long longValueOf(final Attribute attribute) {
    return 0;
  }

  @Override
  public void remove(final InvalidListener listener) {
    listeners.remove(listener);
  }

  @Override
  public String stringValueOf(final Attribute attribute) {
    return group;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("FakeSession [id=")
           .append(id)
           .append(", recvwinsize=")
           .append(recvwinsize)
           .append(", to=")
           .append(to)
           .append(", group=")
           .append(group)
           .append(", type=")
           .append(type)
           .append(", invalid=")
           .append(invalid)
           .append("]");
    return builder.toString();
  }

  @Override
  public Type type() {
    return type;
  }

  public static FakeSession pub(final String id, final int to) {
    return new FakeSession(Type.pub, id, to, "", -1);
  }

  public static FakeSession sub(final String id,
                                final int to,
                                final String group,
                                final int recvwinsize) {
    return new FakeSession(Type.sub, id, to, group, recvwinsize);
  }

  private final String id;

  private final int recvwinsize;

  private final int to;

  private final String group;

  private final Type type;

  private final Collection<InvalidListener> listeners = new CopyOnWriteArrayList<InvalidListener>();

  private volatile boolean invalid;

}
