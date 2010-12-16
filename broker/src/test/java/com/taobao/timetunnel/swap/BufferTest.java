package com.taobao.timetunnel.swap;

import static com.taobao.timetunnel.swap.BufferImpl.DEFAULT_CAPACITY;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.taobao.timetunnel.swap.BufferImpl.ReferenceFactory;
import com.taobao.timetunnel.swap.Chunk.Segment;
import com.taobao.util.Bytes;

/**
 * {@link BufferTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-10
 * 
 */
public class BufferTest {
  @Test
  public void main() throws Exception {
    final ReferenceQueue<byte[]> queue = new ReferenceQueue<byte[]>();
    final ReferenceFactory factory = new ReferenceFactory() {

      @Override
      public Reference<byte[]> createBy(final byte[] data) {
        return new WeakReference<byte[]>(data, queue);
      }
    };
    buffer = new BufferImpl(segment, DEFAULT_CAPACITY, 4, factory);

    final List<Point<ByteBuffer>> points = new ArrayList<Point<ByteBuffer>>();
    for (int i = 0; buffer.hasRemainingFor(Bytes.toBuffer(0)); i++) {
      points.add(buffer.write(Bytes.toBuffer(i)));
    }

    assertThat(points.size(), is(DEFAULT_CAPACITY));

    for (int i = 0; i < DEFAULT_CAPACITY; i++) {
      assertThat(points.get(i).get().duplicate().getInt(), is(i));
    }

    buffer.freeze();

    System.gc();

    for (int i = 0; i < DEFAULT_CAPACITY; i++) {
      assertThat(points.get(i).get().duplicate().getInt(), is(i));
    }

    System.gc();

    assertThat(queue.remove(), is(notNullValue()));

    for (Point<ByteBuffer> point : points) {
      point.clear();
    }

    assertThat(removed, is(true));

  }

  private Buffer buffer;
  private boolean removed;

  private final Segment segment = new Segment() {

    @Override
    public void read(final byte[] buffer) throws IOException {
      final byte[] src = all.array();
      System.arraycopy(src, 0, buffer, 0, src.length);
    }

    private int bs;

    @Override
    public void write(final ByteBuffer[] buffers, long size) throws IOException {
      int s = 0;
      for (final ByteBuffer byteBuffer : buffers) {
        s += byteBuffer.remaining();
      }
      bs = s;
      all = ByteBuffer.allocate(s);
      for (final ByteBuffer byteBuffer : buffers) {
        all.put(byteBuffer);
      }
    }

    private ByteBuffer all;
    private int reduce;

    @Override
    public void reduce(int length) {
      reduce += length + Segment.DATA_SIZE_LENGTH;
      removed = (reduce == bs);
    }
  };

}
