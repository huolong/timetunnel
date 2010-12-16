package com.taobao.timetunnel2.client.dsl.tt2;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.taobao.timetunnel.thrift.gen.ExternalService;
import com.taobao.timetunnel.thrift.gen.Failure;
import com.taobao.timetunnel2.client.dsl.impl.Config;
import com.taobao.timetunnel2.client.dsl.util.ClosedException;
import com.taobao.timetunnel2.client.dsl.util.SleepUtils;

public class Client {
	private static final Logger log = Logger.getLogger(Client.class);
	private final static int MAX_FAILURE_RETRIES = 3;
	private TTransport transport;
	private boolean isClosed = false;
	private ExternalService.Client client;
	private final String host;
	private final int port;
	private String url;

	public Client(String url) {
		this.transport = null;
		this.host = url.split(":")[0];
		this.port = Integer.valueOf(url.split(":")[1]);
		this.url = url;
	}

	private void connect() throws Exception {
		SleepUtils.randomSleep(2000);
		Exception failure = null;
		for (int i = 0; i < MAX_FAILURE_RETRIES; i++)
			try {
				transport = new TSocket(newSocket());
				TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
				client = new ExternalService.Client(protocol);
				return;
			} catch (Exception e) {
				failure = e;
				log.error("Fail to connect to " + host + " " + port, e);
				try {
					Thread.sleep(3000);
				} catch (Exception ignored) {
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

	public synchronized void post(String category, ByteBuffer token, ByteBuffer message) throws Failure, Exception {
		if (isClosed)
			throw new ClosedException("client closed", null);
		try {
			if (transport == null)
				connect();
			if (!transport.isOpen())
				cleanup();
			else
				client.post(category, token, message);
		} catch (Failure e) {
			cleanup();
			log.error("{}", e);
			throw e;
		} catch (Exception e) {
			cleanup();
			log.error("{}", e);
			throw e;
		}
	}

	public synchronized List<ByteBuffer> ackAndGet(String category, ByteBuffer token) throws Failure, Exception {
		if (isClosed)
			throw new ClosedException("client closed", null);
		try {
			if (transport == null)
				connect();
			return client.ackAndGet(category, token);
		} catch (Failure e) {
			cleanup();
			log.error("{}", e);
			throw e;
		} catch (Exception e) {
			cleanup();
			log.error("{}", e);
			throw e;
		}
	}

	public synchronized void stop() {
		cleanup();
		this.isClosed = true;
	}

	public String getUrl() {
		return url;
	}

}
