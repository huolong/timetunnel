package com.taobao.timetunnel.broker;

import java.nio.ByteBuffer;

import com.taobao.util.Bytes;
import com.taobao.util.JsonUtils.Parser;

/**
 * {@link Session}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
final class Session {
  /**
   * Used by {@link Parser}.
   */
  Session() {}

  private Session(final String token,
                            final ClientType type,
                            final int timeout,
                            final String subscriber,
                            final int receiveWindowSize) {
    super();
    this.timeout = timeout;
    this.type = type;
    this.subscriber = subscriber;
    this.token = token;
    this.receiveWindowSize = receiveWindowSize;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Session other = (Session) obj;
    if (token == null) {
      if (other.token != null) return false;
    } else if (!token.equals(other.token)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((token == null) ? 0 : token.hashCode());
    return result;
  }

  public boolean invalid() {
    return false;
  }

  public final String name() {
    return token;
  }

  public final int receiveWindowSize() {
    return receiveWindowSize;
  }

  public final String subscriber() {
    return subscriber;
  }

  public final int timeout() {
    return timeout;
  }

  public final ByteBuffer token() {
    return Bytes.toBuffer(token);
  }

  public void token(final String str) {
    token = str;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("AbstractClientInfo [timeout=")
           .append(timeout)
           .append(", type=")
           .append(type)
           .append(", subscriber=")
           .append(subscriber)
           .append(", token=")
           .append(token)
           .append(", receiveWindowSize=")
           .append(receiveWindowSize)
           .append("]");
    return builder.toString();
  }

  public final ClientType type() {
    return type;
  }

  public static final Session pub(final String name, final int timeout) {
    return new Session(name, ClientType.pub, timeout, "", -1);
  }

  public static final Session sub(final String name,
                                            final int timeout,
                                            final String subscriber,
                                            final int receiveWindowSize) {
    return new Session(name, ClientType.sub, timeout, subscriber, receiveWindowSize);
  }

  protected int timeout;

  protected ClientType type;

  protected String subscriber;

  protected transient String token;

  protected int receiveWindowSize;

  public enum ClientType {
    pub, sub, pub_sub
  }
}
