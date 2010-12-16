package com.taobao.timetunnel2.client.dsl.url;

import com.taobao.timetunnel2.client.dsl.impl.Authentication;
import com.taobao.timetunnel2.client.dsl.util.ClosedException;
import com.taobao.timetunnel2.client.dsl.util.URLException;

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
