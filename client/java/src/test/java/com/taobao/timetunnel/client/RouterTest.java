package com.taobao.timetunnel.client;

import static com.taobao.timetunnel.client.TimeTunnel.passport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.timetunnel.client.TimeTunnel;
import com.taobao.timetunnel.client.broker.PortNum;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.router.LocalRouterService;
import com.taobao.timetunnel.client.router.RouterImpl;
import com.taobao.timetunnel.client.url.ServerGroup;
import com.taobao.timetunnel.client.url.ThriftUrls;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-18
 * 
 */
public class RouterTest {

	private RouterImpl routerImpl;
	private String randomRouterPort;
	private LocalRouterService localRouterService;

	@SuppressWarnings("static-access")
	@Before
	public void init() {
		randomRouterPort = PortNum.randomPort();
		routerImpl = new RouterImpl();
		localRouterService = new LocalRouterService();
		localRouterService.bootstrap(Integer.parseInt(randomRouterPort), routerImpl);
	}

	@Test
	public void testname() throws Exception {
		Config.getInstance().setRouterServerList("localhost:" + randomRouterPort);
		String randomPort = PortNum.randomPort();
		String brokerUrl = "{\"sessionId\":\"xxxxx\",\"brokerserver\":[\"localhost:" + randomPort + "\"]}";
		routerImpl.setBrokerUrls(brokerUrl);

		String user = "tt";
		String pwd = "2";
		TimeTunnel.use(passport(user, pwd));
		String topic = "test1";
		String type = "PUB";
		int recvSize = 0;
		int timout = 1800;
		boolean sequence = false;
		ServerGroup urls = null;
		while (urls == null) {
			try {
				urls = ThriftUrls.getInstance().getUrls(topic, type, recvSize, timout, sequence);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}

		System.out.println(urls);
		assertThat("xxxxx", equalTo(urls.getToken()));
		assertThat("localhost:" + randomPort, equalTo(urls.getServeUnits().get(0).getPubMaster()));
		assertThat("localhost:" + randomPort, equalTo(urls.getServeUnits().get(0).getSubMaster()));
		assertThat(topic, equalTo(routerImpl.getReq().topic));
	}

	@SuppressWarnings("static-access")
	@After
	public void clear() {
		localRouterService.stop();
	}
}
