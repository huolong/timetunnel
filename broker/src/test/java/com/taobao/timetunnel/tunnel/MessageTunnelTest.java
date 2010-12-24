package com.taobao.timetunnel.tunnel;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.InvalidSubscriberException;
import com.taobao.timetunnel.message.ByteBufferMessageFactory;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.message.MessageFactory;
import com.taobao.timetunnel.session.Session;
import com.taobao.util.Bytes;
import com.taobao.util.Race;

/**
 * {@link MessageTunnelTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-15
 * 
 */
public class MessageTunnelTest extends InjectMocksSupport {

  @Before
  public void setUp() throws Exception {
    final MessageFactory<ByteBuffer> messageFactory = new ByteBufferMessageFactory();
    tunnels = new ByteBufferMessageTunnels(listener, 100, messageFactory);
    doReturn("log").when(category).name();
    doReturn("log").when(category).toString();
    doReturn(true).when(category).isMessageUselessReadBy(new HashSet<String>(Arrays.asList("dw")));
    doReturn(true).when(category).isInvaildSubscriber("xx");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldGetAllAndBalanced() throws Exception {
    final List<Future<List<ByteBuffer>>> futures =
      Race.run(new Post(pub("pub0", 10)),
               new Post(pub("pub1", 10)),
               new Post(pub("pub2", 10)),
               new Post(pub("pub3", 10)),
               new Post(pub("pub4", 10)),
               new Get(sub("sub0", 10, "dw", 3)),
               new Get(sub("sub1", 10, "dw", 3)));

    final List<ByteBuffer> got0 = futures.get(5).get();
    final List<ByteBuffer> got1 = futures.get(6).get();
    assertThat(Math.abs(got0.size() - got1.size()),lessThan(100));

    final List<ByteBuffer> result = new LinkedList<ByteBuffer>();
    result.addAll(got0);
    result.addAll(got1);
    final List<Integer> ints = toIntList(result);
    Collections.sort(ints);
    assertThat(ints.size(), is(TIMES));
    assertThat(ints.get(TIMES - 1), is(TIMES - 1));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldGetAllEvenIfSubTimeout() throws Exception {
    final List<Future<List<ByteBuffer>>> futures =
      Race.run(new Post(pub("pub0", 1)),
               new Post(pub("pub1", 1)),
               new Post(pub("pub2", 1)),
               new Get(sub("sub0", 1, "dw", 3)),
               new TimeoutGet(sub("sub1", 1, "dw", 3)));

    final List<ByteBuffer> result = futures.get(3).get();
    final List<Integer> ints = toIntList(result);
    Collections.sort(ints);
    assertThat(ints.size(), is(TIMES));
    assertThat(ints.get(TIMES - 1), is(TIMES - 1));
  }
  
  @Test(expected=InvalidSubscriberException.class)
  public void shouldExpectInvaildSubscriber() throws Exception {
    tunnels.tunnel(category).ackAndGet(sub("sub", 1, "xx", 3));
  }

  @After
  public void tearDown() throws Exception {
    tunnels.dispose();
  }

  private ByteBuffer message(final int i) {
    return Bytes.toBuffer(i);
  }

  private Session pub(final String name, final int timeout) {
    return FakeSession.pub(name, timeout);
  }

  private Session sub(final String name,
                      final int timeout,
                      final String group,
                      final int recvwinsize) {
    return FakeSession.sub(name, timeout, group, recvwinsize);
  }

  private List<Integer> toIntList(final List<ByteBuffer> result) {
    return new ArrayList<Integer>(new AbstractList<Integer>() {

      @Override
      public Integer get(final int index) {
        return result.get(index).getInt(0);
      }

      @Override
      public int size() {
        return result.size();
      }
    });
  }

  @Mock
  private TrimListener listener;
  @Mock
  private Category category;

  private final AtomicInteger postCount = new AtomicInteger();
  private final AtomicInteger getCount = new AtomicInteger();

  private static final int TIMES = 1000;

  private ByteBufferMessageTunnels tunnels;

  /**
   * {@link Get}
   */
  private class Get implements Callable<List<ByteBuffer>> {

    public Get(final Session sub) {
      this.sub = sub;
    }

    @Override
    public List<ByteBuffer> call() throws Exception {
      final List<ByteBuffer> got = new LinkedList<ByteBuffer>();
      for (;;) {
        final List<ByteBuffer> output = tunnels.tunnel(category).ackAndGet(sub);
        got.addAll(output);
        int count = getCount.addAndGet(output.size());
        if (count >= TIMES) break;
      }
      tunnels.tunnel(category).ackAndGet(sub);
      ((FakeSession)sub).invalid();
      return got;
    }

    @Override
    public final String toString() {
      return sub.toString();
    }

    protected final Session sub;

  }

  /**
   * {@link Post}
   */
  private final class Post implements Callable<List<ByteBuffer>> {

    public Post(final Session pub) {
      this.pub = pub;
    }

    @Override
    public List<ByteBuffer> call() throws Exception {
      for (;;) {
        final int i = postCount.getAndIncrement();
        if (i >= TIMES) break;
        tunnels.tunnel(category).post(pub, message(i));
      }
      return Collections.emptyList();
    }

    @Override
    public String toString() {
      return pub.toString();
    }

    private final Session pub;

  }

  private final class TimeoutGet extends Get {

    public TimeoutGet(final Session sub) {
      super(sub);
    }

    @Override
    public List<ByteBuffer> call() throws Exception {
      tunnels.tunnel(category).ackAndGet(sub);
      TimeUnit.MILLISECONDS.sleep(1001L);
      ((FakeSession) sub).invalid();
      return Collections.emptyList();
    }

  }

}
