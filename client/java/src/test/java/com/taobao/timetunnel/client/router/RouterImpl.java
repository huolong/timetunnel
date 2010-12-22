package com.taobao.timetunnel.client.router;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import com.taobao.timetunnel.thrift.router.RouterException;
import com.taobao.timetunnel.thrift.router.RouterService.Iface;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-16
 * 
 */
public class RouterImpl implements Iface {
	private static Logger log = Logger.getLogger(RouterImpl.class);
	private String brokerUrl;

	public static class Request {
		public String user;
		public String pwd;
		public String topic;
		public String apply;
		public Map<String, String> prop;

		public Request(String user, String pwd, String topic, String apply, Map<String, String> prop) {
			super();
			this.user = user;
			this.pwd = pwd;
			this.topic = topic;
			this.apply = apply;
			this.prop = prop;
		}

	}

	private Request req;

	public RouterImpl() {
	}

	@Override
	public String getBroker(String user, String pwd, String topic, String apply, Map<String, String> prop) throws RouterException, TException {
		req = new Request(user, pwd, topic, apply, prop);
		log.error("retrun from router: " + brokerUrl);
		return brokerUrl;
	}

	public Request getReq() {
		return req;
	}

	public void setBrokerUrls(String brokerUrl) {
		if (brokerUrl == null)
			this.brokerUrl = "{\"sessionId\":\"xxxxx\",\"brokerserver\":[\"localhost:" + 10000 + "\"]}";
		else
			this.brokerUrl = brokerUrl;
	}
}
