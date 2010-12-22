package com.taobao.timetunnel.client;

import static com.taobao.timetunnel.client.TimeTunnel.passport;
import static com.taobao.timetunnel.client.TimeTunnel.tunnel;
import static com.taobao.timetunnel.client.TimeTunnel.use;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.TimeTunnel;
import com.taobao.timetunnel.client.broker.BrokerImpl;
import com.taobao.timetunnel.client.broker.LocalBrokerService;
import com.taobao.timetunnel.client.broker.PortNum;
import com.taobao.timetunnel.client.broker.BrokerImpl.Key;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.router.LocalRouterService;
import com.taobao.timetunnel.client.router.RouterImpl;
import com.taobao.timetunnel.client.util.SleepUtils;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-18
 * 
 */
public class PostFailTest2 extends BaseServers {

	private static CountDownLatch latch = new CountDownLatch(2);
	private ScheduledExecutorService sc2;
	private ScheduledExecutorService sc;

	@Test
	public void post() throws Exception {
		latch.await();
		Config.getInstance().setRouterServerList("localhost:" + randomRouterPort);
		String brokerUrl = "{\"sessionId\":\"xxxxx\",\"brokerserver\":[\"localhost:" + port + "\"]}";
		routerImpl.setBrokerUrls(brokerUrl);

		String topic = "PostFailTest2";
		use(passport("hello", "1111"));
		Tunnel t = tunnel(topic);
		String content = "asfdasfadfafasfas";

		TimeTunnel.post(content, t);
		ConcurrentHashMap<Key, Message> received = brokerImpl.getReceived();
		SleepUtils.sleep(10);
		assertThat(received.size(), is(1));
		for (Message m : received.values()) {
			assertThat(new String(m.getContent(), Charset.forName("utf-8")), equalTo(content));
		}
		received.clear();

		SleepUtils.sleep(5000);
		byte[] bytes = content.getBytes(Charset.forName("utf-8"));
		TimeTunnel.post(bytes, t);
		received = brokerImpl.getReceived();
		SleepUtils.sleep(10);
		assertThat(received.size(), is(1));
		for (Message m : received.values()) {
			assertThat(new String(m.getContent(), Charset.forName("utf-8")), equalTo(content));
		}
		received.clear();

		TimeTunnel.post(bytes, "localtestip", 8888L, new HashMap<String, String>(), t);
		received = brokerImpl.getReceived();
		SleepUtils.sleep(10);
		assertThat(received.size(), is(1));
		for (Message m : received.values()) {
			assertThat(m.getIpAddress(), equalTo("localtestip"));
		}
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
		sc.scheduleWithFixedDelay(new Runnable() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				localRouterService.openClose(Integer.parseInt(randomRouterPort), routerImpl);
				latch.countDown();
			}
		}, 0, 3, TimeUnit.SECONDS);
	}

	protected void startBroker() {
		port = PortNum.randomPort();
		brokerImpl = new BrokerImpl();
		localBrokerService = new LocalBrokerService();
		sc2 = Executors.newSingleThreadScheduledExecutor();
		sc2.scheduleWithFixedDelay(new Runnable() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				localBrokerService.openClose(Integer.parseInt(port), brokerImpl);
				latch.countDown();
			}
		}, 0, 4, TimeUnit.SECONDS);
	}

}
