package com.taobao.timetunnel.client.util;

import java.util.Random;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-15
 * 
 */
public class SleepUtils {
	public static void sleep(long sec) {
		try {
			Thread.sleep(sec);
		} catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
		}
	}

	public static void randomSleep(int sec) {
		Random r = new Random();
		int sleepSec = r.nextInt(sec);
		sleep(sleepSec);
	}

	public static void main(String[] args) {
		randomSleep(1000);
	}
}
