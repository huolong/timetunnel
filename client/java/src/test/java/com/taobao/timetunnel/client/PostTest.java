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

import org.junit.After;
import org.junit.Test;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.TimeTunnel;
import com.taobao.timetunnel.client.broker.BrokerImpl.Key;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.util.SleepUtils;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-17
 * 
 */
public class PostTest extends BaseServers {

	@Test
	public void post() throws Exception {
		Config.getInstance().setRouterServerList("localhost:" + randomRouterPort);
		String brokerUrl = "{\"sessionId\":\"xxxxx\",\"brokerserver\":[\"localhost:" + port + "\"]}";
		routerImpl.setBrokerUrls(brokerUrl);

		String topic = "PostTest";
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

		byte[] bytes = content.getBytes(Charset.forName("utf-8"));
		TimeTunnel.post(bytes, t);
		SleepUtils.sleep(10);
		received = brokerImpl.getReceived();
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
