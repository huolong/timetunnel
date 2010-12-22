package com.taobao.timetunnel.client;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * operations for retrieve message from TimeTunnel
 * 
 * @author <a href=mailto:jiugao@taobao.com>jiugao</a>
 * @created 2010-10-27
 * 
 */
public interface SubscribeFuture {

	/**
	 * block until receive message
	 * 
	 * @return a list of message and size not zero unless cancel method called
	 */
	List<Message> get();

	/**
	 * block until receive message or timeout
	 * 
	 * @return zero size message return when no message and timeout or cancel
	 *         method called
	 */
	List<Message> get(long timeout, TimeUnit unit);

	/**
	 * cancel this future, close underline tunnel
	 */
	void cancel();

}
