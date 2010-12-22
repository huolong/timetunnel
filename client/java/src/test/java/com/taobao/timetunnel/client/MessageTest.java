package com.taobao.timetunnel.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.message.MessageFactory;
import com.taobao.timetunnel.client.message.MessagePropKeyEnum;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-22
 * 
 */
public class MessageTest {
	@Test
	public void testCreateMessage() throws Exception {
		String topic = "M1";
		String content = "helloM1";

		Message m1 = MessageFactory.getInstance().createMessage(topic, content.getBytes(Charset.forName("utf-8")));
		assertThat(m1.getTopic(), equalTo(topic));
		assertThat(new String(m1.getContent(), Charset.forName("utf-8")), equalTo(content));
		Map<String, String> props = new HashMap<String, String>();
		props.put("TESTKEY", "COOL");
		Message m2 = MessageFactory.getInstance().createMessage(topic, content.getBytes(Charset.forName("utf-8")), props);
		assertThat(props.get("TESTKEY"), equalTo(m2.getProperty("TESTKEY")));
		Message m3 = MessageFactory.getInstance().createMessage(topic, content.getBytes(Charset.forName("utf-8")), "localtesthost", 10);
		assertThat(m3.getCreatedTime(), equalTo(10L));
		assertThat(m3.getIpAddress(), equalTo("localtesthost"));
		Message m4 = MessageFactory.getInstance().createMessage(topic, content.getBytes(Charset.forName("utf-8")), "localtesthost", 10, props);
		assertThat(props.get("TESTKEY"), equalTo(m4.getProperty("TESTKEY")));
		System.out.println(m4.getAllProperties());
		printM(m4);
	}

	@Test
	public void testMessageCompz() throws Exception {
		String topic = "MC";
		String content = "helloMC";
		Map<String, String> props = new HashMap<String, String>();
		Message m4 = MessageFactory.getInstance().createMessage(topic, content.getBytes(Charset.forName("utf-8")), "localtesthost", 10, props);
		assertThat(false, equalTo(m4.isCompressed()));
		m4.compress();
		assertThat(true, equalTo(m4.isCompressed()));
		m4.decompress();
		assertThat(false, equalTo(m4.isCompressed()));
		printM(m4);
	}

	@Test
	public void testMessageSeri() throws Exception {
		String topic = "MS";
		String content = "helloMS";
		Map<String, String> props = new HashMap<String, String>();
		Message m6 = MessageFactory.getInstance().createMessage(topic, content.getBytes(Charset.forName("utf-8")), "localtesthost", 10, props);
		Message m7 = MessageFactory.getInstance().createMessageFrom(m6.serialize());
		assertThat(m6, equalTo(m7));
		printM(m6);
	}

	private void printM(Message m) throws UnsupportedEncodingException {
		System.out.println(m.getCreatedTime());
		System.out.println(m.getId());
		System.out.println(m.getIpAddress());
		System.out.println(m.getProperty(MessagePropKeyEnum.COMPRESSED.getToken()));
		System.out.println(m.getProperty(MessagePropKeyEnum.COMPRESSALGO.getToken()));
		System.out.println(m.getTopic());
		System.out.println(m.getAllProperties());
		System.out.println(new String(m.getContent(), "utf-8"));
		System.out.println("@@@@end of print@@@@");
	}
}
