package com.taobao.timetunnel.swap;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * {@link NByteBufferFreezer}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
public final class NByteBufferFreezer implements Freezer<ByteBuffer> {

  public NByteBufferFreezer(final File home, final int maxMessageSize) {
    this(home, maxMessageSize, DEFAULT_FILE_MAX_SIZE);
  }

  public NByteBufferFreezer(final File home, final int maxMessageSize, final int fileMaxSize) {
    this.home = home;
    this.maxMessageSize = maxMessageSize;
    if (!home.exists()) home.mkdirs();
  }

  @Override
  public synchronized void dispose() {
    chunk.close();
  }

  @Override
  public synchronized Point<ByteBuffer> freeze(final ByteBuffer buffer) {
    final int seq = seq();
    if (!chunk.hasRemainingFor(buffer)) {
      chunk.close();
      chunk = newChunk(seq);
    }
    return chunk.freeze(buffer);
  }

  private int seq() {
    return (seq == Long.MAX_VALUE) ? (seq = 0) : seq++;
  }

  private Chunk newChunk(final int seq) {
    return new FileChunk(path(seq), DEFAULT_FILE_MAX_SIZE, maxMessageSize);
  }

  private File path(final int seq) {
    return new File(home, seq + "");
  }

  private final File home;
  private final int maxMessageSize;

  private Chunk chunk = new Null();
  private int seq = 0;

  public static final int DEFAULT_FILE_MAX_SIZE = (1 << 20) * 64;

  public static void main(String[] args) {
    System.out.println(Long.MAX_VALUE + 1);
  }

  /**
   * {@link Null}
   */
  private final class Null implements Chunk {
    @Override
    public void close() {}

    @Override
    public Point<ByteBuffer> freeze(final ByteBuffer buffer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasRemainingFor(final ByteBuffer buffer) {
      return false;
    }
  }

}
