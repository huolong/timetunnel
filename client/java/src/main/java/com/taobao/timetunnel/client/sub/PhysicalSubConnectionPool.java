package com.taobao.timetunnel.client.sub;

import java.util.Collection;
import java.util.HashMap;

/**
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-11-15
 * 
 */
public class PhysicalSubConnectionPool {

	private HashMap<String, PhysicalSubConnection> pool = null;

	private static PhysicalSubConnectionPool instance = new PhysicalSubConnectionPool();

	private PhysicalSubConnectionPool() {
		pool = new HashMap<String, PhysicalSubConnection>();
	}

	public static PhysicalSubConnectionPool getInstance() {
		return instance;
	}

	public synchronized PhysicalSubConnection checkOut(String url) {
		if (pool.containsKey(url)) {
			PhysicalSubConnection ret = pool.get(url);
			ret.addRef();
			return ret;
		} else {
			PhysicalSubConnection con = new PhysicalSubConnection(url);
			con.addRef();
			pool.put(url, con);
			return con;
		}
	}

	public synchronized void checkIn(String url) {
		assert (pool.containsKey(url));
		if (pool.get(url).delRef())
			pool.remove(url);
	}

	public synchronized void stop() {
		Collection<PhysicalSubConnection> values = pool.values();
		for (PhysicalSubConnection con : values) {
			con.close();
		}
	}

}
