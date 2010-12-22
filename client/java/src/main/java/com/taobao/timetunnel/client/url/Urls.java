package com.taobao.timetunnel.client.url;

import com.taobao.timetunnel.client.impl.Authentication;
import com.taobao.timetunnel.client.util.ClosedException;
import com.taobao.timetunnel.client.util.URLException;

/**
 * 
 * @author <jiugao@taobao.com>
 * @created 2010-9-25
 * 
 */

public interface Urls {

	void setAuth(Authentication auth);

	ServerGroup getUrls(String routeKey, String type, int size, int timeout, boolean sequence) throws URLException, ClosedException;

	void stop();
}
