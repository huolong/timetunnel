package com.taobao.timetunnel.swap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import com.taobao.util.Bytes;
import com.taobao.util.Race;

/**
 * {@link ChunkTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-9
 * 
 */
public class ChunkTest {
  @Test
  public void shouldFreezeMoreIfClearBeforeFrozen() throws Exception {
    final File path = new File("target/gbf");
    final int buffers = 16;
    chunk = new FileChunk(path, buffers * 8, 4);
    final List<Point<ByteBuffer>> points = new ArrayList<Point<ByteBuffer>>(buffers);

    for (int i = 0; i < buffers - 1; i++) {
      points.add(chunk.freeze(Bytes.toBuffer(i)));
    }

    for (final Point<ByteBuffer> point : points) {
      point.clear();
    }

    for (int i = 0; i < buffers; i++) {
      points.add(chunk.freeze(Bytes.toBuffer(i)));
    }

    assertThat(chunk.hasRemainingFor(Bytes.toBuffer(16)), is(false));
    chunk.close();
    path.delete();
  }

  @Test
  public void shouldOKInNormalFlow() throws Exception {

    final int buffers = 32;
    final File path = new File("target/gaf");
    chunk = new FileChunk(path, buffers * 8, 4);

    final List<Point<ByteBuffer>> points = new ArrayList<Point<ByteBuffer>>(buffers);

    for (int i = 0; i < buffers; i++) {
      points.add(chunk.freeze(Bytes.toBuffer(i)));
    }

    chunk.close();

    assertPointedBuffer(points);

    System.gc();

    assertPointedBuffer(points);

    System.gc();

    assertThat(path.exists(), is(true));

    for (final Point<ByteBuffer> point : points) {
      point.clear();
    }

    assertThat(path.exists(), is(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldOkInRace() throws Exception {
    final File path = new File("target/oir");
    final int buffers = 1600;
    chunk = new FileChunk(path, buffers * 8, 4);

    final BlockingQueue<Point<ByteBuffer>> queue = new LinkedBlockingQueue<Point<ByteBuffer>>(16);

    final Callable<Void> freezing = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        for (int i = 0; i < buffers; i++) {
          queue.put(chunk.freeze(Bytes.toBuffer(i)));
        }
        return null;
      }
    };
    final Callable<Void> removing = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        for (int i = 0; i < buffers; i++) {
          queue.take().clear();
        }
        return null;
      }
    };
    Race.run(freezing, removing);
    chunk.close();
    path.delete();
  }

  private void assertPointedBuffer(final List<Point<ByteBuffer>> points) {
    for (int i = 0; i < points.size(); i++) {
      final ByteBuffer byteBuffer = points.get(i).get().asReadOnlyBuffer();
      assertThat(byteBuffer.getInt(), is(i));
    }
  }

  private Chunk chunk;

}
