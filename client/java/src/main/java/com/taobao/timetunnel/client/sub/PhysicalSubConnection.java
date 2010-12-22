package com.taobao.timetunnel.client.sub;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.tt2.Client;
import com.taobao.timetunnel.thrift.gen.Failure;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-25
 * 
 */
public class PhysicalSubConnection implements Comparable<PhysicalSubConnection> {
	private static Logger log = Logger.getLogger(PhysicalSubConnection.class);
	private final Client client;
	private final AtomicInteger ref;
	private final String url;

	public PhysicalSubConnection(String url) {
		this.ref = new AtomicInteger(0);
		this.url = url;
		this.client = new Client(url);
	}

	public void addRef() {
		this.ref.incrementAndGet();
	}

	public String getUrl() {
		return url;
	}

	public synchronized boolean delRef() {
		if (this.ref.decrementAndGet() == 0) {
			close();
			return true;
		}
		return false;
	}

	public List<ByteBuffer> ackAndGet(String category, ByteBuffer token) throws Failure, Exception {
		return client.ackAndGet(category, token);
	}

	public void close() {
		try {
			this.client.stop();
		} catch (Exception i) {
			log.error("close failed:", i);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PhysicalSubConnection))
			return false;
		PhysicalSubConnection that = (PhysicalSubConnection) obj;
		return this.url.equals(that.url);
	}

	@Override
	public int hashCode() {
		return this.url.hashCode();
	}

	@Override
	public int compareTo(PhysicalSubConnection o) {
		return this.url.compareTo(o.url);
	}

}
