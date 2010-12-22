package com.taobao.timetunnel.client.sub;

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
 * @created 2010-11-15
 * 
 */
public class VirtualSubConnectionFactory {
	private static Logger log = Logger.getLogger(VirtualSubConnectionFactory.class);
	private static VirtualSubConnectionFactory instance = new VirtualSubConnectionFactory();
	private final ConcurrentHashMap<Tunnel, FutureTask<VirtualSubConnection>> pool;
	private final AtomicBoolean stop;
	private final ReentrantReadWriteLock lock;

	private VirtualSubConnectionFactory() {
		pool = new ConcurrentHashMap<Tunnel, FutureTask<VirtualSubConnection>>();
		stop = new AtomicBoolean(false);
		lock = new ReentrantReadWriteLock();
	}

	public static VirtualSubConnectionFactory getInstance() {
		return instance;
	}

	public VirtualSubConnection create(Tunnel t) throws ClosedException {
		if (stop.get())
			throw new ClosedException("has been closed", null);
		lock.readLock().lock();
		try {
			FutureTask<VirtualSubConnection> futureTask = pool.get(t);
			if (futureTask != null)
				return futureGet(futureTask);
			else {
				FutureTask<VirtualSubConnection> newTask = new FutureTask<VirtualSubConnection>(new Machine(t));
				FutureTask<VirtualSubConnection> old = pool.putIfAbsent(t, newTask);
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

	private VirtualSubConnection futureGet(FutureTask<VirtualSubConnection> ft) {
		try {
			return ft.get();
		} catch (Exception e) {
			// can't reach here
			log.error("{}", e);
		}
		// can't reach here
		throw new RuntimeException("can not create VirtualSubConnection");
	}

	static class Machine implements Callable<VirtualSubConnection> {
		private final Tunnel t;

		public Machine(Tunnel t) {
			this.t = t;
		}

		@Override
		public VirtualSubConnection call() {
			return new VirtualSubConnection(t);
		}
	}

	public void destory() {
		stop.set(true);
		lock.writeLock().lock();
		try {
			PhysicalSubConnectionPool.getInstance().stop();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void remove(Tunnel t) {
		lock.writeLock().lock();
		try {
			FutureTask<VirtualSubConnection> removed = pool.remove(t);
			futureGet(removed).stop();
		} finally {
			lock.writeLock().unlock();
		}
	}

}
