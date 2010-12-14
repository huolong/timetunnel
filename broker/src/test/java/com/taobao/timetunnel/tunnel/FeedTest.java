package com.taobao.timetunnel.tunnel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.taobao.timetunnel.Appendable;
import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.message.ByteBufferMessageFactory;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.message.MessageFactory;
import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.util.Bytes;
import com.taobao.util.Race;

/**
 * {@link FeedTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-16
 * 
 */
public class FeedTest extends InjectMocksSupport {

  @Test
  public void dumpToAppendable() throws Exception {
    new Post().call();
    feed.dumpTo(appendable);
    verify(appendable, times(TIMES)).append(eq(category), eq(publisher), any(ByteBuffer.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void parallelOnePostAndTwoGet() throws Exception {
    Race.run(new Post(), new Get("dw"), new Get("ad"));
    assertThat(feed.size(), is(0));
  }

  @Before
  public void setUp() throws Exception {
    doReturn(false).when(category).isMessageExpiredAfter(anyLong());
    doReturn(true).when(category).isMessageUselessReadBy(new HashSet<String>(Arrays.asList("dw",
                                                                                           "ad")));
  }

  @After
  public void tearDown() throws Exception {
    feed.dispose();
  }

  private Message<ByteBuffer> message(final int i) {
    return factory.createBy(Bytes.toBuffer(i), category, publisher);
  }

  @Mock
  private Appendable<ByteBuffer> appendable;

  @Mock
  private Session publisher;

  @Mock
  private Category category;

  private final MessageFactory<ByteBuffer> factory = new ByteBufferMessageFactory();

  private final Feed<ByteBuffer> feed = new ConcurrentFeed<ByteBuffer>("log");

  private static final int TIMES = 1000;

  /**
   * @{link Get}
   */
  private final class Get implements Callable<Void> {

    public Get(final String name) {
      this.name = name;
      sub = mock(Session.class);
      doReturn(name).when(sub).stringValueOf(Attribute.subscriber);
    }

    @Override
    public Void call() throws Exception {
      final Cursor<Message<ByteBuffer>> cursor = feed.cursorOf(name);
      for (int i = 0; i < TIMES; i++) {
        for (;;) {
          final Message<ByteBuffer> message = cursor.next();
          if (message == null) continue;
          assertThat(message.content(), is(Bytes.toBuffer(i)));
          message.readBy(sub);
          if (message.isUseless()) feed.trim();
          break;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return "Get " + name;
    }

    private final Session sub;
    private final String name;
  }

  /**
   * @{link Post}
   */
  private final class Post implements Callable<Void> {

    @Override
    public Void call() throws Exception {
      for (int i = 0; i < TIMES; i++) {
        feed.post(message(i));
      }
      return null;
    }

    @Override
    public String toString() {
      return "Post";
    }
  }
}
