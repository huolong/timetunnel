package com.taobao.timetunnel.client;

import static com.taobao.timetunnel.client.TimeTunnel.passport;
import static com.taobao.timetunnel.client.TimeTunnel.tunnel;
import static com.taobao.timetunnel.client.TimeTunnel.use;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Test;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.SubscribeFuture;
import com.taobao.timetunnel.client.TimeTunnel;
import com.taobao.timetunnel.client.broker.BrokerImpl.SendContent;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.message.MessageFactory;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-18
 * 
 */
@SuppressWarnings("unchecked")
public class MThreadsSubTest extends BaseServers {

	@Test
	public void post() throws Exception {
		Config.getInstance().setRouterServerList("localhost:" + randomRouterPort);
		String brokerUrl = "{\"sessionId\":\"xxxxx\",\"brokerserver\":[\"localhost:" + port + "\"]}";
		routerImpl.setBrokerUrls(brokerUrl);

		final Message m = MessageFactory.getInstance().createMessage("t1", "hello".getBytes(Charset.forName("UTF-8")));
		final Message m2 = MessageFactory.getInstance().createMessage("t1", "bye".getBytes(Charset.forName("UTF-8")));
		final List<ByteBuffer> toSend = Arrays.asList(ByteBuffer.wrap(m.serialize()), ByteBuffer.wrap(m2.serialize()));
		brokerImpl.setContentGen(new SendContent() {
			@Override
			public List<ByteBuffer> get2Send() {
				return toSend;
			}
		});

		use(passport("hello", "1111"));
		final Tunnel t = tunnel("MThreadsSubTest1", false, false, 10);
		final Tunnel t3 = tunnel("MThreadsSubTest2", false, false, 10);
		final SubscribeFuture sub = TimeTunnel.subscribe(t);
		final SubscribeFuture sub2 = TimeTunnel.subscribe(t3);

		ExecutorService pool = Executors.newFixedThreadPool(10);
		final CountDownLatch latch = new CountDownLatch(500);
		for (int i = 0; i < 500; i++) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					List<Message> list = sub.get();
					assertThat(list.size(), is(2));
					for (Message got : list) {
						assertThat(got.getId(), anyOf(equalTo(m2.getId()), equalTo(m.getId())));
					}
					list = sub2.get();
					assertThat(list.size(), is(2));
					for (Message got : list) {
						assertThat(got.getId(), anyOf(equalTo(m2.getId()), equalTo(m.getId())));
					}
					latch.countDown();
				}
			});
		}
		latch.await();
		sub.cancel();
		sub2.cancel();
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
