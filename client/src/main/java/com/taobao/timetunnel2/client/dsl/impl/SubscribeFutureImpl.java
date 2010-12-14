package com.taobao.timetunnel2.client.dsl.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.taobao.timetunnel2.client.dsl.Message;
import com.taobao.timetunnel2.client.dsl.SubscribeFuture;
import com.taobao.timetunnel2.client.dsl.message.MessageFactory;
import com.taobao.timetunnel2.client.dsl.sub.VirtualSubConnection;
import com.taobao.timetunnel2.client.dsl.sub.VirtualSubConnectionFactory;
import com.taobao.timetunnel2.client.dsl.util.BytesUtil;
import com.taobao.timetunnel2.client.dsl.util.ClosedException;
import com.taobao.timetunnel2.client.dsl.util.SleepUtils;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-28
 * 
 */
public class SubscribeFutureImpl implements SubscribeFuture {
	private static Logger log = Logger.getLogger(SubscribeFutureImpl.class);
	private final Tunnel t;
	private final VirtualSubConnection client;
	private final AtomicBoolean timeup;
	private final ScheduledExecutorService timer;
	private final AtomicBoolean stop;

	public SubscribeFutureImpl(Tunnel t) {
		this.t = t;
		this.timeup = new AtomicBoolean(false);
		stop = new AtomicBoolean(false);
		this.timer = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "timer");
			}
		});
		this.client = VirtualSubConnectionFactory.getInstance().create(t);
	}

	private void beginTimer(long timeout, TimeUnit unit) {
		timeup.set(false);
		timer.schedule(new Runnable() {
			@Override
			public void run() {
				timeup.set(true);
			}
		}, timeout, unit);
	}

	@Override
	public List<Message> get() {
		try {
			List<ByteBuffer> ackAndGet = null;
			while ((ackAndGet == null || ackAndGet.size() == 0) && !stop.get()) {
				ackAndGet = client.ackAndGet();
				if (ackAndGet == null || ackAndGet.size() == 0) {
					SleepUtils.sleep(100);
					continue;
				}
			}
			if (ackAndGet == null || ackAndGet.size() == 0) {
				throw new ClosedException("has been closed", null);
			}
			List<Message> ret = covertByteBuffer2Message(ackAndGet);
			return ret;
		} catch (ClosedException e) {
			log.error("tunnel closed", e);
			return new ArrayList<Message>();
		}
	}

	private List<Message> covertByteBuffer2Message(List<ByteBuffer> ackAndGet) {
		List<Message> ret = new ArrayList<Message>();
		for (Iterator<ByteBuffer> it = ackAndGet.iterator(); it.hasNext();) {
			Message m = MessageFactory.getInstance().createMessageFrom(BytesUtil.toBytes(it.next()));
			if (this.t.isCompress())
				m.compress();
			else
				m.decompress();
			ret.add(m);
		}
		return ret;
	}

	@Override
	public List<Message> get(long timeout, TimeUnit unit) {
		try {
			this.beginTimer(timeout, unit);
			List<ByteBuffer> ackAndGet = null;
			while ((ackAndGet == null || ackAndGet.size() == 0) && !stop.get() && !timeup.get()) {
				ackAndGet = client.ackAndGet();
				if (ackAndGet == null || ackAndGet.size() == 0) {
					SleepUtils.sleep(100);
					continue;
				}
			}
			if (ackAndGet == null || ackAndGet.size() == 0) {
				if (stop.get())
					throw new ClosedException("has been closed", null);
				else
					return new ArrayList<Message>();
			}
			List<Message> ret = covertByteBuffer2Message(ackAndGet);
			return ret;
		} catch (ClosedException e) {
			log.error("tunnel closed", e);
			return new ArrayList<Message>();
		}
	}

	@Override
	public void cancel() {
		log.error("INFO: Tunnel cancelled: " + this.t.getName());
		stop.set(true);
		VirtualSubConnectionFactory.getInstance().remove(t);
	}
}
