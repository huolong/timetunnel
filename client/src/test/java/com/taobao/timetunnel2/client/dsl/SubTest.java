package com.taobao.timetunnel2.client.dsl;

import static com.taobao.timetunnel2.client.dsl.TimeTunnel2.passport;
import static com.taobao.timetunnel2.client.dsl.TimeTunnel2.tunnel;
import static com.taobao.timetunnel2.client.dsl.TimeTunnel2.use;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import com.taobao.timetunnel2.client.dsl.broker.BrokerImpl.SendContent;
import com.taobao.timetunnel2.client.dsl.impl.Config;
import com.taobao.timetunnel2.client.dsl.impl.Tunnel;
import com.taobao.timetunnel2.client.dsl.message.MessageFactory;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-18
 * 
 */
public class SubTest extends BaseServers {

	@SuppressWarnings("unchecked")
	@Test
	public void sub() {
		Config.getInstance().setRouterServerList("localhost:" + randomRouterPort);
		String brokerUrl = "{\"sessionId\":\"xxxxx\",\"brokerserver\":[\"localhost:" + port + "\"]}";
		routerImpl.setBrokerUrls(brokerUrl);

		Message m = MessageFactory.getInstance().createMessage("t1", "hello".getBytes(Charset.forName("UTF-8")));
		Message m2 = MessageFactory.getInstance().createMessage("t1", "bye".getBytes(Charset.forName("UTF-8")));
		final List<ByteBuffer> toSend = Arrays.asList(ByteBuffer.wrap(m.serialize()), ByteBuffer.wrap(m2.serialize()));
		brokerImpl.setContentGen(new SendContent() {
			@Override
			public List<ByteBuffer> get2Send() {
				return toSend;
			}
		});

		use(passport("hello", "1111"));
		Tunnel t = tunnel("SubTest", false, false, 10, 2);
		SubscribeFuture sub = TimeTunnel2.subscribe(t);
		List<Message> list = sub.get();
		assertThat(list.size(), is(2));
		for (Message got : list) {
			assertThat(got.getId(), anyOf(equalTo(m2.getId()), equalTo(m.getId())));
		}

		brokerImpl.setContentGen(new SendContent() {
			@Override
			public List<ByteBuffer> get2Send() {
				return new ArrayList<ByteBuffer>();
			}
		});
		List<Message> list2 = sub.get(2, TimeUnit.SECONDS);
		assertThat(list2.size(), equalTo(0));

		sub.cancel();
	}

	@SuppressWarnings("static-access")
	@After
	public void clear() {
		localBrokerService.stop();
		localRouterService.stop();
	}

	@Override
	void postStartBroker() {
	}

	@Override
	void postStartRouter() {
	}

	@Override
	void preStartBroker() {
	}

	@Override
	void preStartRouter() {
	}
}
