package com.taobao.timetunnel.swap;

import static com.taobao.timetunnel.swap.ByteBufferPoint.point;
import static com.taobao.timetunnel.swap.Chunk.Segment.DATA_SIZE_LENGTH;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.timetunnel.swap.Chunk.Segment;
import com.taobao.util.Bytes;

/**
 * {@link BufferImpl}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-10
 * @see Segment
 */
final class BufferImpl implements Buffer {

  /**
   * @param segment in {@link Chunk}
   * @param capacity of slice.
   * @param maxByteBufferSize
   * @param factory
   */
  BufferImpl(final Segment segment,
             final int capacity,
             final int maxByteBufferSize,
             final ReferenceFactory factory) {
    this.segment = segment;
    this.capacity = capacity;
    this.maxByteBufferSize = maxByteBufferSize;
    this.factory = factory;
    buffersForWrite = new ArrayList<ByteBuffer>(capacity * 2);
    LOGGER.debug("{} created.", this);
  }

  /**
   * @param segment in {@link Chunk}
   * @param maxByteBufferSize
   */
  private BufferImpl(final Segment segment, final int maxByteBufferSize) {
    this(segment, DEFAULT_CAPACITY, maxByteBufferSize, DEFAULT_FACTORY);
  }

  @Override
  public synchronized void freeze() {
    if (frozen) return;
    try {
      segment.write(buffersForWrite.toArray(new ByteBuffer[0]), size);
    } catch (final Exception e) {
      // TODO this exception can make data missing.
      throw new RuntimeException(e);
    } finally {
      buffersForWrite.clear();
      frozen = true;
      LOGGER.debug("{} frozen.", this);
    }
  }

  @Override
  public synchronized boolean hasRemainingFor(final ByteBuffer byteBuffer) {
    if (frozen) throw new IllegalStateException("Buffer has been frozen.");
    return size + DATA_SIZE_LENGTH + byteBuffer.remaining() <= capacity
        * (maxByteBufferSize + Segment.DATA_SIZE_LENGTH);
  }

  @Override
  public synchronized int size() {
    return size;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("BufferImpl [capacity=")
           .append(capacity)
           .append(", maxByteBufferSize=")
           .append(maxByteBufferSize)
           .append(", size=")
           .append(size)
           .append(", frozen=")
           .append(frozen)
           .append(", cache=")
           .append(cache)
           .append("]");
    return builder.toString();
  }

  @Override
  public synchronized Point<ByteBuffer> write(final ByteBuffer byteBuffer) {
    if (!hasRemainingFor(byteBuffer))
      throw new IllegalStateException(this + " has not remaining for buffer.");
    if (frozen) throw new IllegalStateException("Buffer has been frozen.");

    final int position = size + DATA_SIZE_LENGTH;
    size += byteBuffer.remaining() + DATA_SIZE_LENGTH;
    final ByteBuffer duplicate = byteBuffer.duplicate();
    duplicate.mark();
    buffersForWrite.add(Bytes.toBuffer(byteBuffer.remaining()));
    buffersForWrite.add(duplicate);
    return point(newWeakReferenceOf(duplicate), newSlice(position, duplicate.remaining()));
  }

  synchronized void clear() {
    if (!frozen) buffersForWrite.clear();
    if (cache != null) {
      cache.clear();
      cache = null;
    }
    // if (frozen) segment.removeOn(size);
    LOGGER.debug("{} cleared.", this);
  }

  synchronized int reduce(final int length) {
    return (size -= length + DATA_SIZE_LENGTH);
  }

  synchronized ByteBuffer slice(final int position, final int length) {
    byte[] buffer = null;
    if (cache != null) buffer = cache.get();

    if (buffer == null) {
      try {
        buffer = new byte[size];
        segment.read(buffer);
        cache = newCacheOf(buffer);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
      LOGGER.debug("{} read from chunk.", this);
    }
    return ByteBuffer.wrap(buffer, position, length);
  }

  private Reference<byte[]> newCacheOf(final byte[] buffer) {
    return factory.createBy(buffer);
  }

  private Slice newSlice(final int position, final int length) {
    return new Slice() {

      @Override
      public ByteBuffer get() {
        return slice(position, length);
      }

      @Override
      public void remove() {
        if (reduce(length) == 0) clear();
        segment.reduce(length);
      }
    };
  }

  private WeakReference<ByteBuffer> newWeakReferenceOf(final ByteBuffer duplicate) {
    return new WeakReference<ByteBuffer>(duplicate);
  }

  public static Buffer buffer(final Segment segment, final int maxByteBufferSize) {
    return new BufferImpl(segment, maxByteBufferSize);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(BufferImpl.class);

  static final int DEFAULT_CAPACITY = 16;

  static final ReferenceFactory DEFAULT_FACTORY = new ReferenceFactory() {

    @Override
    public Reference<byte[]> createBy(final byte[] data) {
      return new SoftReference<byte[]>(data);
    }
  };

  private final Segment segment;
  private final int capacity;
  private final int maxByteBufferSize;
  private final ReferenceFactory factory;
  private final List<ByteBuffer> buffersForWrite;

  private int size = 0;
  private boolean frozen;
  private Reference<byte[]> cache;

  interface ReferenceFactory {
    Reference<byte[]> createBy(byte[] data);
  }
}
