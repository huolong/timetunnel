package com.taobao.timetunnel.tunnel;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.message.Message;
import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.tunnel.ByteBufferMessageWatchers;
import com.taobao.timetunnel.tunnel.Cursor;
import com.taobao.timetunnel.tunnel.Group;
import com.taobao.timetunnel.tunnel.Watchable;
import com.taobao.timetunnel.tunnel.Watcher;
import com.taobao.util.Bytes;

/**
 * @{link WatcherTest}
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-29
 * 
 */
public class WatcherTest extends InjectMocksSupport {

  @Before
  public void setUp() throws Exception {
    doReturn(3).when(session).intValueOf(Attribute.receiveWindowSize);
    doReturn("dw").when(session).stringValueOf(Attribute.subscriber);
    doReturn(Bytes.toBuffer(0)).when(message).content();
    watcher = new ByteBufferMessageWatchers(watchable).watcher(session);
  }

  @Test
  public void shouldAckedGetAndRefluxEqualsPushed() throws Exception {
    int ackedGet = 0;
    int size = 0;
    while (ackedGet + size < TIMES) {
      ackedGet += size;
      size = watcher.ackAndGet().size();
    }
    watcher.dispose();
    final int pushed = watchable.count.get();
    assertThat(watchable.reflux + ackedGet, is(pushed));
  }

  @Mock
  private Session session;
  @Mock
  public Message<ByteBuffer> message;

  private Watcher<ByteBuffer> watcher;

  private static final int TIMES = 10;

  private final FakeWatchable watchable = new FakeWatchable();

  /**
   * @{link FakeWatchable}
   */
  private final class FakeWatchable implements Watchable<ByteBuffer> {


    @Override
    public void onAvaliable(final Watcher<ByteBuffer> watcher) {
      new Thread(new Runnable() {

        @Override
        public void run() {
          fireOnMessageReceived(watcher);
        }
      }, "FireOnMessageReceivedTask").start();
    }

    protected void fireOnMessageReceived(final Watcher<ByteBuffer> watcher) {
      System.out.println(currentThread().getName() + currentThread().getId());
      for (;;) {
        final Iterator<Message<ByteBuffer>> itr = Collections.singleton(message).iterator();
        final Cursor<Message<ByteBuffer>> cursor = new Cursor<Message<ByteBuffer>>() {

          @Override
          public Message<ByteBuffer> next() {
            if (itr.hasNext()) return itr.next();
            return null;
          }
        };
        watcher.onMessageReceived(cursor);
        if (itr.hasNext()) break;
        count.incrementAndGet();
      }
    }

    private int reflux;
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public Group<ByteBuffer> groupOf(Watcher<ByteBuffer> watcher) {
      return new Group<ByteBuffer>() {

        @Override
        public void reclaim(Session session, List<Message<ByteBuffer>> reflux) {
          FakeWatchable.this.reflux += reflux.size();
        }

        @Override
        public void dispose() {}

      };
    }

    @Override
    public void onHasUselessMessages(Watcher<ByteBuffer> watcher) {}

  }
}
