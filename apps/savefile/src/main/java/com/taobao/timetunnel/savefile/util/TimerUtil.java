package com.taobao.timetunnel.savefile.util;

import java.util.Calendar;

import org.apache.log4j.Logger;

public class TimerUtil {
	private static long sleepTime = 0L;
	private static final Logger log = Logger.getLogger(TimerUtil.class);

	public static void sleep2(String fixedTime) {
		if (fixedTime == null) {
			throw new RuntimeException(
					"fixedTime input is null, please check your configuration");
		}
		try {
			sleepTime = diffTime(Calendar.getInstance(), fixedTime);
			log.debug("begin to sleep: " + sleepTime);
			Thread.sleep(sleepTime);
			log.debug("wake up from sleep: " + sleepTime);
		} catch (InterruptedException ignore) {
		}
	}

	private static long diffTime(Calendar cal, String fixedTime) {
		Calendar fixCal = Calendar.getInstance();
		String[] hm = fixedTime.split(":");
		if (hm.length != 2) {
			throw new RuntimeException(
					"fixedTime input is wrong, please check your configuration");
		}
		int hour = Integer.parseInt(hm[0]);

		int min = Integer.parseInt(hm[1]);
		if ((hour > 24) || (min > 60)) {
			throw new RuntimeException(
					"fixedTime input is wrong, please check your configuration");
		}
		fixCal.set(fixCal.get(Calendar.YEAR), fixCal.get(Calendar.MONTH),
				fixCal.get(Calendar.DATE), hour, min);
		if (cal.after(fixCal) || cal.equals(fixCal)) {
			fixCal.add(Calendar.DATE, 1);
		}

		return fixCal.getTimeInMillis() - cal.getTimeInMillis();
	}

	public static void main(String[] args) {
		System.out.println(TimerUtil.diffTime(Calendar.getInstance(), "09:52"));
	}
}
