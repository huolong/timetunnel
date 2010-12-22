package com.taobao.timetunnel.swap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ByteBufferFreezer}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public final class ByteBufferFreezer implements Freezer<ByteBuffer> {

  public ByteBufferFreezer(final File home, int maxMessageSize) {
    this(home, maxMessageSize * 16, DEFAULT_FILE_MAX_SIZE);
  }

  public ByteBufferFreezer(final File home, final int buffersMaxSize, final int fileMaxSize) {
    this.home = home;
    if (home.exists()) dispose();
    if (!home.mkdirs()) throw new IllegalStateException("Can't mkdir " + home);

    this.buffersMaxSize = buffersMaxSize;
    this.fileMaxSize = fileMaxSize;
  }

  @Override
  public synchronized void dispose() {
    try {
      if (channel != null) channel.close();
      current = null;
      if (!home.exists()) return;

      final File[] listFiles = home.listFiles();
      for (final File file : listFiles) {
        delete(file, "ByteBufferFileFreezer disposing");
      }
      delete(home, "ByteBufferFileFreezer disposing");
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized Point<ByteBuffer> freeze(final ByteBuffer buffer) {
    // TODO big buffer to optimizate
    if (current == null) newFileAndChannel();
    serial += 1;

    final int size = buffer.remaining();
    final ByteBuffer duplicate = buffer.duplicate();
    duplicate.mark();
    try {
      if (channel.size() + size >= fileMaxSize) return eofPoint(duplicate);
      return filePoint(size, duplicate);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void delete(final File file, final String procedure) {
    if (!file.delete()) LOGGER.warn("Can't delete file {} in {}", file, procedure);
    else LOGGER.info("Delete file {} in {}", file, procedure);
  }

  private Point<ByteBuffer> eofPoint(final ByteBuffer duplicate) {
    final FilePoint point = new EOFPoint(current, position, duplicate);
    buffers.add(duplicate);
    newFileAndChannel();
    return point;
  }

  private Point<ByteBuffer> filePoint(final int size, final ByteBuffer duplicate) throws IOException {
    final FilePoint point = new FilePoint(current, position, duplicate);
    buffers.add(duplicate);
    buffesSize += size;
    if (buffesSize > buffersMaxSize) flush();
    position += size;
    return point;
  }

  private void flush() throws IOException {
    final ByteBuffer[] srcs = new ByteBuffer[buffers.size()];
    final Iterator<ByteBuffer> itr = buffers.iterator();
    for (int i = 0; i < srcs.length; i++) {
      srcs[i] = itr.next();
      itr.remove();
    }

    channel.write(srcs);
    // channel.force(false);

    buffesSize = 0;
  }

  private void newFileAndChannel() {
    try {
      if (channel != null) {
        flush();
        channel.close();
      }
      current = new File(home, serial + "");
      channel = new FileOutputStream(current).getChannel();
      position = 0;
      LOGGER.info("Create new file {}", current);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufferFreezer.class);

  public static final int DEFAULT_FILE_MAX_SIZE = (1 << 20) * 64;

  private final File home;
  private final List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
  private final int buffersMaxSize;
  private final int fileMaxSize;

  private File current;
  private FileChannel channel;
  private long position;
  private long serial = 0;
  private int buffesSize = 0;

  /**
   * {@link EOFPoint}
   */
  private class EOFPoint extends FilePoint {

    public EOFPoint(final File file, final long offset, final ByteBuffer buffer) {
      super(file, offset, buffer);
    }

    @Override
    public void clear() {
      delete(file, "EOFPoint removing");
    }

  }

  /**
   * {@link FilePoint}
   */
  private class FilePoint implements Point<ByteBuffer> {

    public FilePoint(final File file, final long offset, final ByteBuffer buffer) {
      this.file = file;
      this.offset = offset;
      length = buffer.remaining();
      this.buffer = new WeakReference<ByteBuffer>(buffer);
    }

    @Override
    public final ByteBuffer get() {
      final ByteBuffer value = buffer.get();
      if (value != null) return (ByteBuffer) value.reset();
      final ByteBuffer buffer = ByteBuffer.allocate(length);

      FileChannel fileChannel = null;
      try {
        fileChannel = new FileInputStream(file).getChannel();
        fileChannel.read(buffer, offset);
        return (ByteBuffer) buffer.flip();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      } finally {
        try {
          if (fileChannel != null) fileChannel.close();
        } catch (final Exception e) {/* ignore */}
      }
    }

    @Override
    public void clear() {}

    private final WeakReference<ByteBuffer> buffer;
    private final int length;
    private final long offset;

    protected final File file;

  }
}
