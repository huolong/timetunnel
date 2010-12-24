package com.taobao.timetunnel.swap;

import java.io.File;
import java.nio.ByteBuffer;

import com.taobao.timetunnel.DisposableRepository;
import com.taobao.timetunnel.message.Category;

/**
 * {@link ByteBufferFreezers}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-3
 * 
 */
public final class ByteBufferFreezers extends DisposableRepository<Category, Freezer<ByteBuffer>> {

  public ByteBufferFreezers(final File home, final int chunkCapacity, final int chunkBuffer) {
    super(new Factory<Category, Freezer<ByteBuffer>>() {

      @Override
      public Freezer<ByteBuffer> newInstance(final Category category) {
        return new ByteBufferFreezer(new File(home, category.name()), chunkCapacity, chunkBuffer);
      }
    });
  }

  public Freezer<ByteBuffer> freezer(final Category category) {
    try {
      return getOrCreateIfNotExist(category);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

}
