package com.taobao.timetunnel.client.broker;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-12-9
 * 
 */
public class PortNum {
	static final Random r = new Random();
	static final AtomicInteger ai = new AtomicInteger(10000);

	public static String randomPort() {
		int port = 10000 + r.nextInt(10000);
		return String.valueOf(port);
	}

	public static String incrementNum() {
		int port = ai.incrementAndGet();
		return String.valueOf(port);
	}
}
