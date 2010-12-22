package com.taobao.util;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link FixSizeBufferWriter}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public final class FixSizeBufferWriter extends Writer {
  public FixSizeBufferWriter(final int size) {
    buffer = new char[size];
  }

  @Override
  public void close() throws IOException {}

  @Override
  public void flush() throws IOException {}

  @Override
  public String toString() {
    return new String(buffer) + "\n...";
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len) throws IOException {
    final int length = Math.min((buffer.length - position), len);
    if (length == 0) return; // abandon
    System.arraycopy(cbuf, off, buffer, position, length);
    position = Math.min(buffer.length, position + length);
  }

  private final char[] buffer;

  private int position = 0;
}
