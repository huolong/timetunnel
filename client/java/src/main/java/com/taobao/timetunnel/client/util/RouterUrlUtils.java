package com.taobao.timetunnel.client.util;

import java.util.Random;

import com.taobao.timetunnel.client.impl.Config;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-10-27
 * 
 */
public class RouterUrlUtils {
	private static Random r = new Random();

	public static String getRandomUrl() {
		String[] routerServerList = Config.getInstance().getRouterServerList();
		int length = routerServerList.length;
		if (length == 0)
			throw new RuntimeException("can not get router url");
		return routerServerList[r.nextInt(length)];
	}
}
