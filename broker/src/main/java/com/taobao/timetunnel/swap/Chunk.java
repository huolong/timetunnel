package com.taobao.timetunnel.swap;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * {@link Chunk} used by {@link NByteBufferFreezer}.
 * 
 * <pre>
 * 
 *      | <-                 Chunk                     -> |
 * --------------------------------------------------------->
 *      +++++++++++++++++++++++++++++++++++++++++++++++++++
 * --------------------------------------------------------->
 *      | <-     Segment1    -> | <-       Segment     -> |
 * </pre>
 * 
 * 
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
interface Chunk {

  /**
   * Close {@link Chunk} if no space for more {@link ByteBuffer}.
   */
  void close();

  /**
   * Freeze {@link ByteBuffer} in {@link Chunk}, it can not be invoked after
   * closed.
   * 
   * @param byteBuffer
   * @return {@link Point}
   */
  Point<ByteBuffer> freeze(ByteBuffer byteBuffer);

  /**
   * @param byteBuffer
   * @return false if no space for {@link ByteBuffer} remains.
   */
  boolean hasRemainingFor(ByteBuffer byteBuffer);

  /**
   * {@link Segment}
   * 
   * <pre>
   * 
   *      | <-                 Segment                   -> |
   * --------------------------------------------------------->
   *      +++++++++++++++++++++++++++++++++++++++++++++++++++
   * --------------------------------------------------------->
   *      | <-len(4)-> | <-             data(x)          -> |
   * </pre>
   */
  interface Segment {

    void read(byte[] buffer) throws IOException;

    void reduce(int length);

    void write(ByteBuffer[] buffers, long size) throws IOException;

    int DATA_SIZE_LENGTH = 4;
  }
}
