package com.taobao.timetunnel.client.sub;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.url.ServerGroup;
import com.taobao.timetunnel.client.url.ServerUnit;
import com.taobao.timetunnel.client.url.ThriftUrls;
import com.taobao.timetunnel.client.util.ClosedException;
import com.taobao.timetunnel.client.util.SleepUtils;
import com.taobao.timetunnel.client.util.TType;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-15
 * 
 */
public class VirtualSubConnection {
	private static Logger log = Logger.getLogger(VirtualSubConnection.class);
	private final Tunnel t;
	private ServerGroup sg;
	private final List<PhysicalSubConnection> physicCon;
	private final AtomicBoolean stop;
	private final ReentrantLock lock;
	private int index = 0;

	public VirtualSubConnection(Tunnel t) {
		this.t = t;
		this.sg = null;
		this.stop = new AtomicBoolean(false);
		this.physicCon = new LinkedList<PhysicalSubConnection>();
		this.lock = new ReentrantLock();
	}

	private void constuct() throws ClosedException {
		while (sg == null && !stop.get()) {
			extractUrl();
		}
		if (sg == null)
			throw new ClosedException("has been closed", null);
		addConnection(sg.getServeUnits().iterator());
	}

	private PhysicalSubConnection next() throws ClosedException {
		lock.lock();
		PhysicalSubConnection ret = null;
		try {
			int size = physicCon.size();
			if (size == 0) {
				constuct();
			}
			if (index >= size)
				index = 0;
			ret = physicCon.get(index++);
		} finally {
			lock.unlock();
		}
		return ret;
	}

	public List<ByteBuffer> ackAndGet() throws ClosedException {
		if (stop.get())
			throw new ClosedException("has been closed", null);
		while (!stop.get()) {
			try {
				return next().ackAndGet(t.getName(), sg.getTokenInSerialize());
			} catch (ClosedException e) {
				log.error("{}", e);
				continue;
			} catch (Exception e) {
				reConstruct();
				SleepUtils.sleep(3000);
				continue;
			}
		}
		throw new ClosedException("has been closed", null);
	}

	private void reConstruct() {
		lock.lock();
		try {
			ServerGroup oldSG = sg;
			extractUrl();
			if (oldSG.equals(sg))
				return;
			Set<ServerUnit> notIn = oldSG.notIn(sg);
			deleteConnection(notIn.iterator());
			Set<ServerUnit> notIn2 = sg.notIn(oldSG);
			addConnection(notIn2.iterator());
		} finally {
			lock.unlock();
		}
	}

	private void deleteConnection(Iterator<ServerUnit> abandonSUIt) {
		for (; abandonSUIt.hasNext();) {
			String toRemoveUrl = abandonSUIt.next().getSubMaster();
			log.error("remove not config url: " + toRemoveUrl);
			physicCon.remove(new PhysicalSubConnection(toRemoveUrl));
			PhysicalSubConnectionPool.getInstance().checkIn(toRemoveUrl);
		}
	}

	private void addConnection(Iterator<ServerUnit> newSUIt) {
		for (; newSUIt.hasNext();) {
			String toAddUrl = newSUIt.next().getSubMaster();
			log.error("add new config url: " + toAddUrl);
			physicCon.add(PhysicalSubConnectionPool.getInstance().checkOut(toAddUrl));
		}
	}

	private void extractUrl() {
		ServerGroup newSG = null;
		try {
			newSG = ThriftUrls.getInstance().getUrls(t.getName(), TType.SUB.name(), t.getMaxRcvSize(), t.getTimeout(), t.isSequence());
		} catch (Exception e) {
			log.error("{}", e);
			newSG = null;
		}
		if (newSG == null || newSG.getServeUnits() == null || newSG.getToken() == null || newSG.getServeUnits().size() == 0) {
			log.error("get Url failed, use older one is has");
		} else {
			sg = newSG;
		}
	}

	public void stop() {
		stop.set(true);
		lock.lock();
		try {
			for (PhysicalSubConnection c : physicCon) {
				PhysicalSubConnectionPool.getInstance().checkIn(c.getUrl());
			}
			physicCon.clear();
		} finally {
			lock.unlock();
		}
	}
}
