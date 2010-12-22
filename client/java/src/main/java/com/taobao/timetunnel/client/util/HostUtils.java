package com.taobao.timetunnel.client.util;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-15
 * 
 */
public class HostUtils {
	private static Logger log = Logger.getLogger(HostUtils.class);
	private static String id = null;

	public static String hostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error("{}", e);
			throw new RuntimeException(e);
		}
	}

	public synchronized static String id() {
		if (id == null) {
			id = ManagementFactory.getRuntimeMXBean().getName();
			if (id == null || id.trim().equals("")) {
				throw new RuntimeException("can not get hostid");
			}
		}
		return id;
	}

	public static void main(String[] args) {
		System.out.println(HostUtils.hostname());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(HostUtils.id());
	}
}
