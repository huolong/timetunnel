package com.taobao.timetunnel.reliable;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

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
    final File home = new File("./target/rt");
    DirectoryCleaner.clean(home);
    home.mkdirs();
    reliable = new ByteBufferMessageReliables(home, (1 << 20), (1 << 10)).reliable(category);
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

    assertThat(result, hasItems(3, 4, 5));
  }

  private Reliable<ByteBuffer> reliable;
  @Mock
  private Session session;
  @Mock
  private Category category;

  private final Set<Integer> result = new HashSet<Integer>();
  private final Appendable<ByteBuffer> appendable = new Appendable<ByteBuffer>() {

    @Override
    public void append(final Category category, final Session session, final ByteBuffer message) {
      result.add(message.asReadOnlyBuffer().getInt());
    }
  };
}
