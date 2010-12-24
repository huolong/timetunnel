package com.taobao.timetunnel.message;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.session.Attribute;
import com.taobao.timetunnel.session.Session;
import com.taobao.util.Bytes;
import com.taobao.util.DirectoryCleaner;
import com.taobao.util.MemoryMonitor;

/**
 * {@link ByteBufferMessageCompactorTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-12-7
 * 
 */
public class ByteBufferMessageCompactorTest extends InjectMocksSupport {
  private final File home = new File("target/bbmct");
  private final MemoryMonitor monitor = new MemoryMonitor(512, 1024);
  private final ByteBuffer content = Bytes.toBuffer("content");
  @Mock
  private Category category;
  @Mock
  private Session publisher;
  @Mock
  private Session subscriber;

  @Before
  public void setUp() throws Exception {
    DirectoryCleaner.clean(home);
    home.mkdirs();
    doReturn(true).when(category).isMessageUselessReadBy(anySetOf(String.class));
    doReturn("category").when(category).name();

    doReturn("sub").when(subscriber).stringValueOf(Attribute.subscriber);
  }

  @Test
  public void shouldNotBeMemoryLeak() throws Exception {
    ByteBufferMessageCompactor compactor =
      new ByteBufferMessageCompactor(monitor, home, (1 << 20), (1 << 10));
    long free = MemoryMonitor.free();
    int cap = 100;
    List<Message<ByteBuffer>> messages = new ArrayList<Message<ByteBuffer>>(cap);
    for (int i = 0; i < cap; i++) {
      messages.add(compactor.createBy(content, category, publisher));
    }

    Iterator<Message<ByteBuffer>> itr = messages.iterator();
    while (itr.hasNext()) {
      itr.next().readBy(subscriber);
      itr.remove();
    }

    // Thread.sleep(500L);
    System.gc();
    Thread.sleep(100L);
    assertThat(MemoryMonitor.free(), is(greaterThan(free)));
  }
}
