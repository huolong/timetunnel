package com.taobao.timetunnel2.client.dsl.pub;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.timetunnel2.client.dsl.Message;
import com.taobao.timetunnel2.client.dsl.impl.Tunnel;
import com.taobao.timetunnel2.client.dsl.tt2.Client;
import com.taobao.timetunnel2.client.dsl.url.ServerGroup;
import com.taobao.timetunnel2.client.dsl.url.ThriftUrls;
import com.taobao.timetunnel2.client.dsl.util.ClosedException;
import com.taobao.timetunnel2.client.dsl.util.SleepUtils;
import com.taobao.timetunnel2.client.dsl.util.TType;

/**
 * only one client in tunnel
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-12
 * 
 */
public class PubTunnel {
	private static Logger log = Logger.getLogger(PubTunnel.class);
	private final Tunnel t;
	private final AtomicBoolean closed;
	private Client client;
	private String url;
	private ByteBuffer token;

	public PubTunnel(Tunnel t) {
		this.t = t;
		this.client = null;
		this.closed = new AtomicBoolean(false);
		this.url = null;
		this.token = null;
	}

	private void extractUrl() {
		ServerGroup sg = null;
		try {
			sg = ThriftUrls.getInstance().getUrls(t.getName(), TType.PUB.name(), 0, t.getTimeout(), t.isSequence());
		} catch (Exception e) {
			log.error("{}", e);
			sg = null;
		}
		if (sg == null || sg.getServeUnits() == null || sg.getToken() == null || sg.getServeUnits().get(0) == null || sg.getServeUnits().get(0).getPubMaster() == null) {
			log.error("refresh Url failed, use older one if has");
		} else {
			url = sg.getServeUnits().get(0).getPubMaster();
			token = sg.getTokenInSerialize();
		}
	}

	private void disable() {
		try {
			if (client != null) {
				client.stop();
			}
		} finally {
			client = null;
		}
	}

	public synchronized void destory() {
		closed.set(true);
		disable();
	}

	public void post(Message message) throws ClosedException {
		this.post(ByteBuffer.wrap(message.serialize()));
	}

	public synchronized void post(ByteBuffer bf) throws ClosedException {
		if (closed.get())
			throw new ClosedException("tunnel has been closed", null);
		while (!closed.get()) {
			ensureClient();
			try {
				client.post(t.getName(), this.token, bf);
				log.debug("suc publish use client: " + this.url + " with object: " + this);
				return;
			} catch (Failure e) {
				disable();
				SleepUtils.sleep(5000);
				continue;
			} catch (Exception e) {
				disable();
				SleepUtils.sleep(3000);
				continue;
			}
		}
		throw new ClosedException("tunnel has been closed", null);
	}

	private void ensureClient() throws ClosedException {
		if (client == null) {
			do {
				extractUrl();
			} while (url == null && !closed.get());
			if ((url == null) && closed.get())
				throw new ClosedException("tunnel has been closed", null);
			assert (url != null);
			client = new Client(url);
		}
	}
}
