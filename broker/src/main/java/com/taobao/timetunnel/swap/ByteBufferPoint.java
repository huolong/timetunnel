package com.taobao.timetunnel.swap;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import com.taobao.timetunnel.swap.Buffer.Slice;

/**
 * {@link ByteBufferPoint}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-10
 * 
 */
final class ByteBufferPoint implements Point<ByteBuffer> {
  private ByteBufferPoint(final WeakReference<ByteBuffer> reference, final Slice slice) {
    this.reference = reference;
    this.slice = slice;
  }

  @Override
  public synchronized ByteBuffer get() {
    if (removed) throw new IllegalStateException("Can't get after point removed.");
    final ByteBuffer ref = reference.get();
    if (ref != null) return (ByteBuffer) ref.reset();
    return slice.get();
  }

  @Override
  public synchronized void clear() {
    if (removed) return;
    if (reference.get() != null) reference.clear();
    slice.remove();
    removed = true;
  }

  public static Point<ByteBuffer> point(final WeakReference<ByteBuffer> reference,
                                        final Slice slice) {
    return new ByteBufferPoint(reference, slice);
  }

  /**
   * Referece will not get null before freeze, because
   * {@link BufferImpl#buffersForWrite} hold the strong reference.
   */
  private final WeakReference<ByteBuffer> reference;
  private final Slice slice;

  private boolean removed;

}
