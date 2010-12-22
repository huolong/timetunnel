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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.SubscribeFuture;
import com.taobao.timetunnel.client.TimeTunnel;
import com.taobao.timetunnel.client.broker.BrokerImpl;
import com.taobao.timetunnel.client.broker.LocalBrokerService;
import com.taobao.timetunnel.client.broker.PortNum;
import com.taobao.timetunnel.client.broker.BrokerImpl.SendContent;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.message.MessageFactory;
import com.taobao.timetunnel.client.router.LocalRouterService;
import com.taobao.timetunnel.client.router.RouterImpl;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-18
 * 
 */
public class SubFailTest extends BaseServers {

	private ScheduledExecutorService sc2;
	private ScheduledExecutorService sc;

	@SuppressWarnings("unchecked")
	@Test
	public void sub() throws Exception {
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
		Tunnel t = tunnel("SubFailTest", false, false, 10, 2);
		SubscribeFuture sub = TimeTunnel.subscribe(t);
		List<Message> list = sub.get();
		assertThat(list.size(), is(2));
		for (Message got : list) {
			assertThat(got.getId(), anyOf(equalTo(m2.getId()), equalTo(m.getId())));
		}

		sub.cancel();
	}

	@SuppressWarnings("static-access")
	@After
	public void clear() {
		localBrokerService.stop();
		localRouterService.stop();
		sc.shutdownNow();
		sc2.shutdownNow();
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

	protected void startRouter() {
		randomRouterPort = PortNum.randomPort();
		routerImpl = new RouterImpl();
		localRouterService = new LocalRouterService();
		sc = Executors.newSingleThreadScheduledExecutor();
		sc.schedule(new Runnable() {

			@SuppressWarnings("static-access")
			@Override
			public void run() {
				localRouterService.bootstrap(Integer.parseInt(randomRouterPort), routerImpl);
			}
		}, 4, TimeUnit.SECONDS);
	}

	protected void startBroker() {
		port = PortNum.randomPort();
		brokerImpl = new BrokerImpl();
		localBrokerService = new LocalBrokerService();
		sc2 = Executors.newSingleThreadScheduledExecutor();
		sc2.schedule(new Runnable() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				localBrokerService.bootstrap(Integer.parseInt(port), brokerImpl);
			}
		}, 3, TimeUnit.SECONDS);
	}

}
