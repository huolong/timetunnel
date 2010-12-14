package com.taobao.timetunnel.swap;

import static com.taobao.timetunnel.swap.BufferImpl.buffer;
import static com.taobao.timetunnel.swap.Chunk.Segment.DATA_SIZE_LENGTH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FileChunk} is not thread-safed.
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
final class FileChunk implements Chunk {

  FileChunk(final File path, final long capacity, final int maxMessageSize) {
    this.path = path;
    this.capacity = capacity;
    this.maxMessageSize = maxMessageSize;

    try {
      channel = new FileOutputStream(path).getChannel();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
    LOGGER.info("{} created.", this);
  }

  @Override
  public void close() {
    try {
      buffer.freeze();
      channel.close();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Point<ByteBuffer> freeze(final ByteBuffer byteBuffer) {
    if (!hasRemainingFor(byteBuffer))
      throw new IllegalArgumentException(this + " has not remaining for buffer.");
    if (!buffer.hasRemainingFor(byteBuffer)) {
      buffer.freeze();
      buffer = newBuffer(maxMessageSize);
    }
    size.addAndGet(byteBuffer.remaining() + DATA_SIZE_LENGTH);
    return buffer.write(byteBuffer);
  }

  @Override
  public boolean hasRemainingFor(final ByteBuffer byteBuffer) {
    return size.get() + byteBuffer.remaining() + DATA_SIZE_LENGTH <= capacity;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("FileChunk [path=")
           .append(path)
           .append(", capacity=")
           .append(capacity)
           .append(", size=")
           .append(size)
           .append(", readOnly=")
           .append(!channel.isOpen())
           .append("]");
    return builder.toString();
  }

  private Buffer newBuffer(final int bufferSize) {
    try {
      return buffer(segment(), bufferSize);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Segment segment() throws IOException {
    return new Segment() {
      @Override
      public void read(final byte[] buffer) throws IOException {
        final FileChannel channel = new FileInputStream(path).getChannel();
        channel.read(ByteBuffer.wrap(buffer), position);
        channel.close();
      }

      @Override
      public void reduce(final int length) {
        size.addAndGet(-(length + DATA_SIZE_LENGTH));
      }

      @Override
      public void removeOn(final int size) {
        if (position + size < chunkSize()) return;
        if (path.delete()) LOGGER.info("{} deleted", this);
        else LOGGER.warn("{} delete failed", this);
      }

      @Override
      public void write(final ByteBuffer[] buffers, final long size) throws IOException {
        /*
         * In linux, channel.write(ByteBuffer[]) has max length (eg: 64 byte),
         * so it need loop to guarantee all the buffers will be wrote.
         */
        int wrote = 0;
        while (wrote < size)
          wrote += channel.write(buffers);
      }

      private long chunkSize() {
        return size.get();
      }

      final long position = size.get();
    };
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(FileChunk.class);

  static final int TIMES = 16;

  private final int maxMessageSize;
  private final File path;
  private final long capacity;
  private final FileChannel channel;
  private final AtomicLong size = new AtomicLong();

  private Buffer buffer = new NullBuffer();

  /**
   * {@link NullBuffer}
   */
  private static final class NullBuffer implements Buffer {
    @Override
    public void freeze() {}

    @Override
    public boolean hasRemainingFor(final ByteBuffer byteBuffer) {
      return false;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Point<ByteBuffer> write(final ByteBuffer byteBuffer) {
      throw new UnsupportedOperationException();
    }
  }

}
