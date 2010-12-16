package com.taobao.timetunnel2.client.dsl;

import com.taobao.timetunnel2.client.dsl.broker.BrokerImpl;
import com.taobao.timetunnel2.client.dsl.broker.LocalBrokerService;
import com.taobao.timetunnel2.client.dsl.broker.PortNum;
import com.taobao.timetunnel2.client.dsl.router.LocalRouterService;
import com.taobao.timetunnel2.client.dsl.router.RouterImpl;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-9
 * 
 */
@SuppressWarnings("static-access")
abstract public class BaseServers {
	BrokerImpl brokerImpl;
	RouterImpl routerImpl;
	String port;
	LocalBrokerService localBrokerService;
	LocalRouterService localRouterService;
	String randomRouterPort;

	public BaseServers() {
		startServers();
	}

	private void startServers() {
		preStartBroker();
		startBroker();
		postStartBroker();
		preStartRouter();
		startRouter();
		postStartRouter();
	}

	abstract void preStartBroker();

	abstract void postStartBroker();

	abstract void preStartRouter();

	abstract void postStartRouter();

	protected void startRouter() {
		randomRouterPort = PortNum.randomPort();
		routerImpl = new RouterImpl();
		localRouterService = new LocalRouterService();
		localRouterService.bootstrap(Integer.parseInt(randomRouterPort), routerImpl);
	}

	protected void startBroker() {
		port = PortNum.randomPort();
		brokerImpl = new BrokerImpl();
		localBrokerService = new LocalBrokerService();
		localBrokerService.bootstrap(Integer.parseInt(port), brokerImpl);
	}
}
