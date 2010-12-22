package com.taobao.timetunnel.client;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.disk.FileChannelQueueImpl;
import com.taobao.timetunnel.client.message.MessageFactory;

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
		String toSend = "hello timetunnel client, cool and awesome, why so cool, i have no idea, just cool, binggo ";
		for (int i = 0; i < 100; i++) {
			String str =toSend +i + " " + String.valueOf(random.nextInt(10000000));
			byte[] content = str.getBytes(Charset.forName("utf-8"));
			Message m = MessageFactory.getInstance().createMessage(null, content);
			fcq.add(m);
		}
		for (int i = 0; i < 100; i++) {
			Message m1 = MessageFactory.getInstance().createMessageFrom(fcq.get().array());
			System.out.println(m1.getId());
			assertThat(new String(m1.getContent(), Charset.forName("utf-8")), startsWith(toSend + i));
			System.out.println(new String(m1.getContent(), Charset.forName("utf-8")));
		}
		fcq.close();
	}
}
