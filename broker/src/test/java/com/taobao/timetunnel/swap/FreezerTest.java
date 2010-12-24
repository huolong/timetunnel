package com.taobao.timetunnel.swap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.util.DirectoryCleaner;
import com.taobao.util.Race;

/**
 * {@link FreezerTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public class FreezerTest {

  @Test
  public void freezeGetRemove() throws Exception {
    final int times = 100000;

    final List<Point<ByteBuffer>> points = new ArrayList<Point<ByteBuffer>>(times);
    for (int i = 0; i < times; i++) {
      points.add(freezer.freeze(newByteBuffer().putInt(0, i)));
    }

    for (int i = 0; i < times; i++) {
      final ByteBuffer byteBuffer = points.get(i).get();
      assertThat(byteBuffer.getInt(), is(i));
    }

    for (final Point<ByteBuffer> point : points) {
      point.clear();
    }

    freezer.dispose();
    assertThat(new File("./target/category/client").list().length, is(0));

  }

  @Test
  @SuppressWarnings("unchecked")
  public void freezingRemovingRace() throws Exception {
    final int times = 100;
    final Queue<Point<ByteBuffer>> queue = new ConcurrentLinkedQueue<Point<ByteBuffer>>();
    final Random random = new Random(System.currentTimeMillis());
    final Callable<Object> removing = new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        for (int i = 0; i < times; i++) {
          queue.offer(freezer.freeze(newByteBuffer()));
          Thread.sleep(random.nextInt(10));
        }
        return null;
      }
    };
    final Callable<Object> freezing = new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        for (int i = 0; i < times;) {
          Thread.sleep(random.nextInt(10));
          final Point<ByteBuffer> point = queue.poll();
          if (point == null) continue;
          point.clear();
          i++;
        }
        // TODO Auto-generated method stub
        return null;
      }
    };
    Race.run(freezing, removing);
    freezer.dispose();
    assertThat(home.list().length, is(0));
  }

  @Test
  public void readBlocksAsAChunk() throws Exception {
    final File file = new File("target/rbc");
    prepare(file);

    final ByteBuffer dst = ByteBuffer.allocate(BLOCK_SIZE * TIMES);
    final FileChannel channel = new FileInputStream(file).getChannel();
    channel.read(dst);
    dst.clear();
    final long begin = System.nanoTime();
    channel.read(dst);
    final long end = System.nanoTime();
    final long elapsed = end - begin;
    System.out.println("read blocks as a chunk\t : " + elapsed);
    channel.close();
    file.delete();
  }

  @Test
  public void readBlocksByLoop() throws Exception {
    final File file = new File("target/rbl");
    prepare(file);

    final ByteBuffer dst = ByteBuffer.allocate(BLOCK_SIZE);
    final FileChannel channel = new FileInputStream(file).getChannel();
    for (int i = 0; i < TIMES; i++) {
      channel.read(dst);
      dst.clear();
    }
    final long begin = System.nanoTime();
    for (int i = 0; i < TIMES; i++) {
      channel.read(dst);
      dst.clear();
    }
    final long end = System.nanoTime();
    final long elapsed = end - begin;
    System.out.println("read blocks by loop\t : " + elapsed);
    channel.close();
    file.delete();
  }

  @Before
  public void setUp() throws Exception {
    DirectoryCleaner.clean(home);
    freezer = new ByteBufferFreezer(home, (1 << 20) * 64, (1 << 15));
  }

  @After
  public void tearDown() throws Exception {
    freezer.dispose();
  }

  @Test
  public void writeAChunk() throws Exception {
    final FileChannel channel = new FileOutputStream("./target/wac").getChannel();
    final ByteBuffer src = buffer(BLOCK_SIZE * TIMES);

    channel.write(src);
    channel.force(false);

    final long begin = System.nanoTime();
    channel.write(src);
    // channel.force(false);
    final long end = System.nanoTime();
    final long elapsed = end - begin;
    System.out.println("write blocks as chunk\t : " + elapsed);
  }

  @Test
  public void writeBlocks() throws Exception {
    final FileChannel channel = new FileOutputStream("./target/wbl").getChannel();
    final ByteBuffer src = buffer(BLOCK_SIZE);
    for (int i = 0; i < TIMES; i++) {
      channel.write(src);
      src.rewind();
    }
    final long begin = System.nanoTime();

    for (int i = 0; i < TIMES; i++) {
      channel.write(src);
      src.rewind();
      // channel.force(false);
    }
    final long end = System.nanoTime();
    final long elapsed = end - begin;
    System.out.println("loop write blocks\t : " + elapsed);
    channel.close();
  }

  protected ByteBuffer buffer(final int capacity) {
    final ByteBuffer buffer = ByteBuffer.allocate(capacity);
    while (buffer.hasRemaining()) {
      buffer.putInt(1);
    }
    buffer.flip();
    return buffer;
  }

  protected void prepare(final File file) throws FileNotFoundException, IOException {
    final FileChannel channel = new FileOutputStream(file).getChannel();
    channel.write(buffer(BLOCK_SIZE * TIMES));
    channel.close();
  }

  private ByteBuffer newByteBuffer() {
    final ByteBuffer buffer = ByteBuffer.allocate(1024);
    for (int j = 0; j < buffer.capacity(); j++) {
      buffer.put((byte) 5);
    }
    buffer.flip();
    return buffer;
  }

  private static final int BLOCK_SIZE = 4096;

  final File home = new File("./target/category/client");

  private static final int TIMES = 100;

  private Freezer<ByteBuffer> freezer;

}
