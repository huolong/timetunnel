package com.taobao.timetunnel.client.disk;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.pub.PubTunnelFactory;
import com.taobao.timetunnel.client.util.ClosedException;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-18
 * 
 */
public class DiskQueueFactory {
	private final static Logger log = Logger.getLogger(DiskQueueFactory.class);
	private static DiskQueueFactory instance = new DiskQueueFactory();
	private final ConcurrentHashMap<String, FutureTask<AsyncQueue>> queueMap;
	private final AtomicBoolean stop;
	private final static PubTunnelFactory ptf = PubTunnelFactory.getInstance();
	private final ReentrantReadWriteLock lock;

	public static DiskQueueFactory getInstance() {
		return instance;
	}

	private DiskQueueFactory() {
		queueMap = new ConcurrentHashMap<String, FutureTask<AsyncQueue>>();
		stop = new AtomicBoolean(false);
		lock = new ReentrantReadWriteLock();
	}

	public AsyncQueue create(Tunnel tunnel) throws ClosedException {
		if (stop.get())
			throw new ClosedException("has been closed", null);
		lock.readLock().lock();
		try {
			FutureTask<AsyncQueue> ft = queueMap.get(tunnel.getName());
			if (ft != null)
				return futureGet(ft);
			FutureTask<AsyncQueue> futureTask = new FutureTask<AsyncQueue>(new AsyncQueueBulider(tunnel));
			FutureTask<AsyncQueue> old = queueMap.putIfAbsent(tunnel.getName(), futureTask);
			if (old == null) {
				old = futureTask;
				old.run();
			}
			return futureGet(old);
		} finally {
			lock.readLock().unlock();
		}
	}

	private AsyncQueue futureGet(FutureTask<AsyncQueue> ft) {
		try {
			return ft.get();
		} catch (Exception e) {
			log.error("{}", e);
		}
		throw new RuntimeException("can not create Async Queue");
	}

	static class AsyncQueueBulider implements Callable<AsyncQueue> {
		private Tunnel tunnel;

		public AsyncQueueBulider(Tunnel tunnel) {
			this.tunnel = tunnel;
		}

		@Override
		public AsyncQueue call() {
			assert (ptf != null);
			AsyncQueue asyncQueue = new AsyncQueue(tunnel, ptf);
			asyncQueue.startSender();
			log.debug("started async sender");
			return asyncQueue;
		}
	}

	public void destory() {
		stop.set(true);
		lock.writeLock().lock();
		try {
			for (Iterator<FutureTask<AsyncQueue>> it = queueMap.values().iterator(); it.hasNext();) {
				try {
					it.next().get().close();
				} catch (Exception i) {
					log.error("{}", i);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
}
