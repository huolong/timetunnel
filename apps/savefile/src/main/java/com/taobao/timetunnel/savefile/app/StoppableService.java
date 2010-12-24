package com.taobao.timetunnel.savefile.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-20
 * 
 */
public abstract class StoppableService implements Runnable {
	private static final Logger log = Logger.getLogger(StoppableService.class);
	private static final int STOP_TIMEOUT_IN_SECONDS = 120;

	public static void main(String args[]) throws Exception {
		StoppableService s = new StoppableService() {
			public void prepare() {
				System.out.println("prepare...");
			}

			public void execute() {
				System.out.println("execute...");
				try {
					Thread.sleep(400);
				} catch (Exception ignored) {
				}
			}

			public void shutdown() {
			}
		};
		new Thread(s).start();
		Thread.sleep(3000);
		s.stop();
		System.out.println("88");
		s.stop();
		System.out.println("888");
	}

	private AtomicBoolean stopRequested = new AtomicBoolean(false);
	private CountDownLatch stoppedSignal = new CountDownLatch(1);

	public void run() {
		prepare();
		while (!stopRequested.get()) {
			try {
				execute();
			} catch (Throwable e) {
				log.error("execution error", e);
				break;
			}
		}
		log.info("out of loop, shutting down...");
		stoppedSignal.countDown();
	}

	public void start() {
		log.info("starting up...");
		new Thread(this).start();
	}

	public void stop() {
		log.info("stop pending...");
		stopRequested.set(true);
		try {
			stoppedSignal.await(STOP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		log.info("shutdown...");
		shutdown();
		log.info("finish stopping...");
	}

	public abstract void prepare();

	public abstract void execute();

	public abstract void shutdown();
}
