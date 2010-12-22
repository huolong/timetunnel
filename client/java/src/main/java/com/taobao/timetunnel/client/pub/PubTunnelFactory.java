package com.taobao.timetunnel.client.pub;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.util.ClosedException;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-12
 * 
 */
public class PubTunnelFactory {
	private static Logger log = Logger.getLogger(PubTunnelFactory.class);
	private static PubTunnelFactory instance = new PubTunnelFactory();
	private ConcurrentHashMap<Tunnel, FutureTask<PubTunnel>> map;
	private final ReentrantReadWriteLock lock;
	private final AtomicBoolean stop;

	private PubTunnelFactory() {
		map = new ConcurrentHashMap<Tunnel, FutureTask<PubTunnel>>();
		lock = new ReentrantReadWriteLock();
		stop = new AtomicBoolean(false);
	}

	public static PubTunnelFactory getInstance() {
		return instance;
	}

	public PubTunnel get(Tunnel t) throws ClosedException {
		if (stop.get())
			throw new ClosedException("has been closed", null);
		lock.readLock().lock();
		try {
			FutureTask<PubTunnel> futureTask = map.get(t);
			if (futureTask != null)
				return futureGet(futureTask);
			else {
				FutureTask<PubTunnel> newTask = new FutureTask<PubTunnel>(new Machine(t));
				FutureTask<PubTunnel> old = map.putIfAbsent(t, newTask);
				if (old == null) {
					old = newTask;
					old.run();
				}
				return futureGet(old);
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	private PubTunnel futureGet(FutureTask<PubTunnel> ft) {
		try {
			return ft.get();
		} catch (Exception e) {
			// can not reach here
			log.error("{}", e);
		}
		// can not reach here
		throw new RuntimeException("can not create PubTunnel");
	}

	public void destory() {
		stop.set(true);
		lock.writeLock().lock();
		try {
			for (FutureTask<PubTunnel> v : map.values()) {
				futureGet(v).destory();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	static class Machine implements Callable<PubTunnel> {
		private final Tunnel t;

		public Machine(Tunnel t) {
			this.t = t;
		}

		@Override
		public PubTunnel call() {
			return new PubTunnel(t);
		}
	}

}
