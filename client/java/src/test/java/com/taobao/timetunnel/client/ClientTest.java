package com.taobao.timetunnel.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.broker.BrokerImpl;
import com.taobao.timetunnel.client.broker.LocalBrokerService;
import com.taobao.timetunnel.client.broker.PortNum;
import com.taobao.timetunnel.client.broker.BrokerImpl.Key;
import com.taobao.timetunnel.client.broker.BrokerImpl.SendContent;
import com.taobao.timetunnel.client.message.MessageFactory;
import com.taobao.timetunnel.client.tt2.Client;
import com.taobao.timetunnel.client.util.BytesUtil;
import com.taobao.timetunnel.thrift.gen.Failure;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-18
 * 
 */
@SuppressWarnings("static-access")
public class ClientTest {
	private static BrokerImpl brokerImpl;
	private static String port;
	private static Client c;
	private static String topic;
	private static ByteBuffer token;
	private static LocalBrokerService localBrokerService;

	@BeforeClass
	public static void init() {
		port = PortNum.randomPort();
		brokerImpl = new BrokerImpl();
		localBrokerService = new LocalBrokerService();
		localBrokerService.bootstrap(Integer.parseInt(port), brokerImpl);
		c = new Client("localhost:" + port);
		topic = "ClientTest";
		token = ByteBuffer.wrap("tokenxxx".getBytes(Charset.forName("UTF-8")));
	}

	@Test
	public void rawClientPubTest() throws Exception {
		Message m = MessageFactory.getInstance().createMessage("t1", "hello".getBytes(Charset.forName("UTF-8")));
		c.post(topic, token, ByteBuffer.wrap(m.serialize()));
		Key k = new Key(topic, token, m.getId());
		assertThat(brokerImpl.getReceived().containsKey(k), is(true));
	}

	@Test
	public void rawClientGetTest() throws Failure, Exception {
		Message m = MessageFactory.getInstance().createMessage("t1", "hello".getBytes(Charset.forName("UTF-8")));
		final List<ByteBuffer> toSend = Arrays.asList(ByteBuffer.wrap(m.serialize()));
		brokerImpl.setContentGen(new SendContent() {

			@Override
			public List<ByteBuffer> get2Send() {
				return toSend;
			}
		});
		List<ByteBuffer> ackAndGet = c.ackAndGet(topic, token);
		assertThat(ackAndGet.size(), is(1));
		for (ByteBuffer b : ackAndGet) {
			Message got = MessageFactory.getInstance().createMessageFrom(BytesUtil.toBytes(b));
			assertThat(got.getId(), is(m.getId()));
		}
	}

	@AfterClass
	public static void clear() {
		c.stop();
		localBrokerService.stop();
	}
}
