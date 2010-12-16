package com.taobao.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * {@link Bytes}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-8
 * 
 */
public final class Bytes {
  private Bytes() {}

  public static ByteBuffer toBuffer(final int i) {
    return (ByteBuffer) ByteBuffer.wrap(new byte[4]).putInt(i).flip();
  }

  public static final ByteBuffer toBuffer(final String value) {
    return ByteBuffer.wrap(toBytes(value));
  }

  public static final byte[] toBytes(final int value) {
    return ByteBuffer.wrap(new byte[4]).putInt(value).array();
  }

  public static final byte[] toBytes(final String value) {
    return value.getBytes(UTF8);
  }

  public static final int toInt(final byte[] bs) {
    return ByteBuffer.wrap(bs).getInt();
  }

  public static final String toString(final byte[] bs) {
    return new String(bs, UTF8);
  }

  public static String toString(final ByteBuffer buffer) {
    final byte[] array = new byte[buffer.remaining()];
    buffer.asReadOnlyBuffer().get(array);
    return toString(array);
  }

  private static final Charset UTF8 = Charset.forName("UTF-8");

  public static final byte[] NULL = new byte[0];
}
