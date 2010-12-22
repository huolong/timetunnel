package com.taobao.timetunnel.client.url;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.taobao.timetunnel.client.impl.Authentication;
import com.taobao.timetunnel.client.impl.Config;
import com.taobao.timetunnel.client.util.ClosedException;
import com.taobao.timetunnel.client.util.JsonUtils;
import com.taobao.timetunnel.client.util.RouterUrlUtils;
import com.taobao.timetunnel.client.util.SleepUtils;
import com.taobao.timetunnel.client.util.URLException;
import com.taobao.timetunnel.thrift.router.Constants;
import com.taobao.timetunnel.thrift.router.RouterService;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-10-27
 * 
 */
public class ThriftUrls implements Urls {
	private static final Logger log = Logger.getLogger(ThriftUrls.class);
	private boolean isClosed = false;
	private TTransport transport = null;
	private Map<String, String> prop;
	private RouterService.Client client;
	private String host;
	private int port;
	private static final int MAX_FAILURE_RETRIES = 3;
	private static final ThriftUrls instance = new ThriftUrls();
	private AtomicReference<Authentication> auth;

	private ThriftUrls() {
		this.prop = new HashMap<String, String>();
		auth=new AtomicReference<Authentication>(null);
	}

	public static Urls getInstance() {
		return instance;
	}

	@Override
	public void setAuth(Authentication auth) {
		this.auth.set(auth);
	}

	private void host() {
		String url = RouterUrlUtils.getRandomUrl();
		this.host = url.split(":")[0];
		this.port = Integer.valueOf(url.split(":")[1]);
	}

	private void connect() throws Exception {
		SleepUtils.randomSleep(1000);
		Exception failure = null;
		for (int i = 0; i < MAX_FAILURE_RETRIES; i++) {
			host();
			try {
				transport = new TSocket(newSocket());
				TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
				client = new RouterService.Client(protocol);
				return;
			} catch (Exception e) {
				failure = e;
				log.error("Fail to connect to " + host + " " + port, e);
				try {
					Thread.sleep(3000);
				} catch (Exception ignored) {
				}
			}
		}
		throw failure;
	}

	private Socket newSocket() throws UnknownHostException, IOException, SocketException {
		Socket sk = new Socket(host, port);
		sk.setSoTimeout(Config.getInstance().getRpcTimeOut());
		return sk;
	}

	private void cleanup() {
		if (transport != null)
			try {
				transport.close();
			} finally {
				transport = null;
			}
	}

	@Override
	public synchronized ServerGroup getUrls(String routeKey, String type, int size, int timeout, boolean sequence) throws URLException, ClosedException {
		if (isClosed)
			throw new ClosedException("already closed", null);

		if (auth == null)
			throw new IllegalArgumentException("not use authentication");

		ServerGroup sg = null;
		String brokerStr = null;
		try {
			if (transport == null)
				connect();
			prop.put(Constants.LOCAL_HOST, Config.getInstance().getAppName());
			prop.put(Constants.RECVWINSIZE, String.valueOf(size));
			prop.put(Constants.TYPE, type);
			prop.put(Constants.TIMEOUT, String.valueOf(timeout));
			brokerStr = client.getBroker(auth.get().getUserName(), auth.get().getPassword(), routeKey, sequence ? "1" : "0", prop);
			sg = bulidSG(brokerStr);
		} catch (Exception e) {
			cleanup();
			log.error("get server group failed", e);
			throw new URLException("get server group failed", e);
		}
		return sg;
	}

	private ServerGroup bulidSG(String brokerStr) {
		String token = "";
		List<ServerUnit> sus = new ArrayList<ServerUnit>();
		try {
			RouterTable a = JsonUtils.parse(brokerStr).toAInstance(RouterTable.class);
			token = a.getToken();
			String[] bs = a.getBrokerserver();
			for (String s : bs) {
				sus.add(new ServerUnit(s, ""));
			}
		} catch (Exception e) {
			log.error("bulid server group failed and str: " + brokerStr, e);
			throw new RuntimeException("bulid server group failed and str: " + brokerStr);
		}
		return new ServerGroup(sus, token);
	}

	@Override
	public synchronized void stop() {
		cleanup();
		this.isClosed = true;
	}

	public static void main(String[] args) throws URLException, ClosedException {
		String testUrl = "{\"sessionId\":\"8045f5cb0521c82598584f3151b1a1d5\",\"brokerserver\":[\"{\\\"master\\\":\\\"dwbasis130001.sqa.cm4:39903\\\",\\\"slave\\\":[\\\"er1\\\", \\\"er2\\\"]}\", \"{\\\"master\\\":\\\"dwbasis130001.sqa.cm4:39903\\\",\\\"slave\\\":[\\\"er1\\\", \\\"er2\\\"]}\"]}";
		System.out.println(testUrl);
	}
}

class RouterTable {
	private String sessionId;
	private String[] brokerserver;

	public String getToken() {
		return sessionId;
	}

	public void setToken(String token) {
		this.sessionId = token;
	}

	public String[] getBrokerserver() {
		return brokerserver;
	}

	public void setBrokerserver(String[] bsStr) {
		this.brokerserver = bsStr;
	}

	@Override
	public String toString() {
		return "token id: " + sessionId + " brokers: " + brokerserver[0];
	}
}
