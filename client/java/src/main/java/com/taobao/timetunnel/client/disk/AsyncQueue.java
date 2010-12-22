package com.taobao.timetunnel.client.disk;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.taobao.timetunnel.client.Message;
import com.taobao.timetunnel.client.impl.Tunnel;
import com.taobao.timetunnel.client.pub.PubTunnelFactory;
import com.taobao.timetunnel.client.util.ClosedException;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-10-18
 * 
 */
public class AsyncQueue implements FileChannelQueue {
	private final Tunnel tunnel;
	private final FileChannelQueue queue;
	private final ExecutorService sender;
	private final AtomicBoolean stopped = new AtomicBoolean(false);
	private final PubTunnelFactory ptf;
	private static Logger log = Logger.getLogger(AsyncQueue.class);

	public AsyncQueue(final Tunnel tunnel, PubTunnelFactory ptf) {
		this.tunnel = tunnel;
		this.ptf = ptf;
		queue = new FileChannelQueueImpl(tunnel.getName());
		sender = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "asyncQueue_sender_" + tunnel.getName());
			}
		});
	}

	public void startSender() {
		sender.submit(new SendTask());
	}

	@Override
	public void add(Message m) throws ClosedException {
		if (stopped.get())
			throw new ClosedException("already closed", null);
		queue.add(m);
	}

	@Override
	public void close() {
		stopped.set(true);
		queue.close();
		// send out interrupt to SendTask
		sender.shutdown();
	}

	@Override
	public ByteBuffer get() {
		return queue.get();
	}

	class SendTask implements Runnable {
		@Override
		public void run() {
			while (!stopped.get()) {
				ByteBuffer m = get();
				if (m == null) {
					log.error("null message retrive from filequeue, due to interurpt queue.take");
					continue;
				}
				log.debug("load messsage from disk: " + tunnel.getName());
				assert (ptf != null);
				try {
					ptf.get(tunnel).post(m);
				} catch (ClosedException e) {
					log.error("async sender has been closed and exit send task", e);
					break;
				}
				continue;
			}
		}
	}

}
