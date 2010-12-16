package com.taobao.timetunnel.swap;

import java.nio.ByteBuffer;

/**
 * {@link Buffer} used by {@link Chunk} to improving performance of read and
 * write.
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-10
 * 
 */
interface Buffer {
  /**
   * Freeze {@link ByteBuffer} , more than once invoke this method will be
   * ignored.
   */
  void freeze();

  /**
   * @param byteBuffer
   * @return false if no space for {@link ByteBuffer} remains.
   */
  boolean hasRemainingFor(ByteBuffer byteBuffer);

  /**
   * Write {@link ByteBuffer} in to {@link Buffer}.
   * 
   * @param byteBuffer
   * @return {@link Point}.
   */
  Point<ByteBuffer> write(ByteBuffer byteBuffer);

  /**
   * {@link Slice}
   */
  interface Slice {

    ByteBuffer get();

    void remove();
  }

  int size();
}
