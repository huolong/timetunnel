package com.taobao.timetunnel.message;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.mockito.Mock;

import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.session.Session;
import com.taobao.timetunnel.swap.Freezer;
import com.taobao.timetunnel.swap.Point;

/**
 * MessageTest
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-12
 * 
 */
public class MessageTest extends InjectMocksSupport {
  @Test
  public void shouldExpiredAfterAlreadyReadByAllReaders() throws Exception {
    doReturn(true).when(category).isMessageUselessReadBy(anySetOf(String.class));
    final Message<ByteBuffer> message = factory.createBy(buffer, category, publisher);
    assertThat(message.isUseless(), is(true));
  }

  @Test
  public void shouldExpiredAfterExpiration() throws Exception {
    doReturn(true).when(category).isMessageExpiredAfter(anyLong());
    final Message<ByteBuffer> message = factory.createBy(buffer, category, publisher);
    assertThat(message.isExpired(), is(true));
  }

  @Test
  public void shouldGetContentFromFreezer() throws Exception {

    final Message<ByteBuffer> message = factory.createBy(buffer, category, publisher);

    doReturn(point).when(freezer).freeze(buffer);
    doReturn(buffer).when(point).get();

    message.freezeBy(freezer);
    verify(freezer).freeze(buffer);

    assertThat(message.content(), is(buffer));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldGetExceptionAfterMessageDisposed() throws Exception {
    final Message<ByteBuffer> message = factory.createBy(buffer, category, publisher);
    message.dispose();
    message.content();
  }

  @Test
  public void shouldRemoveContentInFreezerAfterFrozenMessageDisposed() throws Exception {
    doReturn(point).when(freezer).freeze(buffer);
    final Message<ByteBuffer> message = factory.createBy(buffer, category, publisher);
    message.freezeBy(freezer);
    message.dispose();
    verify(point).clear();
  }

  private final ByteBuffer buffer = ByteBuffer.wrap("content".getBytes());
  private final MessageFactory<ByteBuffer> factory = new ByteBufferMessageFactory();
  
  @Mock
  private Point<ByteBuffer> point;
  @Mock
  private Category category;
  @Mock
  private Session publisher;
  @Mock
  private Freezer<ByteBuffer> freezer;
}
