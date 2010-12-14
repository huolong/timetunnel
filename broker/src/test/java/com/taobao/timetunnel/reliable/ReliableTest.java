package com.taobao.timetunnel.reliable;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.taobao.timetunnel.Appendable;
import com.taobao.timetunnel.InjectMocksSupport;
import com.taobao.timetunnel.message.Category;
import com.taobao.timetunnel.session.Session;
import com.taobao.util.Bytes;
import com.taobao.util.DirectoryCleaner;

/**
 * {@link ReliableTest}
 * 
 * @author <a href=mailto:jushi@taobao.com>jushi</a>
 * @created 2010-11-18
 * 
 */
public class ReliableTest extends InjectMocksSupport {

  @Before
  public void setUp() throws Exception {
    doReturn("category").when(category).name();
    File home = new File("./target/rt");
    DirectoryCleaner.clean(home);
    home.mkdirs();
    reliable = new ByteBufferMessageReliables(home, 4096).reliable(category);
  }

  @Test
  public void shouldTrimDump() throws Exception {

    reliable.copy(session, Bytes.toBuffer(3));
    reliable.copy(session, Bytes.toBuffer(4));
    reliable.copy(session, Bytes.toBuffer(5));

    reliable.dump(session, Bytes.toBuffer(0));
    reliable.dump(session, Bytes.toBuffer(1));
    reliable.dump(session, Bytes.toBuffer(2));

    reliable.trim(session, 3);

    reliable.dumpTo(appendable);

    verify(appendable, never()).append(category, session, Bytes.toBuffer(0));
    verify(appendable, never()).append(category, session, Bytes.toBuffer(1));
    verify(appendable, never()).append(category, session, Bytes.toBuffer(2));
    verify(appendable).append(category, session, Bytes.toBuffer(3));
    verify(appendable).append(category, session, Bytes.toBuffer(4));
    verify(appendable).append(category, session, Bytes.toBuffer(5));
  }

  private Reliable<ByteBuffer> reliable;
  @Mock
  private Session session;
  @Mock
  private Category category;
  @Mock
  private Appendable<ByteBuffer> appendable;
}
