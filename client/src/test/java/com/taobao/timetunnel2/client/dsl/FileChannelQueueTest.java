package com.taobao.timetunnel2.client.dsl;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.taobao.timetunnel2.client.dsl.disk.FileChannelQueueImpl;
import com.taobao.timetunnel2.client.dsl.message.MessageFactory;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-13
 * 
 */
public class FileChannelQueueTest {
	@Before
	public void preTest() {
		File file = new File("target/FileChannelQueueTest");
		if (!file.exists())
			return;
		for (File f : file.listFiles()) {
			f.delete();
		}
	}

	@Test
	public void queueTest() throws Exception {
		FileChannelQueueImpl fcq = new FileChannelQueueImpl("target/FileChannelQueueTest");
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			String str = "hello " + i + " " + String.valueOf(random.nextInt(10000000));
			byte[] content = str.getBytes(Charset.forName("utf-8"));
			Message m = MessageFactory.getInstance().createMessage(null, content);
			fcq.add(m);
		}
		for (int i = 0; i < 8; i++) {
			Message m1 = MessageFactory.getInstance().createMessageFrom(fcq.get().array());
			System.out.println(m1.getId());
			assertThat(new String(m1.getContent(), Charset.forName("utf-8")), startsWith("hello " + i));
			System.out.println(new String(m1.getContent(), Charset.forName("utf-8")));
		}
		fcq.close();
	}
}
